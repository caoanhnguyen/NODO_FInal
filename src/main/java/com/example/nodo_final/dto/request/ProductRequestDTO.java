package com.example.nodo_final.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequestDTO {
    @NotBlank(message = "{product.name.notblank}")
    @Size(max = 200, message = "{product.name.size}")
    String name;

    @NotBlank(message = "{product.code.notblank}")
    @Size(max = 50, message = "{product.code.size}")
    String productCode;

    String description;

    @NotNull(message = "{product.price.notnull}")
    @Min(value = 0, message = "{product.price.min}")
    Double price;

    @NotNull(message = "{product.quantity.notnull}")
    @Min(value = 0, message = "{product.quantity.min}")
    Long quantity;

    @NotEmpty(message = "{product.of.category}")
    List<Long> categoryIds;
}
