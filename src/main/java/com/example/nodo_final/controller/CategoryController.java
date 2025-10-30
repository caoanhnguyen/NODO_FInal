package com.example.nodo_final.controller;

import com.example.nodo_final.dto.response.ResponseData;
import com.example.nodo_final.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("")
    public ResponseData<?> createCategory(@RequestBody String categoryName) {
        return ResponseData.builder()
                .status(HttpStatus.CREATED.value())
                .message("Category created successfully")
                .data(null)
                .build();
    }


    @DeleteMapping("/{id}")
    public ResponseData<?> softDelete(@PathVariable Long id) {
        categoryService.softDelete(id);
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Category soft deleted successfully")
                .data(null)
                .build();
    }
}
