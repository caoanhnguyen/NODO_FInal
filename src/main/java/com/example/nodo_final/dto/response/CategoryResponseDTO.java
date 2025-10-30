package com.example.nodo_final.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryResponseDTO {

    Long id;
    String name;
    @JsonProperty("category_code")
    String categoryCode;
    String description;
    List<String> images;
}
