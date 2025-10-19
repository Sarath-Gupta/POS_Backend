package com.increff.pos.controller;

import java.util.List;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.OrderItemDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderItems")
public class OrderItemController {

    @Autowired
    OrderItemDto orderItemDto;

    @RequestMapping(method = RequestMethod.POST)
    public List<OrderItemData> addOrderItems(@RequestBody List<OrderItemForm> listForm) throws ApiException {
        return orderItemDto.add(listForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<OrderItemData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {

        Pageable pageable = PageRequest.of(page, size);
        return orderItemDto.getAll(pageable);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public OrderItemData getById(@PathVariable("id") Integer id) throws ApiException {
        return orderItemDto.getById(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public OrderItemData update(@PathVariable("id") Integer id, @RequestBody OrderItemForm orderItemForm) throws ApiException {
        return orderItemDto.update(id, orderItemForm);
    }

}
