package com.os.tutorials.domain;

import org.json.JSONObject;

import okhttp3.*;
import java.io.FileOutputStream;
import java.io.InputStream;


public class ResourceResponses {


    public static void saveXMLResource (Response response) {
        try {
            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream("downloaded_file.xml");
            out.write(in.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ResourceResponses() {}
}
