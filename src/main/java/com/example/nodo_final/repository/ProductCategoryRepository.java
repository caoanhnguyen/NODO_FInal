package com.example.nodo_final.repository;

import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.entity.ProductCategory;
import com.example.nodo_final.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
//    void saveAllByProductAndCategoryIn(Product product, List<Category> categories);

//    void saveAll(List<ProductCategory> productCategories);

    @Query("SELECT pc FROM ProductCategory pc JOIN FETCH pc.category c WHERE pc.product.id = :productId AND pc.status = :status")
    List<ProductCategory> findLinksByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") Status status
    );

    List<ProductCategory> findByProduct_Id(Long productId);

    List<ProductCategory> findByProduct_IdAndStatus(Long productId, Status status);

    @Query("SELECT pc FROM ProductCategory pc JOIN FETCH pc.category c " +
            "WHERE pc.product.id IN :productIds AND pc.status = :status")
    List<ProductCategory> findActiveLinksByProductIds(
            @Param("productIds") Collection<Long> productIds,
            @Param("status") Status status
    );
}
