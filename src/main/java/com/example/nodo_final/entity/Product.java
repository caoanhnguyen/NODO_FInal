package com.example.nodo_final.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "product_code", nullable = false, unique = true)
    String productCode;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "price")
    Double price;

    @Column(name = "quantity")
    Long quantity;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    Set<ProductCategory> productCategories;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<Resource> images = new ArrayList<>();

}
