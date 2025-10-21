package com.increff.pos.dao;

import com.increff.pos.entity.Orders;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Query;


@Repository
public class OrdersDao extends AbstractDao<Orders> {

    public static final String UPDATE_STATUS_INVOICED = "UPDATE Orders SET status = 'INVOICED' WHERE id = :orderId";
    public static final String UPDATE_STATUS_CANCELLED = "UPDATE Orders SET status = 'CANCELLED' WHERE id = :orderId";

    @Transactional
    public void updateStatusToInvoiced(Integer orderId) {
        Query query = getEntityManager()
                .createQuery(UPDATE_STATUS_INVOICED);
        query.setParameter("orderId", orderId);
        query.executeUpdate();
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Query query = getEntityManager()
                .createQuery(UPDATE_STATUS_CANCELLED);
        query.setParameter("orderId", orderId);
        query.executeUpdate();
    }




}