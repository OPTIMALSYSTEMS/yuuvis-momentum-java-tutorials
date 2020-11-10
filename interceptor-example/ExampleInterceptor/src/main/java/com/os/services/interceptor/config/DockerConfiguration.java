package com.os.services.interceptor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
public class DockerConfiguration {

	// This property is used to check if the configservice is available
    // If not present an exception will be thrown and the service restarts 
    @Value("${docker.enabled}")
    private boolean dockerEnabled;
}
