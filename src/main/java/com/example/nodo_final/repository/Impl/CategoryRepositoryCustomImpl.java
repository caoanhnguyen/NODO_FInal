package com.example.nodo_final.repository.Impl;

import com.example.nodo_final.dto.request.CategorySearchReqDTO;
import com.example.nodo_final.enums.Status;
import com.example.nodo_final.repository.CategoryRepositoryCustom;
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

public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private Map<String, Object> buildWhereClause(CategorySearchReqDTO request, StringBuilder whereClause) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", Status.ACTIVE);

        if (request.getName() != null && !request.getName().isBlank()) {
            whereClause.append("AND LOWER(c.name) LIKE :name ESCAPE '\\' ");
            parameters.put("name", "%" + EscapeHelper.escapeLike(request.getName().toLowerCase()) + "%");
        }
        if (request.getCategoryCode() != null && !request.getCategoryCode().isEmpty()) {
            whereClause.append("AND c.categoryCode = :categoryCode ");
            parameters.put("categoryCode", request.getCategoryCode());
        }
        if (request.getCreatedFrom() != null) {
            whereClause.append("AND c.createdDate >= :createdFrom ");
            parameters.put("createdFrom", request.getCreatedFrom());
        }
        if (request.getCreatedTo() != null) {
            whereClause.append("AND c.createdDate <= :createdTo ");
            parameters.put("createdTo", request.getCreatedTo());
        }
        return parameters;
    }

    @Override
    public List<Object[]> searchCategoriesForExport(CategorySearchReqDTO request) {

        // 1. Xây dựng câu lệnh SELECT ... FROM ... WHERE
        String selectClause = "SELECT c.id, c.name, c.categoryCode, c.description, c.createdDate, c.modifiedDate, c.createdBy, c.modifiedBy ";

        String fromClause = "FROM Category c ";
        StringBuilder whereClause = new StringBuilder("WHERE c.status = :status ");

        // 2. Gọi hàm riêng (Tái sử dụng logic)
        Map<String, Object> parameters = buildWhereClause(request, whereClause);

        // 3. Tạo Query (KHÔNG PHÂN TRANG)
        String jpql = selectClause + fromClause + whereClause;
        Query query = em.createQuery(jpql);
        parameters.forEach(query::setParameter);

        // 4. Trả về List đầy đủ
        return query.getResultList();
    }

    @Override
    public Page<Object[]> searchCategories(CategorySearchReqDTO request, Pageable pageable) {

        // Select các cột theo yêu cầu
        String selectClause = "SELECT c.id, c.name, c.categoryCode, c.description, c.createdDate, c.modifiedDate ";

        String fromClause = "FROM Category c ";

        // Xây dựng điều kiện WHERE
        StringBuilder whereClause = new StringBuilder("WHERE c.status = :status ");

        Map<String, Object> parameters = buildWhereClause(request, whereClause);

        String jpql = selectClause + fromClause + whereClause;
        Query query = em.createQuery(jpql);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        parameters.forEach(query::setParameter);

        List<Object[]> results = query.getResultList();

        // Count query
        String countJpql = "SELECT COUNT(c) " + fromClause + whereClause;
        Query countQuery = em.createQuery(countJpql);
        parameters.forEach(countQuery::setParameter);
        Long total = (Long) countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
