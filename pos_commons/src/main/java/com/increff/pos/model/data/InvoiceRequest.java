package com.increff.pos.model.data;

import com.increff.pos.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceRequest {
    private OrderData orderData;
    private List<OrderItem> orderItemData;
    private List<String> productNames;
}