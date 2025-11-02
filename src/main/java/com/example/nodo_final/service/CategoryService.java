package com.example.nodo_final.service;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.request.UpdateCategoryReqDTO;
import com.example.nodo_final.dto.response.CategoryDetailResDTO;
import com.example.nodo_final.dto.response.PageResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Locale;

public interface CategoryService {

    Long createCategory(CategoryRequestDTO dto, List<MultipartFile> files);

    PageResponse<?> getAllCategories(CategorySearchReqDTO request, Pageable pageable, Locale locale) throws BadRequestException;

    CategoryDetailResDTO updateCategory(Long id, UpdateCategoryReqDTO dto, List<MultipartFile> files, List<Long> deleteIds);

    void softDelete(Long id, Locale locale);

    StreamingResponseBody exportCategories(CategorySearchReqDTO request) throws BadRequestException;
}
