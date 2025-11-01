package com.example.nodo_final.service;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.ResponseData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

public interface ProductService {

    ResponseData<?> createProduct(ProductRequestDTO dto, List<MultipartFile> files, Locale locale);

    ResponseData<?> updateProduct(Long id, UpdateProductReqDTO dto, List<MultipartFile> files, Locale locale);

    void softDelete(Long id, Locale locale);

}
