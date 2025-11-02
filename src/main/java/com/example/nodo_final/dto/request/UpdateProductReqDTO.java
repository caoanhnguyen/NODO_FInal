package com.example.nodo_final.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateProductReqDTO {
    @Size(max = 200, message = "{product.name.size}")
    String name;
    @Size(max = 50, message = "{product.code.size}")
    String productCode;
    @Size(max = 200, message = "{product.description.size}")
    String description;

    @Min(value = 0, message = "{product.price.min}")
    Double price;

    @Min(value = 0, message = "{product.quantity.min}")
    Long quantity;

    List<Long> categoryIds = new ArrayList<>();

    List<Long> deletedIds = new ArrayList<>();
}
