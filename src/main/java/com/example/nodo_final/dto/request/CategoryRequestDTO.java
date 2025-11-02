package com.example.nodo_final.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequestDTO {
    @NotBlank(message = "{category.name.notblank}")
    @Size(max = 100, message = "{category.name.size}")
    String name;

    @NotBlank(message = "{category.code.notblank}")
    @Size(max = 50, message = "{category.code.size}")
    @JsonProperty("category_code")
    String categoryCode;

    @Size(max = 200, message = "{category.description.size}")
    String description;
}
