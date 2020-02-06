import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ImportTutorial {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType OCTETSTREAM = MediaType.parse("application/octetstream; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";



    public static void main(String[] args) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cookieJar(new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL)));
            OkHttpClient client = builder.build();

            //Single Import
            RequestBody singleImportRequestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data",
                            "metaData.json",
                            RequestBody.create(JSON,
                                    new File("./src/main/resources/metaData.json")))
                    .addFormDataPart("cid_63apple",
                            "test.txt",
                            RequestBody.create(PLAINTEXT,
                                    new File("./src/main/resources/test.txt")))
                    .build();
            Request singleImportRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(singleImportRequestBody)
                    .build();

            Response singleImportResponse = client.newCall(singleImportRequest).execute();
            System.out.println(singleImportResponse.body().string());

            //Batch Import
            RequestBody batchImportRequestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data",
                            "metaDataBatch.json",
                            RequestBody.create(JSON,
                                    new File("./src/main/resources/metaDataBatch.json")))
                    .addFormDataPart("cid_63apple",
                            "test1.txt",
                            RequestBody.create(PLAINTEXT,
                                    new File("./src/main/resources/test1.txt")))
                    .addFormDataPart("cid_64apple",
                            "test2.txt",
                            RequestBody.create(PLAINTEXT,
                                    new File("./src/main/resources/test2.txt")))
                    .build();
            Request batchImportRequest = new Request.Builder()
                    //.header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(batchImportRequestBody)
                    .build();

            Response batchImportResponse = client.newCall(batchImportRequest).execute();
            System.out.println(batchImportResponse.body().string());


            RequestBody compoundDocumentImportBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metaDataCompound.json", RequestBody.create(JSON, new File("./src/main/resources/metaDataCompound.json")))
                    .addFormDataPart("cid_63apple", "compound-forward-document.bin", RequestBody.create(OCTETSTREAM, new File("./src/main/resources/compound-forward-document.bin")))
                    .build();
            Request compoundDocumentImportRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME",tenant)
                    .url(baseUrl + "objects")
                    .post(compoundDocumentImportBody)
                    .build();

            Response compoundDocumentImportResponse = client.newCall(compoundDocumentImportRequest).execute();
            String compoundDocumentResponseString = compoundDocumentImportResponse.body().string();
            System.out.println(compoundDocumentResponseString);
            String contentStreamId = getContentStreamIdFromJsonResponse(compoundDocumentResponseString);
            System.out.println(contentStreamId);
            String repositoryId = getRepositoryIdFromJsonResponse(compoundDocumentResponseString);
            System.out.println(repositoryId);
            String archivePath = getArchivePathFromJsonResponse(compoundDocumentResponseString);
            System.out.println(archivePath);

            JSONObject afterCompoundDocumentJson = new JSONObject(new String (Files.readAllBytes(Paths.get("./src/main/resources/metaDataCompoundAfter.json"))));
            afterCompoundDocumentJson.getJSONArray("objects").getJSONObject(0).getJSONArray("contentStreams").getJSONObject(0).put("contentStreamId", contentStreamId);
            afterCompoundDocumentJson.getJSONArray("objects").getJSONObject(0).getJSONArray("contentStreams").getJSONObject(0).put("repositoryId", repositoryId);
//            afterCompoundDocumentJson.getJSONArray("objects").getJSONObject(0).getJSONArray("contentStreams").getJSONObject(0).put("archivePath", archivePath);
            String afterCompoundDocumentJsonString = afterCompoundDocumentJson.toString();

            System.out.println(afterCompoundDocumentJsonString);

            //wait for index service to catch up
            TimeUnit.SECONDS.sleep(5);

            RequestBody compoundDocumentPartImportBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metaDataCompoundAfter.json", RequestBody.create(JSON, afterCompoundDocumentJsonString))
                    .build();

            Request importCompoundPartDocumentRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(compoundDocumentPartImportBody)
                    .build();

            Response importCompoundPartDocumentResponse = client.newCall(importCompoundPartDocumentRequest).execute();
            System.out.println(importCompoundPartDocumentResponse.body().string());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getContentStreamIdFromJsonResponse(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("contentStreamId");
    }

    private static String getRepositoryIdFromJsonResponse(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("repositoryId");
    }

    private static String getArchivePathFromJsonResponse(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("archivePath");
    }
    
}
