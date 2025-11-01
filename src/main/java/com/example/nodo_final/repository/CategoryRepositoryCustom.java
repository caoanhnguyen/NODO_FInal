package com.example.nodo_final.repository;

import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryRepositoryCustom {

    Page<Object[]> searchCategories(CategorySearchReqDTO searchReqDTO, Pageable pageable);

    List<Object[]> searchCategoriesForExport(CategorySearchReqDTO searchReqDTO);
}
