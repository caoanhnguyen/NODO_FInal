package com.example.nodo_final.controller;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.repository.ResourceRepository;
import com.example.nodo_final.service.CategoryService;
import com.example.nodo_final.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final ResourceRepository resourceRepository;
    private final MessageSource messageSource;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<?> createCategory(@Valid @ModelAttribute CategoryRequestDTO categoryRequestDTO,
                                          @RequestParam(value = "files") List<MultipartFile> files, Locale locale) {

        return categoryService.createCategory(categoryRequestDTO, files, locale);
    }


    @GetMapping("/search")
    public ResponseData<?> getAllCategories(
            @Valid @ModelAttribute CategorySearchReqDTO request,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            Locale locale) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .data(categoryService.getAllCategories(request, pageable, locale))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<?> updateCategory(@PathVariable Long id,
                                          @Valid @ModelAttribute CategoryRequestDTO categoryRequestDTO,
                                          @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                          @RequestParam(value = "deleteIds", required = false) List<Long> deleteIds,
                                          Locale locale) {
        return categoryService.updateCategory(id, categoryRequestDTO, files, deleteIds, locale);
    }


    @DeleteMapping("/{id}")
    public ResponseData<?> softDelete(@PathVariable Long id, Locale locale) {
        return categoryService.softDelete(id, locale);
    }

    @PostMapping("/uploadImgTest")
    @Transactional
    public ResponseData<?> uploadImgTest(@ModelAttribute MultipartFile files) {
        Resource resource = fileStorageService.save(files);
        resourceRepository.save(resource);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Image uploaded successfully")
                .data(null)
                .build();
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportCategories( //
                                                                   @Valid @ModelAttribute CategorySearchReqDTO request
    ) {
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