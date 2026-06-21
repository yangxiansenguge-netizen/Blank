package com.blank.app.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeAvatar(MultipartFile file);
    String storePostcard(MultipartFile file);
    String storeStamp(MultipartFile file);
    String storeStampImageWithSeries(MultipartFile file, String seriesName);
}
