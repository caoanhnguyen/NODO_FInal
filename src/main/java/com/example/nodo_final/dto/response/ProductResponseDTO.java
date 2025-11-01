package com.example.nodo_final.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseDTO {

    Long id;
    String name;
    @JsonProperty("product_code")
    String productCode;
    Double price;
    Long quantity;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("modified_date")
    Date modifiedDate;
    String categories;
    List<ResourceResponseDTO> images;
}
