package com.increff.pos.dao;

import com.increff.pos.entity.Orders;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;


@Repository
public class DashboardDao extends AbstractDao<Orders> {

    private static final String SELECT_CLIENT_COUNT = "SELECT COUNT(c.id) FROM Client c";
    private static final String SELECT_PRODUCT_COUNT = "SELECT COUNT(p.id) FROM Product p";
    private static final String SELECT_ORDER_COUNT = "SELECT COUNT(o.id) FROM Orders o WHERE o.status = 'INVOICED'";
    private static final String SELECT_TOTAL_REVENUE = "SELECT COALESCE(SUM(o.total_amount), 0.0) FROM Orders o WHERE o.status = 'INVOICED'";

    public Long getTotalClients() {
        TypedQuery<Long> query = getEntityManager().createQuery(SELECT_CLIENT_COUNT, Long.class);
        return query.getSingleResult();
    }

    public Long getTotalProducts() {
        TypedQuery<Long> query = getEntityManager().createQuery(SELECT_PRODUCT_COUNT, Long.class);
        return query.getSingleResult();
    }

    public Double getTotalRevenue() {
        TypedQuery<Double> query = getEntityManager().createQuery(SELECT_TOTAL_REVENUE, Double.class);
        return query.getSingleResult();
    }

    public Long getTotalOrders() {
        TypedQuery<Long> query = getEntityManager().createQuery(SELECT_ORDER_COUNT, Long.class);
        return query.getSingleResult();
    }
}

