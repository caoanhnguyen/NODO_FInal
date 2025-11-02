package com.example.nodo_final.service;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.response.PageResponse;
import com.example.nodo_final.dto.response.ResponseData;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;

public interface CategoryService {

    ResponseData<?> createCategory(CategoryRequestDTO dto, List<MultipartFile> files, Locale locale);

    PageResponse<?> getAllCategories(CategorySearchReqDTO request, Pageable pageable, Locale locale);

    ResponseData<?> updateCategory(Long id, CategoryRequestDTO dto, List<MultipartFile> files, List<Long> deleteIds, Locale locale);

    ResponseData<?> softDelete(Long id, Locale locale);

    StreamingResponseBody exportCategories(CategorySearchReqDTO request);
}
