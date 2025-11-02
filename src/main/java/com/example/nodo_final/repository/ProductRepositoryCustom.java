package com.example.nodo_final.repository;

import com.example.nodo_final.dto.request.ProductSearchReqDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Object[]> searchProducts(ProductSearchReqDTO request, Pageable pageable);

}
