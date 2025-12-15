package com.os.tutorials;
import com.os.tutorials.domain.Login;

import com.os.tutorials.domain.DmsRequests;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class LoginTutorial {


    public static void main(String[] args) {
        try {
            //crate a client
            OkHttpClient client = Login.buildClient();

            //test the client by calling an API endpoint
            Response getVersionResponse = client.newCall(DmsRequests.getVersion(client)).execute();
            System.out.println(getVersionResponse.body().string());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
