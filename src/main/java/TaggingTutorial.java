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

    private static CookieJar cookieJar = new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    private static OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();

    public static void main(String[] args) throws Exception {
        // Import of an example object.
        String importedObject = importExampleObject();

        // Parse objectId of the imported object.
        String objectId = parseObjectIdFromJsonResponse(importedObject);

        // Add tags via Tagging API, without specifying a traceId.
        addTag(objectId, "analysis", "1");
        addTag(objectId, "testtag", "7");

        // Retrieve tag values via Tagging API.
        // This call is included in this tutorial not only to demonstrate the usage of the corresponding tagging
        // endpoint, but also to avoid a hard coded waiting time to ensure that the tags are added also in the
        // Elasticsearch index.
        getTags(objectId);

        // Query and update a tag.
        queryTag("analysis", "2", "SELECT * FROM document WHERE system:tags[analysis].(state=1 AND (creationDate=YESTERDAY() OR creationDate=TODAY()))");

        // Update a tag via Tagging API.
        updateTag(objectId, "analysis", "3");

        // Generate a random string and use it as traceId for a 3rd tag.
        String traceId = String.format("%016x", new java.util.Random(System.currentTimeMillis()).nextLong());
        System.out.println(traceId);
        addTagWithTraceId(objectId, "tracingprocess", "1", traceId);

        // Update a tag via Tagging API with specified traceId.
        updateTagWithTraceId(objectId, "tracingprocess", "2", traceId);

        // Delete a tag via Tagging API with specified traceId.
        deleteTagWithTraceId(objectId, "tracingprocess", traceId);

        // Modify tags via PATCH metadata update endpoint.
        patchMetadataUpdate(objectId);

        // Show behavior of tags during content update.
        postContentUpdate(objectId);

        // Modify tags via POST metadata update endpoint.
        postMetadataUpdate(objectId);

        // Delete the example object.
        deleteObject(objectId);

    }

    public static String importExampleObject() throws Exception {
        String importResponseString = "";
        try {
            // Configure the import of a document object with binary content file.
            RequestBody importRequestBody = new MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data",
                            "metadata.json",
                            RequestBody.create(JSON, new File("./src/main/resources/metadata.json")))
                    .addFormDataPart("cid_63apple",
                            "test.txt",
                            RequestBody.create(PLAINTEXT, new File("./src/main/resources/test.txt")))
                    .build();
            Request importRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects")
                    .post(importRequestBody)
                    .build();

            // Execute the import and print the response returned by the called endpoint.
            Response importResponse = client.newCall(importRequest).execute();
            importResponseString = importResponse.body().string();
            System.out.println(importResponseString);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return importResponseString;
    }

    public static String parseObjectIdFromJsonResponse(String responseJson) {
        JSONObject jsonObject = new JSONObject(responseJson);
        // Extract the object ID from the 'responseJson' given as argument.
        return jsonObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }

    public static void addTag(String objectId, String tagName, String tagValue) throws Exception {
        try  {
            // Use the endpoint with traceIdMustMatch=false (default, not extra specified).
            // Configure the request to add a tag with name 'tagName' and state 'tagValue' to the object specified by 'objectId'.
            Request addTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/tags/" + tagName + "/state/" + tagValue)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            // Add the tag and print the response returned by the called endpoint.
            Response addTagResponse = client.newCall(addTagRequest).execute();
            String addTagResponseString = addTagResponse.body().string();
            System.out.println(addTagResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addTagWithTraceId(String objectId, String tagName, String tagValue, String traceId) throws Exception {
        try {
            // Set the value for the traceId in the header.
            // Configure the request to add a tag with name 'tagName' and state 'tagValue' to the object specified by 'objectId'.
            Request addTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("X-B3-TraceId", traceId)
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/tags/" + tagName + "/state/" + tagValue)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            // Add the tag and print the response returned by the called endpoint.
            Response addTagResponse = client.newCall(addTagRequest).execute();
            String addTagResponseString = addTagResponse.body().string();
            System.out.println(addTagResponseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void queryTag(String tagName, String newTagValue, String statement) throws Exception {
        try {
            // Configure a request to update the state of tag 'tagName' such that it has state 'newTagValue' afterwards, but only for the first found object matching the query 'statement'.
            Request queryTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/tags/" + tagName + "/state/" + newTagValue + "?query=" + statement)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            // Execute the configured call and print the response returned by the called endpoint.
            Response queryTagResponse = client.newCall(queryTagRequest).execute();
            String queryTagResponseString = queryTagResponse.body().string();
            System.out.println(queryTagResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getTags(String objectId) throws Exception {
        try {
            // Configure the request to retrieve the information on all tags assigned to the object specified by 'objectId'.
            Request getTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/tags")
                    .get().build();

            // Execute the configured call and print the response returned by the called endpoint.
            Response tagResponse = client.newCall(getTagRequest).execute();
            String tagResponseString = tagResponse.body().string();
            System.out.println(tagResponseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateTag(String objectId, String tagName, String newTagValue) throws Exception {
        try {
            // Configure a request to update the state of tag 'tagName' that is assigned to the object specified by 'objectId' such that it has state 'newTagValue' afterwards.
            Request updateTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/tags/" + tagName + "/state/" + newTagValue + "?overwrite=true")
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            // Execute the configured call and print the response returned by the called endpoint.
            Response updateTagResponse = client.newCall(updateTagRequest).execute();
            String updateTagResponseString = updateTagResponse.body().string();
            System.out.println(updateTagResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateTagWithTraceId(String objectId, String tagName, String newTagValue, String traceId) throws Exception {
        try {
            // Use the endpoint with traceIdMustMatch=true and set the value for the traceId in the header.
            // Configure a request to update the state of tag 'tagName' that is assigned to the object specified by 'objectId' such that it has state 'newTagValue' afterwards.
            Request updateTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("X-B3-TraceId", traceId)
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/tags/" + tagName + "/state/" + newTagValue + "?overwrite=true&traceIdMustMatch=true")
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            // Execute the configured call and print the response returned by the called endpoint.
            Response updateTagResponse = client.newCall(updateTagRequest).execute();
            String updateTagResponseString = updateTagResponse.body().string();
            System.out.println(updateTagResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteTagWithTraceId(String objectId, String tagName, String traceId) throws Exception {
        try {
            // Use the endpoint with traceIdMustMatch=true and set the value for the traceId in the header.
            // Configure a request for the deletion of the tag 'tagName' from the object specified by 'objectId'.
            Request deleteTagRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("X-B3-TraceId", traceId)
                    .url(baseUrl + "/api/dms/objects/"+objectId + "/tags/" + tagName + "?traceIdMustMatch=true")
                    .delete().build();

            // Execute the deletion.
            Response deleteTagResponse = client.newCall(deleteTagRequest).execute();

            // Print a message depending on the response returned by the tag deletion endpoint.
            if(deleteTagResponse.code() == 200) System.out.println("Tag '" + tagName + "' successfully deleted.");
            else System.out.println("Error while deleting: " + deleteTagResponse.code());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void patchMetadataUpdate(String objectId) throws Exception {
        try {
            //Configure a PATCH metadata update on the object specified by 'objectId'.
            Request patchMetadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId)
                    .patch(RequestBody.create(JSON, new File("./src/main/resources/metadataPatchTagging.json")))
                    .build();

            // Execute the update and print the response returned by the called endpoint.
            Response patchMetadataResponse = client.newCall(patchMetadataRequest).execute();
            String patchMetadataResponseString = patchMetadataResponse.body().string();
            System.out.println(patchMetadataResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postContentUpdate(String objectId) throws Exception {
        try {
            //Configure a content update on the object specified by 'objectId'.
            Request updateContentRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("Content-Disposition", "attachment; filename=\"test2.txt\"")
                    .url(baseUrl + "/api/dms/objects/" + objectId + "/contents/file")
                    .post(RequestBody.create(PLAINTEXT, new File("./src/main/resources/test2.txt")))
                    .build();

            // Execute the update and print the response returned by the called endpoint.
            Response updateContentResponse = client.newCall(updateContentRequest).execute();
            String updateContentResponseString = updateContentResponse.body().string();
            System.out.println(updateContentResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postMetadataUpdate(String objectId) throws Exception {
        try {
            //Configure a POST metadata update on the object specified by 'objectId'.
            Request updateMetadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/" + objectId)
                    .post(RequestBody.create(JSON, new File("./src/main/resources/metadataUpdate.json")))
                    .build();

            // Execute the update and print the response returned by the called endpoint.
            Response updateMetadataResponse = client.newCall(updateMetadataRequest).execute();
            String updateMetadataResponseString = updateMetadataResponse.body().string();
            System.out.println(updateMetadataResponseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteObject(String objectId) throws Exception {
        try {
            // Configure the deletion of the object specified by objectId.
            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "/api/dms/objects/"+objectId)
                    .delete().build();

            // Execute the deletion.
            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Object successfully deleted.");
            else System.out.println("Error while deleting: " + deleteResponse.code());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
