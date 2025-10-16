package com.increff.pos.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.OrdersDao;
import com.increff.pos.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderApi extends AbstractApi<Orders>{

    @Autowired
    OrdersDao ordersDao;

    @Transactional
    public void add(Orders order) {
        ordersDao.add(order);
    }

    public List<Orders> getAll() {
        return ordersDao.findAll();
    }

    public Orders getById(Integer id) throws ApiException {
        Orders order = ordersDao.findById(id);
        ifNotExists(order);
        return order;
    }
}
