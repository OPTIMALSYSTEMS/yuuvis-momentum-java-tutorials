package com.os.tutorials;

import org.json.JSONObject;


public class DmsResponses {


    public static String getObjectId(String responseJson) {
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }
    public static String getContentStreamId(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("contentStreamId");
    }

    public static String getRepositoryId(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("repositoryId");
    }

    public static String getArchivePath(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("archivePath");
    }

    public DmsResponses() {}
}
