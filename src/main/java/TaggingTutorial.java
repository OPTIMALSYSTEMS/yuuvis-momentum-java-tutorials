import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Base64;

public class TaggingTutorial {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+ Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";

    public static void main(String[] args) {

        String objectId = "";

        try {
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
                    .url(baseUrl + "objects")
                    .post(importRequestBody)
                    .build();

            Response importResponse = client.newCall(importRequest).execute();
            String importResponseString = importResponse.body().string();
            System.out.println(importResponseString);

            //parse created ObjectId from Import Response Text
            objectId = parseObjectIdFromJsonResponse(importResponseString);

            String tagName = "testTag";
            String tagValue = "1";

            //Add Tag
            Request addTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/" + objectId + "/tags/" + tagName + "/state/" + tagValue)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            Response addTagResponse = client.newCall(addTagRequest).execute();
            String addTagResponseString = addTagResponse.body().string();
            System.out.println(addTagResponseString);

            //getting tags of imported object
            Request getTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "objects/" + objectId + "/tags")
                    .get().build();

            Response tagResponse = client.newCall(getTagRequest).execute();
            String tagResponseString = tagResponse.body().string();
            System.out.println(tagResponseString);

            String newTagValue = "2";

            //Update Tag
            Request updateTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/" + objectId + "/tags/" + tagName + "/state/" + newTagValue + "?overwrite=true")
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            Response updateTagResponse = client.newCall(updateTagRequest).execute();
            String updateTagResponseString = updateTagResponse.body().string();
            System.out.println(updateTagResponseString);

            String statement = "SELECT * FROM system:object where system:creationDate=Today()";

            //Query Tag
            Request queryTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/tags/" + tagName + "/state/" + newTagValue + "?query=" + statement)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            Response queryTagResponse = client.newCall(queryTagRequest).execute();
            String queryTagResponseString = queryTagResponse.body().string();
            System.out.println(queryTagResponseString);

            //delete tag
            Request deleteTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/"+objectId + "/tags/" + tagName)
                    .delete().build();

            Response deleteTagResponse = client.newCall(deleteTagRequest).execute();

            if(deleteTagResponse.code() == 200) System.out.println("Successfully deleted.");
            else System.out.println("Error while deleting: "+deleteTagResponse.code());

            //delete object
            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/"+objectId)
                    .delete().build();

            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Successfully deleted.");
            else System.out.println("Error while deleting: "+deleteResponse.code());
        }
        catch (Exception e) {
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
