import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public static OkHttpClient.Builder builder = new OkHttpClient.Builder();
    public static OkHttpClient client = builder.build();


    public static void main(String[] args) {
        createImportCompoundDocument();
    }

    /**
     * creates and imports compound document from 3 text files
     */
    public static void createImportCompoundDocument() {
        try {
            byte[] document1BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test.txt"));
            byte[] document2BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test1.txt"));
            byte[] document3BA = FileUtils.readFileToByteArray(new File("./src/main/resources/test2.txt"));

            File compoundFile = File.createTempFile("compound", ".bin");
            OutputStream bos = new BufferedOutputStream(new FileOutputStream(compoundFile));

            String[] ranges = new String[3];
            String[] partialNames = new String[3];

            //write partial document bytestreams into binary compound file
            long offset = 0;
            long document1BAlength = document1BA.length;
            String range1 = offset + "-" + (offset + document1BAlength - 1);
            bos.write(document1BA);
            ranges[0] = range1;
            partialNames[0] = "test.txt";

            offset += document1BAlength;
            long document2BAlength = document2BA.length;
            String range2 = offset + "-" + (offset + document2BAlength - 1);
            bos.write(document2BA);
            ranges[1] = range2;
            partialNames[1] = "test1.txt";

            offset += document2BAlength;
            long document3BAlength = document3BA.length;
            String range3 = (offset) + "-" + (offset + document3BAlength - 1);
            bos.write(document3BA);
            ranges[2] = range3;
            partialNames[2] = "test2.txt";

            IOUtils.closeQuietly(bos);

            //import compound document and 3/3 partial documents
            System.out.println("Attempting import of complete compound document");
            String compoundImportJsonString = createCompoundImportMetadataObject("testCompound", "./compound.bin", "cid_63apple", partialNames, ranges).toString();
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

            Response compoundImportResponse = client.newCall(compoundImportRequest).execute();
            String compoundImportResponseString = compoundImportResponse.body().string();
            String contentStreamId = extractContentStreamIdFromResponse(compoundImportResponseString);
            String repositoryId = extractRepositoryIdFromResponse(compoundImportResponseString);
            String[] partialDocumentObjectIds = extractPartialDocumentObjectIdsFromResponse(compoundImportResponseString);

//            System.out.println(compoundImportResponseString);
//            System.out.println(contentStreamId);
//            System.out.println(repositoryId);

            TimeUnit.SECONDS.sleep(5);
            //retrieve text of individual documents
            for (String objectId : partialDocumentObjectIds) {
                System.out.println(objectId);
                Request getContentOfPartialDocumentRequest = new Request.Builder()
                        .header("Authorization", auth)
                        .header("X-ID-TENANT-NAME", tenant)
                        .url(baseUrl + "objects/" + objectId + "/contents/file")
                        .get().build();
                Response getContentOfPartialDocumentResponse = client.newCall(getContentOfPartialDocumentRequest).execute();
                System.out.println(getContentOfPartialDocumentResponse.body().string());
            }

            //import again, this time with 2/3 partial documents contained in the metadata on import
            System.out.println("Attempting import of compound document without all partial documents");
            String incompleteCompoundImportString = createCompoundImportMetadataObject("testCompound2", "./compound.bin", "cid_63apple", new String[]{partialNames[0], partialNames[1]}, new String[]{ranges[0], ranges[1]}).toString();

            RequestBody partialCompoundImportRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metadata.json", RequestBody.create(JSON, incompleteCompoundImportString))
                    .addFormDataPart("cid_63apple", "compound.bin", RequestBody.create(OCTETSTREAM, compoundFile))
                    .build();

            Request partialCompoundImportRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(partialCompoundImportRequestBody)
                    .build();

            Response partialCompoundImportResponse = client.newCall(partialCompoundImportRequest).execute();
            String partialCompoundImportResponseString = partialCompoundImportResponse.body().string();
            String contentStreamId2 = extractContentStreamIdFromResponse(partialCompoundImportResponseString);
            String repositoryId2 = extractRepositoryIdFromResponse(partialCompoundImportResponseString);
            String[] partialDocumentObjectIds2 = extractPartialDocumentObjectIdsFromResponse(partialCompoundImportResponseString);

            TimeUnit.SECONDS.sleep(5);
            //retrieve text of individual documents
            for (String objectId : partialDocumentObjectIds2) {
                System.out.println(objectId);
                Request getContentOfPartialDocumentRequest = new Request.Builder()
                        .header("Authorization", auth)
                        .header("X-ID-TENANT-NAME", tenant)
                        .url(baseUrl + "objects/" + objectId + "/contents/file")
                        .get().build();
                Response getContentOfPartialDocumentResponse = client.newCall(getContentOfPartialDocumentRequest).execute();
                System.out.println(getContentOfPartialDocumentResponse.body().string());
            }

            //finally, import the last partial document using the contentStreamId of the compound document
            System.out.println("Attempting import of remaining partial document");
            String postPartialDocumentImportJsonString = createPartialImportMetadataObject(contentStreamId2, repositoryId2, new String[]{partialNames[2]}, new String[]{ranges[2]}).toString();
            System.out.println(postPartialDocumentImportJsonString);

            RequestBody postPartialDocumentImportRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", "metadata.json", RequestBody.create(JSON, postPartialDocumentImportJsonString))
                    .build();

            Request postPartialDocumentImportRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(postPartialDocumentImportRequestBody)
                    .build();

            Response postPartialDocumentImportResponse = client.newCall(postPartialDocumentImportRequest).execute();
            String postPartialDocumentImportResponseString = postPartialDocumentImportResponse.body().string();
            String[] partialDocumentObjectIds3 = extractPartialDocumentObjectIdsFromResponse(postPartialDocumentImportResponseString);

            TimeUnit.SECONDS.sleep(5);
            //retrieve text of individual documents
            for (String objectId : partialDocumentObjectIds3) {
                System.out.println(objectId);
                Request getContentOfPartialDocumentRequest = new Request.Builder()
                        .header("Authorization", auth)
                        .header("X-ID-TENANT-NAME", tenant)
                        .url(baseUrl + "objects/" + objectId + "/contents/file")
                        .get().build();
                Response getContentOfPartialDocumentResponse = client.newCall(getContentOfPartialDocumentRequest).execute();
                System.out.println(getContentOfPartialDocumentResponse.body().string());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject createCompoundImportMetadataObject(String name, String path, String cid, String[] partialNames, String[] ranges) {
        if (partialNames.length == ranges.length) {
            JSONObject root = new JSONObject();
            JSONArray objects = new JSONArray();
            objects.put(createCompoundDocumentMetadataObject(name, path, cid));
            for (int i = 0; i < partialNames.length; i++) {
                objects.put(createPartialDocumentMetadataObject(partialNames[i], path, cid, ranges[i]));
            }
            root.put("objects", objects);
            return root;
        } else {
            System.out.println("amount of ranges and names for partial documents do not match!");
            return null;
        }
    }

    public static JSONObject createPartialImportMetadataObject(String contentStreamId, String repo, String[] partialNames, String[] ranges) {
        if (partialNames.length == ranges.length) {
            JSONObject root = new JSONObject();
            JSONArray objects = new JSONArray();
            for (int i = 0; i < partialNames.length; i++) {
                objects.put(createPostPartialDocumentMetadataObject(partialNames[i], contentStreamId, repo, ranges[i]));
            }
            root.put("objects", objects);
            return root;
        } else {
            System.out.println("amount of ranges and names for partial documents do not match!");
            return null;
        }
    }

    ;

    /**
     * creates the metadata object for a compound document
     *
     * @param name
     * @param path
     * @param cid
     * @return
     */
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

    /**
     * creates the metadata object for a single partial document for importing alongside the compound document
     *
     * @param name
     * @param path
     * @param cid
     * @param range
     * @return
     */
    public static JSONObject createPartialDocumentMetadataObject(String name, String path, String cid, String range) {
        JSONObject partialDocument = new JSONObject();
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
        partialDocument.put("properties", properties);
        partialDocument.put("contentStreams", contentStreams);

        return partialDocument;
    }

    /**
     * creates the metadata object for a partial document for importing after the initial import of the corresponding compound document
     *
     * @param name
     * @param contentStreamId
     * @param repo
     * @param range
     * @return
     */
    public static JSONObject createPostPartialDocumentMetadataObject(String name, String contentStreamId, String repo, String range) {
        JSONObject partialDocument = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("system:objectTypeId", new JSONObject().put("value", "document"));
        properties.put("name", new JSONObject().put("value", name));
        JSONObject contentStream = new JSONObject();
        contentStream.put("mimeType", "text/plain");
        contentStream.put("contentStreamId", contentStreamId);
        contentStream.put("repositoryId", repo);
        contentStream.put("range", range);
        JSONArray contentStreams = new JSONArray();
        contentStreams.put(contentStream);
        partialDocument.put("properties", properties);
        partialDocument.put("contentStreams", contentStreams);

        return partialDocument;
    }

    /**
     * extracts contentstreamId from a response
     *
     * @param responseJson
     * @return
     */
    public static String extractContentStreamIdFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("contentStreamId");
    }

    /**
     * extracts repositoryId from a response
     *
     * @param responseJson
     * @return
     */
    public static String extractRepositoryIdFromResponse(String responseJson) {
        JSONObject responseJSONObject = new JSONObject(responseJson);
        return responseJSONObject.getJSONArray("objects")
                .getJSONObject(0)
                .getJSONArray("contentStreams")
                .getJSONObject(0)
                .getString("repositoryId");
    }

    /**
     * extracts the objectIds of partial documents (meaning objects with contentstreams containing at least one range) from a response
     *
     * @param responseJson
     * @return
     */
    public static String[] extractPartialDocumentObjectIdsFromResponse(String responseJson) {
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

}


