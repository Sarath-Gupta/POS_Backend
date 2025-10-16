package com.increff.pos.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double mrp;

    @Column(nullable = true)
    private String imgUrl;
}
