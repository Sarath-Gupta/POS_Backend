package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.OrdersDto;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    @Autowired
    OrdersDto ordersDto;

    @RequestMapping(method = RequestMethod.GET)
    public Page<OrderData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {
        Pageable pageable = PageRequest.of(page, size);
        return ordersDto.getAll(pageable);
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.GET)
    public Page<OrderData> getOrdersFiltered(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ordersDto.getFilteredOrders(pageable, orderId, status, startDate, endDate);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public OrderData getById(@PathVariable("id") Integer id) throws ApiException {
        return ordersDto.getById(id);
    }

    @RequestMapping(value = "/api/order/{orderId}/finalize", method = RequestMethod.POST)
    public InvoiceData finalizeOrder(@PathVariable Integer orderId) throws ApiException {
        return ordersDto.finalizeOrder(orderId);
    }

    @RequestMapping(value = "/cancel/{orderId}", method = RequestMethod.PUT)
    public void cancelOrder(@PathVariable Integer orderId) throws ApiException {
        ordersDto.cancelOrder(orderId);
    }




}
