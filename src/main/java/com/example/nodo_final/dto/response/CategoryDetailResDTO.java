package com.example.nodo_final.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDetailResDTO {

    Long id;
    String name;
    @JsonProperty("category_code")
    String categoryCode;
    String description;
    String status;
    List<ResourceResponseDTO> images;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("created_by")
    String createdBy;
}
