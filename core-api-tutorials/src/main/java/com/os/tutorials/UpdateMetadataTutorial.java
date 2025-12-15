package com.os.tutorials;

import com.os.tutorials.domain.DmsRequests;
import com.os.tutorials.domain.DmsResponses;
import com.os.tutorials.domain.Login;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class UpdateMetadataTutorial {

    public static final MediaType JPEG = MediaType.parse("image/jpeg; charset=utf-8");
    public static final String resourcesDirectory = "./src/main/resources/";

    //Resources for Initial Import and Update
    public static final String metadataFilename = "metadataRabbit.json";
    public static final String metadataUpdateFilename = "metadataRabbitUpdate.json";
    public static final String plainTextFilename = "1080px-Deilenaar.jpeg";


    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            //Initial Single Import of a plain text document
            Response singleImportResponse = client.newCall(DmsRequests.importSingleDocument(resourcesDirectory, metadataFilename, plainTextFilename, JPEG)).execute();
            String singleImportResponseString = singleImportResponse.body().string();
            System.out.println(singleImportResponseString);

            String objectId = DmsResponses.getObjectId(singleImportResponseString);
            System.out.println(objectId);

            //Retrieve and Display Metadata
            Response metadataResponse = client.newCall(DmsRequests.getMetadata(objectId)).execute();
            String metadataResponseString = metadataResponse.body().string();
            System.out.println(metadataResponseString);

            //Update Metadata
            Response updateResponse = client.newCall(DmsRequests.patchUpdateMetadata(objectId, resourcesDirectory, metadataUpdateFilename)).execute();
            String updateResponseString = updateResponse.body().string();
            System.out.println(updateResponseString);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
