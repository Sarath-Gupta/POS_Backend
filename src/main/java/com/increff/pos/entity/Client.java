package com.increff.pos.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/*TODO:
snake case column, table names, use lombok, unique constraint class level,wrapper classes only
 */

@Entity
@Getter
@Setter
public class Client extends BaseEntity {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

   @Column(nullable = false, unique = true)
    private String clientName;
}
