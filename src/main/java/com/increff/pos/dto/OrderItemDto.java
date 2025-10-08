package com.increff.pos.dto;

import com.increff.pos.api.service.OrderItemApi;
import com.increff.pos.api.flow.OrderItemFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<OrderItemData> getAll() {
        List<OrderItem> listPojo= orderItemApi.getAll();
        return mapper.convert(listPojo, OrderItemData.class);
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
