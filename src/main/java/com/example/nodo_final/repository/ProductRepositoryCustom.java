package com.example.nodo_final.repository;

import com.example.nodo_final.dto.request.ProductSearchReqDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {

    Page<Object[]> searchProducts(ProductSearchReqDTO request, Pageable pageable);

    List<Object[]> searchProductsForExport(ProductSearchReqDTO request);
}
