package com.example.nodo_final.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateCategoryReqDTO {

    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    String name;

    @Size(max = 50, message = "Mã loại không được vượt quá 50 ký tự")
    @JsonProperty("category_code")
    String categoryCode;

    @Size(max = 200, message = "Mô tả không được vượt quá 200 ký tự")
    String description;
}
