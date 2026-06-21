package com.blank.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}")
    private String corsOrigins;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            String resPath = new File("res").getCanonicalPath().replace("\\", "/") + "/";
            registry.addResourceHandler("/res/**").addResourceLocations("file:" + resPath);
            String uploadPath = new File("uploads").getCanonicalPath().replace("\\", "/") + "/";
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath);
        } catch (IOException e) {
            registry.addResourceHandler("/res/**").addResourceLocations("file:res/");
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String origins = corsOrigins;
        if (origins == null || origins.isEmpty()) {
            origins = "http://localhost:3000,http://127.0.0.1:3000,http://localhost:3004,http://127.0.0.1:3004,http://localhost:5173,http://127.0.0.1:5173";
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(origins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
