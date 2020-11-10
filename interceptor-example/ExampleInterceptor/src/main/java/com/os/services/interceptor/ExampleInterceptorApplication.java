
package com.os.services.interceptor;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleInterceptorApplication
{

    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(ExampleInterceptorApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
