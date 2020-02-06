import okhttp3.*;
import okhttp3.MediaType;
import org.json.JSONObject;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

public class TwoPartImportTutorial {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String tenant = "";
    public static final String baseUrl = "";

    public static String metadataFilePath = "D:\\Projects\\redcloudtutorial\\redcloudtutorial-two-part-import\\src\\main\\resources\\metadata.json";
    public static String contentFilePath = "D:\\Projects\\redcloudtutorial\\redcloudtutorial-two-part-import\\src\\main\\resources\\test.txt";

    public static void main(String[] args) {
        try{
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cookieJar(new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL)));
            OkHttpClient client = builder.build();

            //first, only import Metadata
            Request importMetadataRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)                    .url(baseUrl+"/dms/objects")
                    .post(RequestBody.create(JSON, new File(metadataFilePath)))
                    .build();

            Response importMetadataResponse = client.newCall(importMetadataRequest).execute();
            String importMetadataResponseBodyString = importMetadataResponse.body().string();
            System.out.println(importMetadataResponseBodyString);

            //then extract the objectID from the response
            JSONObject importResponseJsonObject = new JSONObject(importMetadataResponseBodyString);
            String objectId = importResponseJsonObject
                    .getJSONArray("objects")
                    .getJSONObject(0)
                    .getJSONObject("properties")
                    .getJSONObject("system:objectId")
                    .getString("value");

            //wait for index service to catch up
            TimeUnit.SECONDS.sleep(1);

            //finally update the content to the /contents/file endpoint of our object
            Request updateContentRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("Content-Disposition", "attachment; filename=\"test.txt\"")
                    .url(baseUrl+"/dms/objects/"+objectId+"/contents/file")
                    .post(RequestBody.create(PLAINTEXT, new File(contentFilePath)))
                    .build();
            Response updateContentResponse = client.newCall(updateContentRequest).execute();
            System.out.println(updateContentResponse.code());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
