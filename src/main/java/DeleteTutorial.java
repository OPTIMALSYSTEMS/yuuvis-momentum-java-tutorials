import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeleteTutorial {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";



    public static void main(String[] args) {
        String objectId = "";

        try{
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cookieJar(new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL)));
            OkHttpClient client = builder.build();

            //Initial Import
            RequestBody importRequestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data",
                            "metadata.json",
                            RequestBody.create(JSON,
                                    new File("./src/main/resources/metadata.json")))
                    .addFormDataPart("cid_63apple",
                            "test.txt",
                            RequestBody.create(PLAINTEXT,
                                    new File("./src/main/resources/test.txt")))
                    .build();
            Request importRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects")
                    .post(importRequestBody)
                    .build();

            Response importResponse = client.newCall(importRequest).execute();
            String importResponseString = importResponse.body().string();
            System.out.println(importResponseString);

            //parse created ObjectId from Import Response Text
            objectId = parseObjectIdFromJsonResponse(importResponseString);

            //wait for index service to catch up
            TimeUnit.SECONDS.sleep(5);

            //Update content
            Request updateContentRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("Content-Disposition", "attachment; filename=\"test2.txt\"")
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/contents/file")
                    .post(RequestBody.create(PLAINTEXT, new File("./src/main/resources/test2.txt")))
                    .build();

            Response updateContentResponse = client.newCall(updateContentRequest).execute();
            String updateContentResponseString = updateContentResponse.body().string();
            System.out.println(updateContentResponseString);


            //wait for index service to catch up
            TimeUnit.SECONDS.sleep(1);

            //delete old version
            Request deleteVersionRequest = new Request.Builder()
//                    .header("Authorization", "Basic "+auth)
                    .header("X-ID-TENANT-NAME", "")
                    .url(baseUrl + "/api/dms/objects/"+objectId+"/versions/1")
                    .delete().build();

            Response deleteVersionResponse = client.newCall(deleteVersionRequest).execute();

            if(deleteVersionResponse.code( ) == 200) System.out.println("Successfully deleted old version.");
            else System.out.println("Error while deleting old version: "+deleteVersionResponse.code());

            //delete object
            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/"+objectId)
                    .delete().build();

            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Successfully deleted.");
            else System.out.println("Error while deleting: "+deleteResponse.code());

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String parseObjectIdFromJsonResponse(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }
}
