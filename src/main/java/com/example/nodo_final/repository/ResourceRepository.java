package com.example.nodo_final.repository;

import com.example.nodo_final.dto.response.ResourceResponseDTO;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.entity.Resource;
import com.example.nodo_final.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("SELECT r FROM Resource r WHERE r.id IN :ids AND r.category.id = :categoryId AND r.status = :status")
    List<Resource> findResourcesForDelete(
            @Param("ids") List<Long> ids,
            @Param("categoryId") Long categoryId,
            @Param("status") Status status
    );

    @Query("SELECT r FROM Resource r WHERE r.id IN :ids AND r.product.id = :productId AND r.status = :status")
    List<Resource> findResourcesForDeleteByProductId(
            @Param("ids") List<Long> ids,
            @Param("productId") Long productId,
            @Param("status") Status status
    );

    List<Resource> findByProductAndStatus(Product product, Status status); //
}
