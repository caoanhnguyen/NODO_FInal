package com.example.nodo_final.service;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.ProductSearchReqDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.PageResponse;
import com.example.nodo_final.dto.response.ResponseData;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;

public interface ProductService {

    Long createProduct(ProductRequestDTO dto, List<MultipartFile> files);

    ResponseData<?> updateProduct(Long id, UpdateProductReqDTO dto, List<MultipartFile> files, Locale locale);

    void softDelete(Long id, Locale locale);

    PageResponse<?> getAllProducts(ProductSearchReqDTO request, Pageable pageable, Locale locale);

    StreamingResponseBody exportProducts(ProductSearchReqDTO request) throws BadRequestException;

}
