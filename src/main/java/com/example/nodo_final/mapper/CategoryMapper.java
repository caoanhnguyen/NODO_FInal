package com.example.nodo_final.mapper;

import com.example.nodo_final.dto.request.CategoryRequestDTO;
import com.example.nodo_final.dto.request.UpdateCategoryReqDTO;
import com.example.nodo_final.dto.response.CategoryDetailResDTO;
import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Resource;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses =  {ResourceMapper.class})
public interface CategoryMapper {

    Category toEntity(CategoryRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateCategoryReqDTO dto, @MappingTarget Category entity);

    @Mapping(target = "images", source = "imageList")
    CategoryDetailResDTO toDetailDto(Category category, List<Resource> imageList);
}
