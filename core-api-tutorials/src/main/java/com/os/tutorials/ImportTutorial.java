package com.os.tutorials;

import com.os.tutorials.domain.DmsRequests;
import com.os.tutorials.domain.DmsResponses;
import com.os.tutorials.domain.Login;
import okhttp3.*;

public class ImportTutorial {

    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType JPEG = MediaType.parse("image/jpeg; charset=utf-8");
    public static final String resourcesDirectory = "./src/main/resources/";

    //Resources for Single Import
    public static final String metadataFilename = "metadata.json";
    public static final String plainTextFilename = "geislein.txt";

    //Resources for Batch Import
    public static final String metadataBatchFilename = "metadataBatch.json";
    public static final String contentFilename1 = "schneiderlein.txt";
    public static final String contentFilename2 = "960px-Offterdinger_Das_tapfere_Schneiderlein.jpeg";





    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            //Single Import
            Response singleImportResponse = client.newCall(DmsRequests.importSingleDocument(resourcesDirectory, metadataFilename, plainTextFilename, PLAINTEXT)).execute();
            String singleImportResponseString = singleImportResponse.body().string();
            System.out.println(singleImportResponseString);

            String objectId = DmsResponses.getObjectId(singleImportResponseString);
            System.out.println(objectId);

            //Batch Import
            Response batchImportResponse = client.newCall(DmsRequests.batchImportTextAndImage(resourcesDirectory, metadataBatchFilename, contentFilename1, contentFilename2)).execute();
            String batchImportResponseString = batchImportResponse.body().string();
            System.out.println(batchImportResponseString);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
