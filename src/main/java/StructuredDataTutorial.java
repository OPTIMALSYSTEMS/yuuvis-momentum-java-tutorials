import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.util.Base64;

public class StructuredDataTutorial {
    public static String baseUrl = "";
    public static String username = "";
    public static String userpassword = "";
    public static String tenant = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient.Builder builder = new OkHttpClient.Builder();
    public static OkHttpClient client = builder.build();

    public static void main(String[] args) throws Exception
    {
        // Import the example app schema.
        importSchema();

        // Import two example objects and store their object IDs.
        String importedBook = importObject("./src/main/resources/mediumBook.json");
        String objectIdBook = parseObjectIdFromJsonResponse(importedBook);

        String importedCollection = importObject("./src/main/resources/mediumCollection.json");
        String objectIdCollection = parseObjectIdFromJsonResponse(importedCollection);

        // Request metadata of the imported objects to ensure the import is ready also in the Elasticsearch index.
        requestMetadata(objectIdBook);
        requestMetadata(objectIdCollection);

        // Example search queries.
        // (1) All objects of type medium.
        searchForObjects(
                "SELECT * FROM appBibjsonsample:medium",
                0,
                50);

        // (2) Return only the bibjson value.
        searchForObjects(
                "SELECT appBibjsonsample:bibjson FROM appBibjsonsample:medium",
                0,
                50);

        // (3) Return only parts of the bibjson value.
        searchForObjects(
                "SELECT appBibjsonsample:bibjson.title,appBibjsonsample:bibjson.author[1].name,appBibjsonsample:bibjson.metadata.id FROM appBibjsonsample:medium",
                0,
                50);

        // (4) Search for a medium by its bibliographic ID.
        searchForObjects(
                "SELECT appBibjsonsample:bibjson.title FROM appBibjsonsample:medium WHERE appBibjsonsample:bibjson.id = 'ID_of_book'",
                0,
                50);

        // (5) If the path is unknown, a '*' wildcard can be used.
        searchForObjects(
                "SELECT appBibjsonsample:bibjson.metadata.id FROM appBibjsonsample:medium WHERE appBibjsonsample:bibjson.* = 'ID_of_schema_article'",
                0,
                50);

        // (6) Search within a list and full-text search with CONTAINS.
        searchForObjects(
                "SELECT appBibjsonsample:bibjson.title FROM appBibjsonsample:medium WHERE appBibjsonsample:bibjson.author[*].name CONTAINS('Sturz')",
                0,
                50);

        // Delete the example objects.
        deleteObject(objectIdBook);
        deleteObject(objectIdCollection);

    }


    public static void importSchema() throws Exception
    {
        // Specify the file to be imported as schema.
        String filename = "./src/main/resources/schemaBibjsonsampleApp.xml";

        try{
            // Configure the request body for the schema import.
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "schema.xml", RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), new File(filename)))
                    .build();

            // Configure the request, add the request body.
            Request getSchemaRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/system/apps/bibjsonsample/schema")
                    .post(requestBody)
                    .build();

            //Execute the schema import. Store and print the validation response returned by the called endpoint.
            Response validateResponse = client.newCall(getSchemaRequest).execute();
            String validateResponseString = validateResponse.body().string();
            System.out.println(validateResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String importObject(String filepath) throws Exception
    {
        String importedObjectAsString = "";
        try {

            // Configure the import of metadata to create an object without content.
            Request importRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+"/api/dms/objects")
                    .post(RequestBody.create(JSON, new File(filepath)))
                    .build();

            // Execute the import of an object and print the response returned by the called endpoint.
            Response importResponse = client.newCall(importRequest).execute();
            importedObjectAsString = importResponse.body().string();
            System.out.println(importedObjectAsString);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return importedObjectAsString;
    }

    public static String parseObjectIdFromJsonResponse(String responseJson){
        JSONObject jsonObject = new JSONObject(responseJson);
        // Extract the object ID from the 'responseJson' given as argument.
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }

    public static void requestMetadata(String objectId)
    {
        try {
            // Configure the retrieval request for the metadata of the object specified by 'objectId'.
            Request metadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "/api/dms/objects/" + objectId)
                    .get().build();

            // Execute the metadata request and print the response returned by the called endpoint.
            Response metadataResponse = client.newCall(metadataRequest).execute();
            String metadataResponseString = metadataResponse.body().string();
            System.out.println(metadataResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void searchForObjects(String statement, int skipCount, int maxItems) throws Exception
    {
        try {
            JSONObject queryObject = new JSONObject();
            JSONObject queryAttributes = new JSONObject();
            queryAttributes.put("statement", statement);
            queryAttributes.put("skipCount", skipCount);
            queryAttributes.put("maxItems", maxItems);
            queryObject.put("query", queryAttributes);
            String searchQuery = queryObject.toString();

            Request searchRequest = new Request.Builder()
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + userpassword).getBytes()))
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/search")
                    .post(RequestBody.create(JSON, searchQuery))
                    .build();
            Response searchResponse = client.newCall(searchRequest).execute();
            System.out.println(searchResponse.body().string());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteObject(String objectId) throws Exception
    {
        try {
            // Configure the deletion of the object specified by objectId.
            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/"+objectId)
                    .delete().build();

            // Execute the deletion.
            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Successfully deleted.");
            else System.out.println("Error while deleting: "+deleteResponse.code());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
