package com.blank.app.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.blank.app.exception.BusinessException;
import com.blank.app.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${app.oss.endpoint}")
    private String endpoint;
    @Value("${app.oss.access-key-id}")
    private String accessKeyId;
    @Value("${app.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${app.oss.bucket-name}")
    private String bucketName;

    private OSS ossClient;

    private static final List<String> ALLOWED_MIMES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("OSS client initialized: bucket={}, endpoint={}", bucketName, endpoint);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) ossClient.shutdown();
    }

    @Override
    public String storeAvatar(MultipartFile file) {
        return store(file, "avatars", "avatar_");
    }

    @Override
    public String storePostcard(MultipartFile file) {
        return store(file, "postcards", "postcard_");
    }

    @Override
    public String storeStamp(MultipartFile file) {
        return store(file, "stamps", "stamp_");
    }

    @Override
    public String storeStampImageWithSeries(MultipartFile file, String seriesName) {
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String baseName = getBaseName(file.getOriginalFilename(), ext);
        String objectKey = "stamps/systemstamps/" + seriesName + "/" + baseName + ext;

        // Check if exists, add suffix to avoid overwrite
        if (ossClient.doesObjectExist(bucketName, objectKey)) {
            objectKey = "stamps/systemstamps/" + seriesName + "/" + baseName + "_" + System.currentTimeMillis() + ext;
        }

        try {
            ossClient.putObject(bucketName, objectKey, file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("文件上传OSS失败");
        }

        return "https://" + bucketName + "." + endpoint + "/" + objectKey;
    }

    private String store(MultipartFile file, String subDir, String prefix) {
        validateFile(file);
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        String baseName = getBaseName(originalName, ext);
        // Sanitize: keep only alphanumeric, Chinese, dash, underscore
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
        if (baseName.isEmpty()) baseName = "file";
        String filename = baseName + "_" + System.currentTimeMillis() + ext;
        String objectKey = subDir + "/" + filename;

        try {
            ossClient.putObject(bucketName, objectKey, file.getInputStream());
        } catch (IOException e) {
            log.error("OSS上传失败: objectKey={}", objectKey, e);
            throw new BusinessException("文件上传失败");
        }

        return "https://" + bucketName + "." + endpoint + "/" + objectKey;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("请上传图片");
        if (!ALLOWED_MIMES.contains(file.getContentType()))
            throw new BusinessException("只允许上传图片文件 (jpeg/png/gif/webp)");
    }

    private String getExtension(String filename) {
        if (filename == null) return ".png";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".png";
    }

    private String getBaseName(String filename, String ext) {
        if (filename == null) return "file";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }
}
