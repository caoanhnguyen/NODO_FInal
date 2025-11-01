package com.example.nodo_final.mapper;

import com.example.nodo_final.dto.response.ResourceResponseDTO;
import com.example.nodo_final.entity.Resource;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResourceMapper {


    ResourceResponseDTO toResponseDto(Resource resource);

    List<ResourceResponseDTO> toListResponseDto(List<Resource> resources);
}
