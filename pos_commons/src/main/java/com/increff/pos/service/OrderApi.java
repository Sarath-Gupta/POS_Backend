package com.increff.pos.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.OrdersDao;
import com.increff.pos.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Orders> getAll(Pageable pageable) {
        return ordersDao.findAll(pageable);
    }

    public Orders getById(Integer id) throws ApiException {
        Orders order = ordersDao.findById(id);
        ifNotExists(order);
        return order;
    }

    public void updateStatusToInvoiced(Integer orderId) {
        ordersDao.updateStatusToInvoiced(orderId);
    }

    public void cancelOrder(Integer orderId) {
        ordersDao.cancelOrder(orderId);
    }

    public void update(Orders order) {
        ordersDao.update(order);
    }

}
