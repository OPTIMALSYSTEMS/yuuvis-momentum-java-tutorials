package com.os.tutorials.domain;

import okhttp3.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Base64;

public class Login {
    public static final String username = "root";
    public static final String userpassword = "changeme";
    public static final String auth = "Basic "+ Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "myfirsttenant";
    public static final String baseUrl = "http://123.456.78.9:30080";


    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            Response getVersionResponse = client.newCall(DmsRequests.getVersion(client)).execute();
            System.out.println(getVersionResponse.body().string());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    public static OkHttpClient buildClient() {
        CookieJar cookieJar = new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();

        return client;
    }

    public Login() {}
}
