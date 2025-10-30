package com.example.nodo_final.entity;

import com.example.nodo_final.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "status", length = 1)
    Status status = Status.ACTIVE;

    @Column(name = "created_date", updatable = false)
    @CreatedDate
    Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    Date modifiedDate;

    @Column(name = "created_by", updatable = false)
    @CreatedBy
    String createdBy;

    @Column(name = "modified_by")
    @LastModifiedBy
    String modifiedBy;
}
