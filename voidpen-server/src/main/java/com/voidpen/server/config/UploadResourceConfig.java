package com.voidpen.server.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Value("${voidpen.oss.local-dir:uploads}")
    private String localDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteDir = new File(localDir).getAbsolutePath().replace("\\", "/");
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + absoluteDir + "/");
    }
}
