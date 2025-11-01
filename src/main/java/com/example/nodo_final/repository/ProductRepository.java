package com.example.nodo_final.repository;

import com.example.nodo_final.entity.Product;
import com.example.nodo_final.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatus(Long id, Status status);

    boolean existsByProductCode(String productCode);

    Optional<Product> findByProductCode(String productCode);
}
