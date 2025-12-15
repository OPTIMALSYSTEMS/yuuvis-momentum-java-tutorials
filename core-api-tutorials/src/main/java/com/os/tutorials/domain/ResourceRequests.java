package com.os.tutorials.domain;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
public class ResourceRequests {
    public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    public static Request getAppliedSchema () {

        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .header("Accept", "application/xml")
                .url(Login.baseUrl + "/api/dms/schema/native")
                .get()
                .build();
    }

    public static Request validateAppSchema (String app, String filename) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "schema.xml", RequestBody.create(XML, new File(filename)))
                .build();

        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl + "/api/system/apps/"+app+"/schema/validate")
                .post(requestBody)
                .build();
    }

    public static Request importAppSchema (String app, String filename) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "schema.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), new File(filename)))
                .build();

        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl + "/api/system/apps/"+app+"/schema")
                .post(requestBody)
                .build();
    }

    public ResourceRequests() {}
}
