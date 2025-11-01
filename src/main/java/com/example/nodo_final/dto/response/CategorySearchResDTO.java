package com.example.nodo_final.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CategorySearchResDTO {

    Long id;
    String name;
    @JsonProperty("category_code")
    String categoryCode;
    String description;
    List<ResourceResponseDTO> images;
    @JsonProperty("created_date")
    Date createdDate;
    @JsonProperty("modified_date")
    Date modifiedDate;

}
