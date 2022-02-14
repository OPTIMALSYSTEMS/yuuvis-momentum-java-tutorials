package com.example.demo;

import com.google.gson.Gson;
import okhttp3.Authenticator;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>Creates an OkHttpClient bean for automatized login to a yuuvis Momentum system</p>
 *
 * <p>
 * If a Client certificate has been configured, it is used for the login. Otherwise username and password are used.
 * The login via certificate uses the OAuth 2.0 Password Grant Flow.
 * </p>
 *
 */
@Configuration
public class HttpClientConfiguration
{
	@Autowired ApplicationContext applicationContext;

	/**
	 * The OkHttpClient has to trust SSL protected Hosts and SSL is required for certificate authentication.
	 * This X509TrustManager bean makes the OkHttpClient accepting self signed SSL certificates without being imported to the client's truststore.
	 * It's configured this way to make this example easier to handle. In productive environments you have to import server certificates to your truststore.
	 * Consult the OkHttpClient Documentation for that (https://square.github.io/okhttp/features/https/#customizing-trusted-certificates-kt-java).
	 * This has nothing to do with the certificate authentication procedure.
	 *
	 * @return X509TrustManager for accepting self signed SSL certificates
	 */
	@Bean
	X509TrustManager trustManager()
	{
		return new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
			public void checkClientTrusted(X509Certificate[] certs, String authType) {}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		};
	}

	@Bean
	CookieManager cookieManager()
	{
		return new CookieManager(){{setCookiePolicy(CookiePolicy.ACCEPT_ALL);}};
	}

	/**
	 * This Interceptor renames incoming session cookies with the tenant name and puts them in the cookie store. Before
	 * sending a new request, it removes all session cookies but the one that belongs to the tenant and restores the name.
	 *
	 * It makes sure, you use the correct session for each tenant.
	 *
	 * @return Interceptor
	 */
	@Bean
	Interceptor cookieHandler()
	{
		return chain ->
		{
			if (chain.request().header("X-ID-TENANT-NAME") == null) return chain.proceed(chain.request());

			// applies a filter to remove all Sessions but the one that belongs to the tenant and renames it correctly before sending the request
			Response response = chain.proceed(chain.request().header("Cookie") == null ? chain.request().newBuilder().build() :
							chain.request().newBuilder().removeHeader("Cookie").addHeader("Cookie",
											Arrays.stream(StringUtils.splitByWholeSeparator(chain.request().header("Cookie"), "; "))
															.filter(s -> s.startsWith("GWSESSIONID_" + chain.request().header("X-ID-TENANT-NAME")) || !s.startsWith("GWSESSIONID"))
															.map(s -> StringUtils.replace(s,"GWSESSIONID_" + chain.request().header("X-ID-TENANT-NAME"), "GWSESSIONID"))
															.collect(Collectors.joining(", "))
							).build());

			String session = response.headers().values("Set-Cookie").stream().filter(s -> s.startsWith("GWSESSIONID")).findAny().orElse(null);
			if (session == null) return response;

			// saves new session cookies with the tenant in its name
			session = StringUtils.replace(session, "GWSESSIONID", "GWSESSIONID_" + chain.request().header("X-ID-TENANT-NAME"));
			cookieManager().put(chain.request().url().uri(), Collections.singletonMap("Set-Cookie", Arrays.asList(session)));

			return response;
		};
	}

	/**
	 * This Authenticator realizes response status code 401 (Unauthorized) and initiates authentication with client certificate.
	 * For that is uses the OAuth 2.0 Password Grant Flow to get an access token and adds it to copy of the request before repeating it.
	 *
	 * @param tokenEndpoint comes from the service configuration {@code oidc.token-endpoint}
	 * @param certificateClient comes from the service configuration {@code oidc.certificate.client}
	 * @return Authenticator to authenticate with client certificate
	 */
	@Bean
	@ConditionalOnProperty("oidc.certificate.pem")
	Authenticator certificateAuthenticator(@Value("${oidc.token-endpoint}") String tokenEndpoint, @Value("${oidc.certificate.client}") String certificateClient)
	{
		return (route, response) ->
		{
			if (response.request().header("X-ID-TENANT-NAME") == null) return null;

			Request request = new Request.Builder()
							.url(StringUtils.replace(tokenEndpoint, "$(tenant)", response.request().header("X-ID-TENANT-NAME")))
							.post(new FormBody.Builder()
											.add("client_id", certificateClient)
											.add("grant_type", "password")
											.build())
							.build();

			Response authResponse = this.applicationContext.getBean(OkHttpClient.class).newCall(request).execute();

			if (authResponse.code() != 200) return null;

			String token = (String)(new Gson()).fromJson(authResponse.body().string(), Map.class).get("access_token");

			return response.request().newBuilder().header("Authorization", "Bearer " + token).build();
		};
	}

	/**
	 * This Authenticator realizes response status code 401 (Unauthorized) and initiates authentication with username and password.
	 * It is used if no certificate was configured.
	 *
	 * @param username comes from the service configuration {@code oidc.username}
	 * @param password comes from the service configuration {@code oidc.password}
	 * @return Authenticator to authenticate with username and password
	 */
	@Bean
	@ConditionalOnMissingBean
	Authenticator credentialsAuthenticator(@Value("${oidc.username}") String username, @Value("${oidc.password}") String password)
	{
		return (route, response) ->
		{
			String credentials = new String(Base64.encodeBase64((username + ":" + password).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			return response.request().newBuilder().header("Authorization", "Basic " + credentials).build();
		};
	}

	/**
	 * When working with client certificates a keystore is required for the OkHttpClient.
	 * The functions loads the certificate from the configuration and puts it in a generated keystore.
	 *
	 * @param pem comes from the service configuration {@code oidc.certificate.pem}
	 * @return generated keystore with the client certificate
	 */
	@Bean
	@ConditionalOnProperty("oidc.certificate.pem")
	KeyStore clientCertificate(@Value("${oidc.certificate.pem}") String pem) throws Exception
	{
		// load certificate
		String key = StringUtils.substringBetween(pem,"-----BEGIN PRIVATE KEY-----","-----END PRIVATE KEY-----");
		String cert = StringUtils.substringBetween(pem,"-----BEGIN CERTIFICATE-----","-----END CERTIFICATE-----");

		// embedding certificate in a keystore
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
		PrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(cert)));
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, "KEY".toCharArray());
		keyStore.setCertificateEntry(UUID.randomUUID().toString(), certificate);
		keyStore.setKeyEntry("KEY", privKey, "KEY".toCharArray(), new Certificate[] {certificate});

		return keyStore;
	}

	/**
	 * The OkHttpClient needs a SSLSocketFactory for both, SSL and certificate authentication.
	 * The truststore and the keystore are registered at the factory.
	 *
	 * @param clientCertificate injection of the keystore containing the client certificate
	 * @return SSLSocketFactory
	 */
	@Bean
	SSLSocketFactory socketFactory(@Qualifier("clientCertificate") @Autowired(required = false) KeyStore clientCertificate) throws Exception
	{
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(clientCertificate, "KEY".toCharArray());
		KeyManager[] keyManagers = kmf.getKeyManagers();
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(clientCertificate != null ? keyManagers : null, new TrustManager[]{trustManager()}, new java.security.SecureRandom());

		return context.getSocketFactory();
	}

	/**
	 * Creates an OkHttpClient that accepts arbitrary self signed certificates for SSL and automatic login at yuuvis momentum when needed.
	 *
	 * @param socketFactory injection of the SSLSocketFactory containing truststore and keystore
	 * @param authenticator injection of the authenticator for automatic login
	 * @return OkHttpClient
	 */
	@Bean
	OkHttpClient okHttpClient(SSLSocketFactory socketFactory, Authenticator authenticator)
	{
		return new OkHttpClient.Builder()
						.connectTimeout(1, TimeUnit.MINUTES)
						.readTimeout(10, TimeUnit.MINUTES)
						.writeTimeout(10, TimeUnit.MINUTES)
						.hostnameVerifier((s, sslSession) -> true)
						.followRedirects(true)
						.followSslRedirects(true)
						.authenticator(authenticator)
						.addNetworkInterceptor(cookieHandler())
						.cookieJar(new JavaNetCookieJar(cookieManager()))
						.sslSocketFactory(socketFactory, trustManager())
						.build();
	}
}
