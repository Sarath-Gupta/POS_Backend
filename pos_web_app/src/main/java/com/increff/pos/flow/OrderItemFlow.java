package com.increff.pos.flow;

import com.increff.pos.service.OrderApi;
import com.increff.pos.service.OrderItemApi;
import com.increff.pos.service.ProductApi;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class OrderItemFlow {

    @Autowired
    ProductApi productApi;

    @Autowired
    OrderItemApi orderItemApi;

    @Autowired
    OrderApi orderApi;

    @Autowired
    InventoryApi inventoryApi;

    @Transactional
    public void add(List<OrderItem> list) throws ApiException {
        for(OrderItem orderItem : list) {
            Integer productId = orderItem.getProductId();
            validateProduct(orderItem, productId);
            inventoryApi.checkStock(productId, orderItem.getQuantity());
        }

        Orders order = new Orders();
        orderApi.add(order);

        for(OrderItem orderItem : list) {
            Integer productId = orderItem.getProductId();
            Integer quantityOrdered = orderItem.getQuantity();
            orderItem.setOrderId(order.getId());
            orderItemApi.add(orderItem);
            inventoryApi.reduceStock(productId, quantityOrdered);
        }
    }

    private void validateProduct(OrderItem orderItem, Integer productId) throws ApiException{
        Product product = productApi.findById(productId);
        if(orderItem.getSellingPrice() > product.getMrp()) {
            throw new ApiException("Selling Price cannot be greater than MRP");
        }
    }
}