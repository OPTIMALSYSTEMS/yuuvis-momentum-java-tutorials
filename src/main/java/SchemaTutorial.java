import java.io.File;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SchemaTutorial {
    public static String baseUrl = "";
    public static String username = "";
    public static String userpassword = "";
    public static String tenant = "";

    public static void main(String[] args) throws Exception
    {
        getSchema();
//        validateSchema();
//        importSchema();
    }

    public static void getSchema() throws Exception
    {

        Request getSchemaRequest = new Request.Builder()
                .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                .header("X-ID-TENANT-NAME", tenant)
                .url(baseUrl + "/api/admin/schema")
                .get()
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();


        {
            Response activeSchemaResponse = client.newCall(getSchemaRequest).execute();
            String activeSchemaAsString = activeSchemaResponse.body().string();
            System.out.println(activeSchemaAsString);
        }

        try(FileOutputStream fos = new FileOutputStream("./src/main/resources/activeSchema.xml")) {
            Response activeSchemaResponse = client.newCall(getSchemaRequest).execute();
            byte[] bytes = activeSchemaResponse.body().bytes();
            fos.write(bytes);
        }

    }


    public static void validateSchema() throws Exception
    {

        String filename = "./src/main/resources/schemaToValidate.xml";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "schema.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), new File(filename)))
                .build();

        Request getSchemaRequest = new Request.Builder()
                .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                .header("X-ID-TENANT-NAME", tenant)
                .url(baseUrl + "/api/admin/schema/validate")
                .post(requestBody)
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();


        {
            Response validationResponse = client.newCall(getSchemaRequest).execute();
            System.out.println(validationResponse.code());
            String validationResponseAsString = validationResponse.body().string();
            System.out.println(validationResponseAsString);
        }

        try(FileOutputStream fos = new FileOutputStream("./src/main/resources/validationResult.json")) {
            Response validationResponse = client.newCall(getSchemaRequest).execute();
            byte[] bytes = validationResponse.body().bytes();
            fos.write(bytes);
        }
    }

    public static void importSchema() throws Exception
    {

        String filename = "./src/main/resources/schemaToImport.xml";


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "schema.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), new File(filename)))
                .build();

        Request getSchemaRequest = new Request.Builder()
                .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                .header("X-ID-TENANT-NAME", tenant)
                .url(baseUrl + "/api/admin/schema")
                .post(requestBody)
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();


        {
            Response validateResponse = client.newCall(getSchemaRequest).execute();
            String validateResponseString = validateResponse.body().string();
            System.out.println(validateResponseString);
        }

        try(FileOutputStream fos = new FileOutputStream("./src/main/resources/validationResult.json")) {
            Response validateResponse = client.newCall(getSchemaRequest).execute();
            byte[] bytes = validateResponse.body().bytes();
            fos.write(bytes);
        }
    }
}
