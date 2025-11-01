package com.example.nodo_final.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategorySearchReqDTO {
    String name;
    String categoryCode;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date createdFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date createdTo;
}
