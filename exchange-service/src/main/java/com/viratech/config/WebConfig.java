package com.viratech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public void addCorsMappings(CorsRegistry registry){

      /*  String[] arrayRoutes = {
                "http://localhost:3000",
                "http://my-frontend.com"
        };*/

        registry.addMapping("/**")
                //.allowedOrigins(arrayRoutes)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
