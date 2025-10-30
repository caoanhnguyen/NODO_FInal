package com.example.nodo_final.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "resources")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Resource extends BaseEntity {

    @Column(name = "resource_name")
    String resourceName;

    @Column(name = "url", length = 500)
    String url;

    @Column(name = "uuid", unique = true, nullable = false)
    String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;
}
