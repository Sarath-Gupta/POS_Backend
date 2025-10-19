package com.increff.pos.dto;

import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.service.OrderApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.entity.Orders;
import com.increff.pos.util.AbstractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class OrdersDto {

    @Autowired
    OrderApi orderApi;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    OrderFlow orderFlow;

    public Page<OrderData> getAll(Pageable pageable) {
        Page<Orders> ordersPage = orderApi.getAll(pageable);
        return ordersPage.map(orders -> mapper.convert(orders, OrderData.class));
    }

    public OrderData getById(Integer id) throws ApiException {
        Orders orderPojo = orderApi.getById(id);
        return mapper.convert(orderPojo, OrderData.class);
    }

    public InvoiceData finalizeOrder(Integer orderId) throws ApiException{
        return orderFlow.finalizeOrder(orderId);
    }

}
