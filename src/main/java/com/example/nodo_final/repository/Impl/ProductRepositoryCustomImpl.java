package com.example.nodo_final.repository.Impl;

import com.example.nodo_final.dto.request.ProductSearchReqDTO;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.repository.ProductRepositoryCustom;
import com.example.nodo_final.utils.EscapeHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Object[]> searchProducts(ProductSearchReqDTO request, Pageable pageable) {

        // 1. SELECT: 7 cột (id, name, code, price, quantity, createdDate, modifiedDate)
        String selectClause = "SELECT DISTINCT p.id, p.name, p.productCode, p.price, p.quantity, p.createdDate, p.modifiedDate ";

        // 2. FROM: Chuẩn bị JOIN động
        String fromClause = "FROM Product p ";

        // 3. WHERE: Xây dựng động
        StringBuilder whereClause = new StringBuilder("WHERE p.status = :status "); //
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Status.ACTIVE);

        // --- Thêm JOIN và WHERE động ---

        // (PHỨC TẠP) Lọc theo categoryId [cite: 144]
        if (request.getCategoryId() != null) {
            fromClause += "JOIN p.productCategories pc "; // JOIN động
            whereClause.append("AND pc.category.id = :categoryId AND pc.status = :status ");
            parameters.put("categoryId", request.getCategoryId());
        }

        // Lọc theo name [cite: 141]
        if (request.getName() != null && !request.getName().isEmpty()) {
            whereClause.append("AND LOWER(p.name) LIKE :name ESCAPE '\\' ");
            parameters.put("name", "%" + EscapeHelper.escapeLike(request.getName().toLowerCase()) + "%");
        }

        // Lọc theo code [cite: 142]
        if (request.getProductCode() != null && !request.getProductCode().isEmpty()) {
            whereClause.append("AND p.productCode = :productCode ");
            parameters.put("productCode", request.getProductCode());
        }

        // Lọc theo ngày [cite: 143]
        if (request.getCreatedFrom() != null) {
            whereClause.append("AND p.createdDate >= :createdFrom ");
            parameters.put("createdFrom", request.getCreatedFrom());
        }
        if (request.getCreatedTo() != null) {
            whereClause.append("AND p.createdDate <= :createdTo ");
            parameters.put("createdTo", request.getCreatedTo());
        }

        // --- QUERY 1: Lấy DỮ LIỆU (Object[]) ---
        String jpql = selectClause + fromClause + whereClause.toString() + " ORDER BY p.createdDate DESC";
        Query query = em.createQuery(jpql);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        parameters.forEach(query::setParameter);

        List<Object[]> results = query.getResultList();

        // --- QUERY 2: Lấy COUNT (phải khớp JOIN) ---
        String countJpql = "SELECT COUNT(DISTINCT p) " + fromClause + whereClause.toString();
        Query countQuery = em.createQuery(countJpql);
        parameters.forEach(countQuery::setParameter);

        Long totalElements = (Long) countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, totalElements);
    }
}
