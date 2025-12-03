package com.os.tutorials;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.lang.Object;

public class DmsRequests {

    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request importPlainText(String resourcesDirectory, String metadataSource, String contentSource, MediaType contenttype) {
        RequestBody singleImportRequestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data",
                        metadataSource,
                        RequestBody.create(JSON, new File(resourcesDirectory + metadataSource)))
                .addFormDataPart("cid0 ",
                        "schneiderlein.txt",
                        RequestBody.create(PLAINTEXT, new File("./src/main/resources/schneiderlein.txt")))
                .build();

        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl + "/api/dms/objects")
                .post(singleImportRequestBody)
                .build();

    }

    public static Request getMetadata(String objectId) {
        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl+ "/api/dms/objects/" + objectId)
                .get().build();


    }

    public DmsRequests() {}
}
