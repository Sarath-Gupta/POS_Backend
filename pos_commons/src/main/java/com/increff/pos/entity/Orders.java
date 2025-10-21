package com.increff.pos.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import com.increff.pos.commons.OrderStatus;

@Entity
@Setter
@Getter
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    private Double total_amount;

}