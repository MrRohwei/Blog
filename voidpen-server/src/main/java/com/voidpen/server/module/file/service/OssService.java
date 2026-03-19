package com.voidpen.server.module.file.service;

import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OssService {

    @Value("${voidpen.oss.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Value("${voidpen.oss.local-dir:uploads}")
    private String localDir;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String ext = resolveExtension(file.getOriginalFilename());
        String dateDir = LocalDate.now().toString().replace("-", "/");
        String objectName = "voidpen/" + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        Path targetPath = Paths.get(localDir, objectName.split("/")).normalize();
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            return normalizeBaseUrl(baseUrl) + "/" + objectName;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String resolveExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return ".bin";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return ".bin";
        }
        return fileName.substring(lastDotIndex);
    }

    private String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "http://localhost:8080/uploads";
        }
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
