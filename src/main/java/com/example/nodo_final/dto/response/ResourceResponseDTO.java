package com.example.nodo_final.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceResponseDTO {
    @JsonIgnore
    Long id;
    @JsonProperty("resource_name")
    String resourceName;
    String url;
    String uuid;
    @JsonIgnore
    String status;
}
