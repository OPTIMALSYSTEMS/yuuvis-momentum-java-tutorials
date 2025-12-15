package com.os.tutorials;

import com.os.tutorials.domain.DmsRequests;
import com.os.tutorials.domain.DmsResponses;
import com.os.tutorials.domain.Login;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class GetTutorial {

    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType JPEG = MediaType.parse("image/jpeg; charset=utf-8");
    public static final String resourcesDirectory = "./src/main/resources/";

    //Resources for Initial Import
    public static final String metadataFilename = "metadata.json";
    public static final String plainTextFilename = "geislein.txt";


    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            //Initial Single Import of a plain text document
            Response singleImportResponse = client.newCall(DmsRequests.importSingleDocument(resourcesDirectory, metadataFilename, plainTextFilename, PLAINTEXT)).execute();
            String singleImportResponseString = singleImportResponse.body().string();
            System.out.println(singleImportResponseString);

            String objectId = DmsResponses.getObjectId(singleImportResponseString);
            System.out.println(objectId);

            //Retrieve and Display Metadata
            Response metadataResponse = client.newCall(DmsRequests.getMetadata(objectId)).execute();
            String metadataResponseString = metadataResponse.body().string();
            System.out.println(metadataResponseString);

            //Retrieve and store Binary Content (as TXT file in this example)
            Response contentResponse = client.newCall(DmsRequests.getContent(objectId)).execute();
            DmsResponses.saveTxtFile(contentResponse);
            System.out.println("Successfully downloaded the binary content file. Please check your project's target directory.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
