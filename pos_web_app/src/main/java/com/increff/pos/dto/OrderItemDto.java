package com.increff.pos.dto;

import com.increff.pos.entity.Client;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.service.OrderItemApi;
import com.increff.pos.flow.OrderItemFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OrderItemDto {

    @Autowired
    OrderItemApi orderItemApi;

    @Autowired
    OrderItemFlow orderItemFlow;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;

    public List<OrderItemData> add(List<OrderItemForm> listForm) throws ApiException {
        validationUtil.validate(listForm);
        List<OrderItem> listPojo = mapper.convert(listForm, OrderItem.class);
        orderItemFlow.add(listPojo);
        return mapper.convert(listPojo, OrderItemData.class);
    }

    public Page<OrderItemData> getAll(Pageable pageable) {
        Page<OrderItem> orderItemPage = orderItemApi.getAll(pageable);
        return orderItemPage.map(orderItem -> mapper.convert(orderItem, OrderItemData.class));
    }

    public OrderItemData getById(Integer id) throws ApiException {
        OrderItem orderItemPojo = orderItemApi.getById(id);
        return mapper.convert(orderItemPojo, OrderItemData.class);
    }

    public OrderItemData update(Integer id, OrderItemForm orderItemForm) throws ApiException {
        validationUtil.validate(orderItemForm);
        OrderItem orderItem = mapper.convert(orderItemForm, OrderItem.class);
        OrderItem orderItemUpdated = orderItemApi.update(id, orderItem);
        return mapper.convert(orderItemUpdated, OrderItemData.class);
    }

}
