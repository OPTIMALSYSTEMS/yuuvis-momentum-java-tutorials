import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Base64;

public class RoleSetTutorial {

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

            //getting role set for a tenant
            Request getRoleSetRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "admin/permissions")
                    .get().build();

            Response roleSetResponse = client.newCall(getRoleSetRequest).execute();
            String roleSetResponseString = roleSetResponse.body().string();
            System.out.println(roleSetResponseString);

            //updating role set for a tenant
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "permissions.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), roleSetResponseString))
                    .build();

            Request postRoleSetRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "admin/permissions")
                    .post(requestBody)
                    .build();

            Response postRoleSetResponse = client.newCall(postRoleSetRequest).execute();
            String postRoleSetResponseString = postRoleSetResponse.body().string();
            System.out.println(postRoleSetResponseString);

            //validating role set of a tenant
            RequestBody validationRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "permissions.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), roleSetResponseString))
                    .build();

            Request validationRoleSetRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "admin/permissions/validate")
                    .post(validationRequestBody)
                    .build();

            Response valRoleSetResponse = client.newCall(validationRoleSetRequest).execute();
            String valRoleSetResponseString = valRoleSetResponse.body().string();
            System.out.println(valRoleSetResponseString);

            //getting the global role set
            Request getGlobalRoleSetRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "system/permissions")
                    .get().build();

            Response globalRoleSetResponse = client.newCall(getGlobalRoleSetRequest).execute();
            String roleSetGlobalResponseString = globalRoleSetResponse.body().string();
            System.out.println(roleSetGlobalResponseString);

            //updating the global role set
            RequestBody globalRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "permissions.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), roleSetGlobalResponseString))
                    .build();

            Request postGlobalRoleSetRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "system/permissions")
                    .post(globalRequestBody)
                    .build();

            Response globalPostRoleSetResponse = client.newCall(postGlobalRoleSetRequest).execute();
            String globalPostRoleSetResponseString = globalPostRoleSetResponse.body().string();
            System.out.println(globalPostRoleSetResponseString);

            //validating the global role set
            RequestBody globalValidationRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "permissions.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), roleSetGlobalResponseString))
                    .build();

            Request globalValidationRoleSetRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "system/permissions/validate")
                    .post(globalValidationRequestBody)
                    .build();

            Response globalValRoleSetResponse = client.newCall(globalValidationRoleSetRequest).execute();
            String globalValidationRoleSetResponseString = globalValRoleSetResponse.body().string();
            System.out.println(globalValidationRoleSetResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
