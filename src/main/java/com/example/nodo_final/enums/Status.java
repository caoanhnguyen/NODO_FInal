package com.example.nodo_final.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
    @JsonProperty("0")
    DELETE,
    @JsonProperty("1")
    ACTIVE
}
