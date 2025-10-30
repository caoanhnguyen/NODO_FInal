package com.example.nodo_final.service.Impl;

import com.example.nodo_final.entity.Category;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.exception.ResourceNotFoundException;
import com.example.nodo_final.repository.CategoryRepository;
import com.example.nodo_final.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    @Transactional
    public void softDelete(Long id) {
        Category category = categoryRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setStatus(Status.DELETE);
    }
}
