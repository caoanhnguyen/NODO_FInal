package com.example.nodo_final.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchReqDTO {
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
    String name;

    String productCode;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date createdFrom;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date createdTo;

    Long categoryId;
}
