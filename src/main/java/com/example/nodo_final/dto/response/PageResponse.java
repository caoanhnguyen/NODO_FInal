package com.example.nodo_final.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> implements Serializable {

    T data;
    int currentPage;
    int pageSize;
    long totalElements;
    int totalPages;
    boolean hasNext;
    boolean hasPrevious;
}
