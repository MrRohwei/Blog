package com.voidpen.server.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OpenApiConfigController {

    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> swaggerConfig(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String apiDocsUrl = contextPath + "/v3/api-docs";
        String configUrl = contextPath + "/v3/api-docs/swagger-config";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("configUrl", configUrl);
        response.put("url", apiDocsUrl);
        response.put("urls", List.of(Map.of("name", "default", "url", apiDocsUrl)));
        response.put("validatorUrl", "");
        return response;
    }
}
