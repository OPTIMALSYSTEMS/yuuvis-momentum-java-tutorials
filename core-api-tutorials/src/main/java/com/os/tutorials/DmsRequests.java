package com.os.tutorials;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.lang.Object;
import java.util.HashMap;

public class DmsRequests {

    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType JPEG = MediaType.parse("image/jpeg; charset=utf-8");

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request importSingleDocument(String resourcesDirectory, String metadataSource, String contentSource, MediaType contenttype) {
        RequestBody singleImportRequestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data",
                        metadataSource,
                        RequestBody.create(JSON, new File(resourcesDirectory + metadataSource)))
                .addFormDataPart("cid0 ",
                        contentSource,
                        RequestBody.create(contenttype, new File(resourcesDirectory + contentSource)))
                .build();

        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl + "/api/dms/objects")
                .post(singleImportRequestBody)
                .build();

    }

    public static Request batchImportTextAndImage(String resourcesDirectory, String metadataSource, String contentSource1, String contentSource2) {
        RequestBody batchImportRequestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data",
                        metadataSource,
                        RequestBody.create(JSON,
                                new File(resourcesDirectory+metadataSource)))
                .addFormDataPart("cid1",
                        contentSource1,
                        RequestBody.create(PLAINTEXT,
                                new File(resourcesDirectory+contentSource1)))
                .addFormDataPart("cid2",
                        contentSource2,
                        RequestBody.create(JPEG,
                                new File(resourcesDirectory+contentSource2)))
                .build();

        return new Request.Builder()
                //.header("Authorization", auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl + "/api/dms/objects")
                .post(batchImportRequestBody)
                .build();
    }

    public static Request getMetadata(String objectId) {
        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl+ "/api/dms/objects/" + objectId)
                .get().build();
    }

    public static Request getContent(String objectId) {
        return new Request.Builder()
                .header("Authorization", Login.auth)
                .header("X-ID-TENANT-NAME", Login.tenant)
                .url(Login.baseUrl+ "/api/dms/objects/" + objectId + "/contents/file")
                .get().build();
    }

    public DmsRequests() {}
}
