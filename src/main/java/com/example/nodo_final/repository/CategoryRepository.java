package com.example.nodo_final.repository;

import com.example.nodo_final.entity.Category;
import com.example.nodo_final.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndStatus(Long id, Status status);

    Optional<Category> findByCategoryCode(String categoryCode);

    boolean existsByCategoryCode(String categoryCode);

    List<Category> findAllByIdInAndStatus(List<Long> ids, Status status);

}
