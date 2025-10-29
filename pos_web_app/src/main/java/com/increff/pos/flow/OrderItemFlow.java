package com.increff.pos.flow;

import com.increff.pos.model.form.OrderItemForm;
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

import java.util.ArrayList;
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
        order.setTotal_amount(0.0);
        orderApi.add(order);
        Integer generatedOrderId = order.getId();
        Orders orderGenerated = orderApi.getById(generatedOrderId);
        Double grandTotal = 0.0;
        for(OrderItem orderItem : list) {
            Integer productId = orderItem.getProductId();
            Integer quantityOrdered = orderItem.getQuantity();
            orderItem.setOrderId(generatedOrderId);
            orderItemApi.add(orderItem);
            grandTotal += (quantityOrdered * orderItem.getSellingPrice());
            inventoryApi.reduceStock(productId, quantityOrdered);
        }

        orderGenerated.setTotal_amount(grandTotal);
        orderApi.update(orderGenerated);
    }

    private void validateProduct(OrderItem orderItem, Integer productId) throws ApiException{
        Product product = productApi.findById(productId);
        if(orderItem.getSellingPrice() > product.getMrp()) {
            throw new ApiException("Selling Price cannot be greater than MRP");
        }
    }

    public List<OrderItem> convert(List<OrderItemForm> listForm) throws ApiException{
        List<OrderItem> listPojo = new ArrayList<>();
        for(OrderItemForm orderItemForm : listForm) {
            Product product = productApi.findByBarcode(orderItemForm.getBarcode());

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItemForm.getQuantity());
            orderItem.setProductId(product.getId());
            orderItem.setSellingPrice(orderItemForm.getSellingPrice());

            listPojo.add(orderItem);
        }
        return listPojo;
    }
}