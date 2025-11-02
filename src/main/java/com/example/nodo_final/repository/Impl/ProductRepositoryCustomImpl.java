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

    private Map<String, Object> buildWhereClause(ProductSearchReqDTO request,
                                                 StringBuilder fromClause, //
                                                 StringBuilder whereClause) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Status.ACTIVE);

        // Lọc theo categoryId
        if (request.getCategoryId() != null) {
            // Nối động FROM và WHERE
            fromClause.append("JOIN p.productCategories pc ");
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

        return parameters;
    }

    @Override
    public Page<Object[]> searchProducts(ProductSearchReqDTO request, Pageable pageable) {

        // 1. SELECT: 7 cột (id, name, code, price, quantity, createdDate, modifiedDate)
        String selectClause = "SELECT DISTINCT p.id, p.name, p.productCode, p.price, p.quantity, p.createdDate, p.modifiedDate ";

        // 2. FROM: Chuẩn bị JOIN động
        String fromClause = "FROM Product p ";

        // 3. WHERE: Xây dựng động
        StringBuilder whereClause = new StringBuilder("WHERE p.status = :status "); //
        Map<String, Object> parameters = buildWhereClause(request, new StringBuilder(fromClause), whereClause);


        // --- QUERY 1: Lấy DỮ LIỆU (Object[]) ---
        String jpql = selectClause + fromClause + whereClause + " ORDER BY p.createdDate DESC";
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

    @Override
    public List<Object[]> searchProductsForExport(ProductSearchReqDTO request) {

        // 1. Sửa SELECT: Lấy 7 cột (chưa có Danh mục)
        String selectClause = "SELECT DISTINCT p.id, p.name, p.productCode, p.price, p.quantity, p.createdDate, p.modifiedDate ";

        StringBuilder fromClause = new StringBuilder("FROM Product p ");
        StringBuilder whereClause = new StringBuilder("WHERE p.status = :status ");

        // 2. Gọi hàm riêng (Tái sử dụng logic)
        Map<String, Object> parameters = buildWhereClause(request, fromClause, whereClause);

        // 3. Tạo Query (KHÔNG PHÂN TRANG)
        String jpql = selectClause + fromClause + whereClause;
        Query query = em.createQuery(jpql);
        parameters.forEach(query::setParameter);

        // 4. Trả về List đầy đủ
        return query.getResultList();
    }
}
