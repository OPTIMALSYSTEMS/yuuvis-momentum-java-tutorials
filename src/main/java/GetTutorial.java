import okhttp3.*;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class GetTutorial {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String username = "";
    public static final String userpassword = "";
    public static final String auth = "Basic "+Base64.getEncoder().encodeToString((username+":"+userpassword).getBytes());
    public static final String tenant = "";
    public static final String baseUrl = "";



    public static void main(String[] args) {
        String q1 = createGenericSearchQuery("Name", "LIKE", "E%");
        String q2 = createGenericSearchQuery("system:versionNumber", ">", "1");
        String q3 = createFulltextQuery("Europan");
        String q4 = createMultiAttributeQuery(new String[]{"Name", "system:versionNumber"}, new String[]{"LIKE", "="}, new String[]{"E%", "1"});
        String q5 = createRangeAttributeQuery("system:versionNumber", "1", "10");
        String q6 = createStringInListQuery("system:versionNumber", new String[]{"1", "2"});
        String q7 = createOrderedQuery("system:versionNumber", false);

        System.out.println(q1);
        System.out.println(q2);
        System.out.println(q3);
        System.out.println(q4);
        System.out.println(q5);
        System.out.println(q6);
        System.out.println(q7);

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
                            "indexData.json",
                            RequestBody.create(JSON,
                                    new File("./src/main/resources/indexData.json")))
                    .addFormDataPart("cid_63apple",
                            "test.txt",
                            RequestBody.create(PLAINTEXT,
                                    new File("./src/main/resources/test.txt")))
                    .build();
            Request importRequest = new Request.Builder()
                    .header("Authorization", "Basic " + auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects")
                    .post(importRequestBody)
                    .build();

            Response importResponse = client.newCall(importRequest).execute();
            String importResponseString = importResponse.body().string();
            System.out.println(importResponseString);

            //parse created ObjectId from Import Response Text
            objectId = parseObjectIdFromJsonResponse(importResponseString);

            //Update Metadata
            //read new metadata from file indexData2.json
            String updatedIndexDataString = "";
            String currentLine = "";
            BufferedReader bufferedReader = new BufferedReader(new FileReader("./src/main/resources/indexData2.json"));
            while ((currentLine = bufferedReader.readLine()) != null){
                updatedIndexDataString += "\n "+currentLine;
            }
            Request updateMetadataRequest = new Request.Builder()
                    //.header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/" + objectId)
                    .post(RequestBody.create(JSON, updatedIndexDataString))
                    .build();
            Response updateMetadataResponse = client.newCall(updateMetadataRequest).execute();

            //getting metadata of imported object
            Request metadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "objects/" + objectId)
                    .get().build();

            Response metadataResponse = client.newCall(metadataRequest).execute();
            String metadataResponseString = metadataResponse.body().string();
            System.out.println(metadataResponseString);

            //getting content of imported object
            Request contentRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "objects/" + objectId +"/contents/file")
                    .get().build();

            Response contentResponse = client.newCall(contentRequest).execute();
            String contentResponseString = contentResponse.body().string();
            System.out.println(contentResponseString);

            //getting specific version of metadata of imported object
            Request versionMetadataRequest = new Request.Builder()
                    .header("Authorization", auth)
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl+ "objects/" + objectId + "/versions/1")
                    .get().build();

            Response versionMetadataResponse = client.newCall(versionMetadataRequest).execute();
            String versionMetadataResponseString = versionMetadataResponse.body().string();
            System.out.println(versionMetadataResponseString);

            //search for name beginning with E
            Request attributeSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search/")
                    .post(RequestBody.create(JSON, q1))
                    .build();
            Response attributeSearchResponse = client.newCall(attributeSearchRequest).execute();
            System.out.println(attributeSearchResponse.body().string());

            //search for objects with version number > 2 #
            Request versionNumberSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q2))
                    .build();
            Response versionNumberSearchResponse = client.newCall(versionNumberSearchRequest).execute();
            System.out.println(versionNumberSearchResponse.body().string());

            //full text search for first word of imported content
            Request fulltextSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q3))
                    .build();
            Response fulltextSearchResponse = client.newCall(fulltextSearchRequest).execute();
            System.out.println(fulltextSearchResponse.body().string());


            //search for multiple parameters
            Request multiParamSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q4))
                    .build();
            Response multiParamSearchResponse = client.newCall(multiParamSearchRequest).execute();
            System.out.println(multiParamSearchResponse.body().string());

            Request rangeSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q5))
                    .build();
            Response rangeSearchResponse = client.newCall(rangeSearchRequest).execute();
            System.out.println(rangeSearchResponse.body().string());

            Request inListSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q6))
                    .build();

            Response inListSearchResponse = client.newCall(inListSearchRequest).execute();
            System.out.println(inListSearchResponse.body().string());

            Request orderedSearchRequest = new Request.Builder()
                    .header("X-ID-TENANT-NAME", tenant)
                    .url(baseUrl + "objects/search")
                    .post(RequestBody.create(JSON, q7))
                    .build();
            Response orderedSearchResponse = client.newCall(orderedSearchRequest).execute();
            System.out.println(orderedSearchResponse.body().string());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * creates simple CMIS query for a single attribute search
     *
     * @param attributeName
     * @param operator
     * @param value
     * @return query in JSON format
     */
    public static String createGenericSearchQuery(String attributeName, String operator, String value) {
        String result = "";
        try {
            String statement = "SELECT * FROM system:object WHERE " + attributeName + " " + operator + " '" + value + "'";
            return createQueryJSON(statement, 0, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * creates simple CMIS query for a full-text search
     * @param text
     * @return query in JSON format
     */
    public static String createFulltextQuery(String text) {
        String result = "";
        try {
            String statement = "SELECT * FROM system:object WHERE CONTAINS('" + text + "')";
            return createQueryJSON(statement, 0, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    ;

    /**
     * creates JSON from query parameters
     * @param statement
     * @param skipCount
     * @param maxItems
     * @return
     */
    public static String createQueryJSON(String statement, int skipCount, int maxItems) {
        JSONObject queryObject = new JSONObject();
        JSONObject queryAttributes = new JSONObject();
        queryAttributes.put("statement", statement);
        queryAttributes.put("skipCount", skipCount);
        queryAttributes.put("maxItems", maxItems);
        queryObject.put("query", queryAttributes);

        return queryObject.toString();
    }

    /**
     * creates CMIS query for searching multiple parameters
     * @param attributes
     * @param operators
     * @param values
     * @return
     */
    public static String createMultiAttributeQuery(String[] attributes, String[] operators, String[] values) {
        if (attributes.length == operators.length & operators.length == values.length) {
            //create conditions for each attribute
            String[] attributeConditions = new String[attributes.length];
            for (int i = 0; i < attributeConditions.length; i++) {
                String currentAttributeCondition = attributes[i] + " " + operators[i];
                currentAttributeCondition += " '" + values[i] + "'";
                attributeConditions[i] = currentAttributeCondition;
            }
            //create statement out of attribute conditions
            String statement = "SELECT * FROM system:object WHERE " + attributeConditions[0];
            for (int i = 1; i < attributeConditions.length; i++) {
                statement += " AND " + attributeConditions[i];
            }
            return createQueryJSON(statement, 0, 50);
        } else {
            System.out.println("input arrays don't have matching length");
            return "";
        }
    }

    public static String createRangeAttributeQuery(String attribute, String rangeStart, String rangeEnd){
        String statement = "SELECT * FROM system:object WHERE ("+attribute+">"+rangeStart+" AND "+attribute+"<"+rangeEnd+");";
        return createQueryJSON(statement,0, 50);
    }

    public static String createStringInListQuery(String attribute, String[] values){
        String valueString = "";
        for(int i = 0; i < values.length; i++){
            valueString += "'" + values[i] + "'";
            if(i != values.length - 1){
                valueString += ", ";
            }
        }
        String statement = "SELECT * FROM system:object WHERE "+attribute+ " IN ("+valueString+");";
        return createQueryJSON(statement, 0, 50);
    }

    public static String createOrderedQuery(String orderAttribute, boolean orderAsc){
        String orderOrder = "";
        if(orderAsc) orderOrder = "ASC";
        else orderOrder = "DESC";

        String statement = "SELECT * FROM system:object ORDER BY "+orderAttribute+ " "+orderOrder+";";
        return createQueryJSON(statement,0 ,50);
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
