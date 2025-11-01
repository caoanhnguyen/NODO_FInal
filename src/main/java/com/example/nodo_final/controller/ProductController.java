package com.example.nodo_final.controller;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@RestController
@RequestMapping("api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<?> createProduct(@Valid @ModelAttribute ProductRequestDTO productRequestDTO,
                                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         Locale locale) {
        return productService.createProduct(productRequestDTO, files, locale);
    }

    @PutMapping("/{id}")
    public ResponseData<?> updateProduct(@PathVariable Long id,
                                         @Valid @ModelAttribute UpdateProductReqDTO dto,
                                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         Locale locale) {
        return productService.updateProduct(id, dto, files, locale);
    }

    @DeleteMapping("/{id}")
    public ResponseData<?> softDeleteProduct(@PathVariable Long id, Locale locale) {
        productService.softDelete(id, locale);
        return ResponseData.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }
}
