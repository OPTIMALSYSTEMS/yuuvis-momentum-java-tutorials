import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class CompoundTutorial {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType OCTETSTREAM = MediaType.parse("application/octetstream; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";

    public static CookieJar cookieJar = new JavaNetCookieJar(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    public static OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar).build();


    public static void main(String[] args) {
        createImportCompoundDocument();
    }

    public static void createImportCompoundDocument() {
        try {
            byte[] document1BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test.txt"));
            byte[] document2BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test1.txt"));
            byte[] document3BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test2.txt"));

            File compoundFile = File.createTempFile("compound", ".bin");
            OutputStream bos = new BufferedOutputStream(new FileOutputStream(compoundFile));

            String[] ranges = new String[3];
            String[] SubNames = new String[3];

            //write bytestreams into binary compound file
            long offset = 0;
            long document1BAlength = document1BA.length;
            String range1 = offset + "-" + (offset + document1BAlength - 1);
            bos.write(document1BA);
            ranges[0] = range1;
            SubNames[0] = "test.txt";

            offset += document1BAlength;
            long document2BAlength = document2BA.length;
            String range2 = offset + "-" + (offset + document2BAlength - 1);
            bos.write(document2BA);
            ranges[1] = range2;
            SubNames[1] = "test1.txt";

            offset += document2BAlength;
            long document3BAlength = document3BA.length;
            String range3 = (offset) + "-" + (offset + document3BAlength - 1);
            bos.write(document3BA);
            ranges[2] = range3;
            SubNames[2] = "test2.txt";

            IOUtils.closeQuietly(bos);

            //import compound document and 3 sub-documents
            System.out.println("Attempting import of compound document and 3 sub-documents.");
            String compoundImportJsonString = createCompoundImportMetadataObject("testCompound", "compound.bin", "cid_63apple", SubNames, ranges).toString();
            System.out.println(compoundImportJsonString);

            RequestBody compoundImportRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metadata.json", RequestBody.create(JSON, compoundImportJsonString))
                    .addFormDataPart("cid_63apple", "compound.bin", RequestBody.create(OCTETSTREAM, compoundFile))
                    .build();

            Request compoundImportRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(compoundImportRequestBody)
                    .build();

            System.out.println(compoundImportRequest);
            Response compoundImportResponse = client.newCall(compoundImportRequest).execute();
            String compoundImportResponseString = compoundImportResponse.body().string();
            String contentStreamId = extractContentStreamIdFromResponse(compoundImportResponseString);
            String repositoryId = extractRepositoryIdFromResponse(compoundImportResponseString);
            String archivePath = extractArchivePathFromResponse(compoundImportResponseString);
            String compoundObjectId = extractFirstObjectIdFromResponse(compoundImportResponseString);
            String[] subDocumentObjectIds1 = extractSubDocumentObjectIdsFromResponse(compoundImportResponseString);

            System.out.println(compoundImportResponseString);

            TimeUnit.SECONDS.sleep(5);
            //retrieve content of the sub-documents
            for (String objectId : subDocumentObjectIds1) {
                System.out.println(objectId);
                Request getContentOfSubDocumentRequest = new Request.Builder()
                        .header("Authorization", auth)
                        .header("X-ID-TENANT-NAME", tenant)
                        .url(baseUrl + "objects/" + objectId + "/contents/file")
                        .get().build();
                Response getContentOfSubDocumentResponse = client.newCall(getContentOfSubDocumentRequest).execute();
                System.out.println(getContentOfSubDocumentResponse.body().string());
            }

            //subsequently, import one more sub-document using the contentStreamId, repositoryId and archivePath of the compound document
            System.out.println("Attempting subsequent import a sub-document.");
            String postSubDocumentImportJsonString = createSubImportMetadataObject(contentStreamId, repositoryId, archivePath, new String[]{"sub-concatenation.txt"}, new String[]{"1159-1365,0-45"}).toString();
            System.out.println(postSubDocumentImportJsonString);

            RequestBody postSubDocumentImportRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metadata.json", RequestBody.create(JSON, postSubDocumentImportJsonString))
                    .build();

            Request postSubDocumentImportRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(postSubDocumentImportRequestBody)
                    .build();

            Response postSubDocumentImportResponse = client.newCall(postSubDocumentImportRequest).execute();
            String postSubDocumentImportResponseString = postSubDocumentImportResponse.body().string();
            String[] subDocumentObjectIds2 = extractSubDocumentObjectIdsFromResponse(postSubDocumentImportResponseString);

            TimeUnit.SECONDS.sleep(5);
            //retrieve content of the new sub-document
            String objectId = subDocumentObjectIds2[0];
            System.out.println(objectId);

            Request getContentOfSubDocumentRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/" + objectId + "/contents/file")
                    .get().build();

            Response getContentOfSubDocumentResponse = client.newCall(getContentOfSubDocumentRequest).execute();
            System.out.println(getContentOfSubDocumentResponse.body().string());

            TimeUnit.SECONDS.sleep(5);
            //delete the created objects
            deleteObject(subDocumentObjectIds2[0]);
            deleteSubdocuments(subDocumentObjectIds1);
            deleteObject(compoundObjectId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject createCompoundImportMetadataObject(String name, String path, String cid, String[] SubNames, String[] ranges) {
        if (SubNames.length == ranges.length) {
            JSONObject root = new JSONObject();
            JSONArray objects = new JSONArray();
            objects.put(createCompoundDocumentMetadataObject(name, path, cid));
            for (int i = 0; i < SubNames.length; i++) {
                objects.put(createSubDocumentMetadataObject(SubNames[i], path, cid, ranges[i]));
            }
            root.put("objects", objects);
            return root;
        } else {
            System.out.println("amount of ranges and names for sub-documents do not match!");
            return null;
        }
    }

    public static JSONObject createSubImportMetadataObject(String contentStreamId, String repo, String archivePath, String[] SubNames, String[] ranges) {
        if (SubNames.length == ranges.length) {
            JSONObject root = new JSONObject();
            JSONArray objects = new JSONArray();
            for (int i = 0; i < SubNames.length; i++) {
                objects.put(createPostSubDocumentMetadataObject(SubNames[i], contentStreamId, repo, archivePath, ranges[i]));
            }
            root.put("objects", objects);
            return root;
        } else {
            System.out.println("amount of ranges and names for sub-documents do not match!");
            return null;
        }
    }

    public static JSONObject createCompoundDocumentMetadataObject(String name, String path, String cid) {
        JSONObject compoundDocument = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("system:objectTypeId", new JSONObject().put("value", "document"));
        properties.put("name", new JSONObject().put("value", name));
        JSONObject contentStream = new JSONObject();
        contentStream.put("mimeType", "application/octet-stream");
        contentStream.put("fileName", path);
        contentStream.put("cid", cid);
        JSONArray contentStreams = new JSONArray();
        contentStreams.put(contentStream);
        compoundDocument.put("properties", properties);
        compoundDocument.put("contentStreams", contentStreams);

        return compoundDocument;
    }

    public static JSONObject createSubDocumentMetadataObject(String name, String path, String cid, String range) {
        JSONObject SubDocument = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("system:objectTypeId", new JSONObject().put("value", "document"));
        properties.put("name", new JSONObject().put("value", name));
        JSONObject contentStream = new JSONObject();
        contentStream.put("mimeType", "text/plain");
        contentStream.put("fileName", path);
        contentStream.put("cid", cid);
        contentStream.put("range", range);
        JSONArray contentStreams = new JSONArray();
        contentStreams.put(contentStream);
        SubDocument.put("properties", properties);
        SubDocument.put("contentStreams", contentStreams);

        return SubDocument;
    }

    public static JSONObject createPostSubDocumentMetadataObject(String name, String contentStreamId, String repo, String archivePath, String range) {
        JSONObject SubDocument = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("system:objectTypeId", new JSONObject().put("value", "document"));
        properties.put("name", new JSONObject().put("value", name));
        JSONObject contentStream = new JSONObject();
        contentStream.put("mimeType", "text/plain");
        contentStream.put("contentStreamId", contentStreamId);
        contentStream.put("repositoryId", repo);
        contentStream.put("archivePath", archivePath);
        contentStream.put("range", range);
        JSONArray contentStreams = new JSONArray();
        contentStreams.put(contentStream);
        SubDocument.put("properties", properties);
        SubDocument.put("contentStreams", contentStreams);

        return SubDocument;
    }

    public static String extractContentStreamIdFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("contentStreamId");
    }

    public static String extractRepositoryIdFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("repositoryId");
    }

    public static String extractArchivePathFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("archivePath");
    }

    public static String extractFirstObjectIdFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("system:objectId")
                .getString("value");
    }

    public static String[] extractSubDocumentObjectIdsFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        JSONArray objects = responseJSONObject.getJSONArray("objects");
        List<String> objectIds = new ArrayList<>();
        for (int i = 0; i < objects.length(); i++) {
            JSONObject current = objects.getJSONObject(i);
            if (current.getJSONArray("contentStreams").getJSONObject(0).has("range")) {
                objectIds.add(current.getJSONObject("properties").getJSONObject("system:objectId").getString("value"));
            }
        }
        return objectIds.toArray(new String[objectIds.size()]);
    }

    public static void deleteSubdocuments(String[] objectIds) {
        try {
            JSONObject root = new JSONObject();
            JSONArray objects = new JSONArray();
            for (int i = 0; i < objectIds.length; i++) {
                objects.put(
                        new JSONObject()
                                .put("properties", new JSONObject()
                                        .put("system:objectId", new JSONObject()
                                                .put("value", objectIds[i]))));
            }
            root.put("objects", objects);
            System.out.println("Attempting deletion of all following objects: "+root);

            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .header("Content-Type", "application/json")
                    .url(baseUrl + "objects")
                    .delete(RequestBody.create(JSON, String.valueOf(root))).build();

            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Objects successfully deleted.");
            else System.out.println("Error while deleting: " + deleteResponse.code() + deleteResponse.body().string());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void deleteObject(String objectId) {
        try {
            System.out.println("Attempting deletion of single object with ID "+objectId);

            Request deleteRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/"+objectId)
                    .delete().build();

            Response deleteResponse = client.newCall(deleteRequest).execute();

            if(deleteResponse.code() == 200) System.out.println("Object successfully deleted.");
            else System.out.println("Error while deleting: " + deleteResponse.code() + deleteResponse.body().string());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
