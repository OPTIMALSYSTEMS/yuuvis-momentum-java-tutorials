package com.os.tutorials;

import okhttp3.*;

public class ImportTutorial {

    public static final MediaType PLAINTEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String resourcesDirectory = "./src/main/resources/";
    public static final String metadataFilename = "metadata.json";
    public static final String plainTextFilename = "schneiderlein.txt";



    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            //Single Import
            Response singleImportResponse = client.newCall(DmsRequests.importPlainText(resourcesDirectory, metadataFilename, plainTextFilename, PLAINTEXT)).execute();
            String singleImportResponseString = singleImportResponse.body().string();
            //System.out.println(singleImportResponse.body().string());

            String contentStreamId = DmsResponses.getContentStreamId(singleImportResponseString);
            System.out.println(contentStreamId);
            String repositoryId = DmsResponses.getRepositoryId(singleImportResponseString);
            System.out.println(repositoryId);
            String archivePath = DmsResponses.getArchivePath(singleImportResponseString);
            System.out.println(archivePath);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
