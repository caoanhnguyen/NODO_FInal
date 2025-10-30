package com.example.nodo_final.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    String categoryCode;

    @Column(name = "description", length = 200)
    String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    Set<ProductCategory> productCategories;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    List<Resource> images = new ArrayList<>();

}
