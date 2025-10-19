package com.increff.pos.model.data;

import com.increff.pos.entity.Orders;
import com.increff.pos.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceRequest {
    private Orders order;
    private List<OrderItem> orderItems;
}