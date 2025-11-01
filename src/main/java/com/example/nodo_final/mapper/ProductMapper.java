package com.example.nodo_final.mapper;

import com.example.nodo_final.dto.request.ProductRequestDTO;
import com.example.nodo_final.dto.request.UpdateProductReqDTO;
import com.example.nodo_final.dto.response.ProductResponseDTO;
import com.example.nodo_final.entity.Category;
import com.example.nodo_final.entity.Product;
import com.example.nodo_final.entity.Resource;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ResourceMapper.class})
public interface ProductMapper {

    Product toEntity(ProductRequestDTO productRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Product updateEntityFromDto(UpdateProductReqDTO dto, @MappingTarget Product product);

    @Named("mapCategory")
    default String mapCategory(List<Category> categories) {
        if(categories == null) {
            return null;
        }
        else {
            return categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
        }
    }


    @Mapping(target = "categories", expression = "java(mapCategory(categories))")
    @Mapping(source = "imageList", target = "images")
    ProductResponseDTO toDto(Product product, @Context List<Category> categories, List<Resource> imageList);
}
