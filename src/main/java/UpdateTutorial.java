import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateTutorial {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";

    private static class MyCookieJar implements CookieJar {

        private List<Cookie> cookies;

        public MyCookieJar() {
        }

        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            this.cookies =  cookies;
        }

        public List<Cookie> loadForRequest(HttpUrl url) {
            if (cookies != null)
                return cookies;
            return new ArrayList<Cookie>();

        }
    }


    public static void main(String[] args) {
        String objectId = "";

        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cookieJar(new MyCookieJar());
            OkHttpClient client = builder.build();

            //Initial Import
            RequestBody importRequestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data",
                            "metaData.json",
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

            //Update Metadata
            Request updateMetadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId)
                    .post(RequestBody.create(JSON, new File("./src/main/resources/metadataUpdate.json")))
                    .build();

            Response updateMetadataResponse = client.newCall(updateMetadataRequest).execute();
            String updateMetadataResponseString = updateMetadataResponse.body().string();
            System.out.println(updateMetadataResponseString);

            //wait for index service to catch up
            TimeUnit.SECONDS.sleep(1);

            //Update Content
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

            //getting specific version of an object
            Request versionMetadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "/api/dms/objects/" + objectId + "/versions/1")
                    .get().build();

            Response versionMetadataResponse = client.newCall(versionMetadataRequest).execute();
            String versionMetadataResponseString = versionMetadataResponse.body().string();
            System.out.println(versionMetadataResponseString);

            //getting content of specific version of an object
            Request versionContentRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "/api/dms/objects/" + objectId + "/versions/2/contents/file")
                    .get().build();

            Response versionContentResponse = client.newCall(versionContentRequest).execute();
            String versionContentResponseString = versionContentResponse.body().string();
            System.out.println(versionContentResponseString);



        } catch (Exception e) {
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
