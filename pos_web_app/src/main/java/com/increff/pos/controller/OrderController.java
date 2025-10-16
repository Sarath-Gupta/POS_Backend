package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.OrdersDto;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrdersDto ordersDto;

    @RequestMapping(method = RequestMethod.GET)
    public List<OrderData> getAll() {
        return ordersDto.getAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public OrderData getById(@PathVariable("id") Integer id) throws ApiException {
        return ordersDto.getById(id);
    }
}
