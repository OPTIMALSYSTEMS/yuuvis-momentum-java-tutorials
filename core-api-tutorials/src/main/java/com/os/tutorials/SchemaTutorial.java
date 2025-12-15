package com.os.tutorials;

import com.os.tutorials.domain.Login;
import com.os.tutorials.domain.ResourceRequests;
import com.os.tutorials.domain.ResourceResponses;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class SchemaTutorial {
    public static final String schemaResourceFile = "./src/main/resources/schemaAppPetshop.xml";
    public static final String appName= "petshop";

    public static void main(String[] args) {
        try {
            OkHttpClient client = Login.buildClient();

            //get applied schema
            Response activeSchemaResponse = client.newCall(ResourceRequests.getAppliedSchema()).execute();
            ResourceResponses.saveXmlFile(activeSchemaResponse);
            System.out.println("Successfully downloaded the applied schema. Please check your project's target directory.");

            //validate app schema
            Response validationResponse = client.newCall(ResourceRequests.validateAppSchema(appName, schemaResourceFile)).execute();
            System.out.println(validationResponse.code());
            String validationResponseAsString = validationResponse.body().string();
            System.out.println(validationResponseAsString);

            //import app schema
            Response importResponse = client.newCall(ResourceRequests.importAppSchema(appName, schemaResourceFile)).execute();
            System.out.println(importResponse.code());
            String importResponseAsString = importResponse.body().string();
            System.out.println(importResponseAsString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
