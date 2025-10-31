package com.example.nodo_final.service;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.response.ResponseData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

public interface CategoryService {

    ResponseData<?> createCategory(CategoryRequestDTO dto, List<MultipartFile> files, Locale locale);

    ResponseData<?> updateCategory(Long id, CategoryRequestDTO dto, List<MultipartFile> files, List<Long> deleteIds, Locale locale);

    ResponseData<?> softDelete(Long id, Locale locale);
}
