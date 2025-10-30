package com.increff.pos.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemApi extends AbstractApi<OrderItem>{

    @Autowired
    OrderItemDao orderItemDao;

    @Transactional
    public void add(OrderItem orderItem) {
        orderItemDao.add(orderItem);
    }

    public Page<OrderItem> getAll(Pageable pageable) {
        return orderItemDao.findAll(pageable);
    }

    public OrderItem getById(Integer id) throws ApiException {
        OrderItem orderItemPojo = orderItemDao.findById(id);
        ifNotExists(orderItemPojo);
        return orderItemPojo;
    }

    @Transactional
    public OrderItem update(Integer id, OrderItem orderItem) throws ApiException {
        OrderItem orderItemExisting = orderItemDao.findById(id);
        ifNotExists(orderItemExisting);
        orderItemExisting.setQuantity(orderItem.getQuantity());
        orderItemExisting.setSellingPrice(orderItem.getSellingPrice());
        return orderItemExisting;
    }

    @Transactional
    public List<OrderItem> getAllByOrderId(Integer id) {
        List<OrderItem> listItems = orderItemDao.getByOrderId(id);
        return listItems;
    }
}
