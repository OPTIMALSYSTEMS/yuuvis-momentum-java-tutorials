import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryTutorial {

    public static void main( String[] args )
    {
        /** String representation of the URL to the Authentication-Service of the enaio® system. */
        String baseUrl = "";
        /** User name of an user with authorization to retrieve audit entries for a specific object. */
        String userName = "";
        /** User password of an user with authorization to retrieve audit entries for a specific object. */
        String userPwd = "";
        /** Tenant name of the user with user name {@code userName}. */
        String userTenant = "";

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();

        String auth = "Basic " + Base64.getEncoder().encodeToString((userName + ":" + userPwd).getBytes());
        String objectId = "";
        String historyUrl = baseUrl + "/dms-core/objects/" + objectId + "/history";

        Request request = new Request.Builder()
                .header("Authorization", auth)
                .header("X-ID-TENANT-NAME", userTenant)
                .url(historyUrl)
                .get()
                .build();

        try
        {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
