package com.increff.pos.dao;

import com.increff.pos.entity.OrderItem;
import org.springframework.stereotype.Repository;
import java.util.List;
import javax.persistence.TypedQuery;
import java.time.LocalDate;

@Repository
public class OrderItemDao extends AbstractDao<OrderItem> {

    private static final String SELECT_BY_ORDER_ID = "SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId";

    public List<OrderItem> getByOrderId(Integer orderId) {
        TypedQuery<OrderItem> query = getEntityManager().createQuery(SELECT_BY_ORDER_ID, OrderItem.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }



}
