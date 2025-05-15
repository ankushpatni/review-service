package com.zuzu.reviewservice.application.service;

import java.io.InputStream;
import java.util.List;

public interface S3Service {
    List<String> listFiles(String prefix);
    InputStream downloadFile(String key);
    boolean fileExists(String key);
} 