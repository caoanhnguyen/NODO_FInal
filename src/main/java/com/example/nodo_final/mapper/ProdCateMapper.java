package com.example.nodo_final.mapper;

import com.example.nodo_final.entity.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProdCateMapper {

    ProductCategory toEntity(Long productId, Long categoryId);
}
