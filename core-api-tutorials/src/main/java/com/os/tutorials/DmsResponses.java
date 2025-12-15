package com.os.tutorials;

import okhttp3.Response;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;


public class DmsResponses {


    public static String getObjectId(String responseJson) {
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }

    public static void saveTxtFile (Response response) {
        try {
            InputStream in = response.body().byteStream();
            FileOutputStream out = new FileOutputStream("downloaded_file.txt");
            out.write(in.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DmsResponses() {}
}
