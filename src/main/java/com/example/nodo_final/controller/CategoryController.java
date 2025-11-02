package com.example.nodo_final.controller;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.request.UpdateCategoryReqDTO;
import com.example.nodo_final.dto.response.CategoryDetailResDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
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

import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<?> createCategory(@Valid @ModelAttribute CategoryRequestDTO categoryRequestDTO,
                                          @RequestParam(value = "files", required = false) List<MultipartFile> files, Locale locale) {

        return ResponseData.builder()
                .status(HttpStatus.CREATED.value())
                .message(messageSource.getMessage("category.create.success", null, locale))
                .data(categoryService.createCategory(categoryRequestDTO, files))
                .build();
    }


    @GetMapping("/search")
    public ResponseData<?> getAllCategories(
            @Valid @ModelAttribute CategorySearchReqDTO request,
            @Min(value = 0, message = "{common.page.min}")
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @Min(value = 1, message = "{common.size.min}")
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            Locale locale) throws BadRequestException {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .data(categoryService.getAllCategories(request, pageable, locale))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<?> updateCategory(@Min(value = 1, message = "{common.id.positive}") @PathVariable Long id,
                                          @Valid @ModelAttribute UpdateCategoryReqDTO categoryRequestDTO,
                                          @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                          @RequestParam(value = "deleteIds", required = false) List<Long> deleteIds,
                                          Locale locale) {
        CategoryDetailResDTO data = categoryService.updateCategory(id, categoryRequestDTO, files, deleteIds);
        String message = messageSource.getMessage("category.update.success", null, locale);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }


    @DeleteMapping("/{id}")
    public ResponseData<?> softDelete(@Min(value = 1, message = "{common.id.positive}") @PathVariable Long id, Locale locale) {
        categoryService.softDelete(id, locale);
        String message = messageSource.getMessage("category.delete.success", null, locale);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(null)
                .build();
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportCategories(@Valid @ModelAttribute CategorySearchReqDTO request) throws BadRequestException {
        String filename = "categories.xlsx";

        // 1. Gọi Service (Service trả về "dòng chảy")
        StreamingResponseBody stream = categoryService.exportCategories(request);

        // 2. Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        // 3. Trả về
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

}