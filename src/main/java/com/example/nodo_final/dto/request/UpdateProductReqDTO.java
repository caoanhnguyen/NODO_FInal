package com.example.nodo_final.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateProductReqDTO {
    @Size(max = 200, message = "Tên không được vượt quá 200 ký tự")
    String name;

    String productCode;

    String description;

    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
    Double price;

    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    Long quantity;

    List<Long> categoryIds;

    List<Long> deletedIds;
}
