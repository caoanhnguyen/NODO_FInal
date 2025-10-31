package com.example.nodo_final.mapper;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.response.CategoryDetailResDTO;
import com.example.nodo_final.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses =  {ResourceMapper.class})
public interface CategoryMapper {

    Category toEntity(CategoryRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryRequestDTO dto, @MappingTarget Category entity);


    CategoryDetailResDTO toDetailDto(Category entity);
}
