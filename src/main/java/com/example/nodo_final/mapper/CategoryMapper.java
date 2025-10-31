package com.example.nodo_final.mapper;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequestDTO dto);
}
