package com.example.nodo_final.controller;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.ProductSearchReqDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.ProductResponseDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@RestController
@RequestMapping("api/products")
@Validated
public class ProductController {

    private final ProductService productService;
    private final MessageSource messageSource;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<?> createProduct(@Valid @ModelAttribute ProductRequestDTO productRequestDTO,
                                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         Locale locale) {
        return ResponseData.builder()
                .status(HttpStatus.CREATED.value())
                .message(messageSource.getMessage("product.create.success", null, locale))
                .data(productService.createProduct(productRequestDTO, files))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<?> updateProduct(@Min(value = 1, message = "{common.id.positive}") @PathVariable Long id,
                                         @Valid @ModelAttribute UpdateProductReqDTO dto,
                                         @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                         Locale locale) {
        ProductResponseDTO productResponseDTO = productService.updateProduct(id, dto, files);
        String message = messageSource.getMessage("product.update.success", null, locale);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(productResponseDTO)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseData<?> softDeleteProduct(@Min(value = 1, message = "{common.id.positive}") @PathVariable Long id, Locale locale) {
        productService.softDelete(id, locale);
        return ResponseData.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @GetMapping("/search")
    public ResponseData<?> getAllProducts(  @Valid @ModelAttribute ProductSearchReqDTO request,
                                            @Min(value = 0, message = "{common.page.min}")
                                            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                            @Min(value = 1, message = "{common.size.min}")
                                            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                            Locale locale) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Products retrieved successfully")
                .data(productService.getAllProducts(request, pageable, locale))
                .build();
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportProducts(@Valid @ModelAttribute ProductSearchReqDTO request) throws BadRequestException {
        String filename = "products.xlsx";

        // 1. Gọi Service (Service trả về "dòng chảy")
        StreamingResponseBody stream = productService.exportProducts(request);

        // 2. Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        // 3. Trả về
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream); // Trả về "dòng chảy"
    }
}
