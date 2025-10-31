package com.example.nodo_final.controller;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.repository.ResourceRepository;
import com.example.nodo_final.service.CategoryService;
import com.example.nodo_final.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

}