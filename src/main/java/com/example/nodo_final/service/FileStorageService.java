package com.example.nodo_final.service;

import com.example.nodo_final.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void init();
    Resource save(MultipartFile file);
}
