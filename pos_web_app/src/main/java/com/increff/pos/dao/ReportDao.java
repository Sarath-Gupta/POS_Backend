package com.increff.pos.dao;

import com.increff.pos.entity.Orders;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.List;


@Repository
public class ReportDao extends AbstractDao<Orders>{

    public List<Tuple> getDashboardSummary() {
        String jpql = "SELECT " +
                "COALESCE(SUM(oi.quantity * oi.sellingPrice), 0) AS totalRevenue, " +
                "COUNT(DISTINCT o.id) AS totalOrders, " +
                "COUNT(DISTINCT p.id) AS totalProducts, " +
                "COUNT(DISTINCT c.id) AS totalClients, " +
                "COALESCE(SUM(oi.quantity * oi.sellingPrice) / NULLIF(COUNT(DISTINCT o.id), 0.0), 0.0) AS averageOrderValue, " +
                "COUNT(CASE WHEN i.quantity < 10 THEN 1 END) AS lowStockItems, " +
                "COALESCE(SUM(i.quantity * p.mrp), 0) AS inventoryValue " +
                "FROM Orders o " +
                "LEFT JOIN OrderItem oi ON o.id = oi.orderId " +
                "LEFT JOIN Product p ON oi.productId = p.id " +
                "LEFT JOIN Client c ON p.clientId = c.id " +
                "LEFT JOIN Inventory i ON p.id = i.productId" +
                "WHERE o.status = 'INVOICED'";
        TypedQuery<Tuple> query = getEntityManager().createQuery(jpql, Tuple.class);
        return query.getResultList();
    }

    public List<Tuple> getSalesMetrics() {
        String jpql = "SELECT " +
                "COALESCE(SUM(oi.quantity * oi.sellingPrice), 0) AS totalRevenue, " +
                "COUNT(DISTINCT o.id) AS totalOrders " +
                "FROM Orders o, OrderItem oi " +
                "WHERE o.id = oi.orderId AND o.status = 'INVOICED' ";
        TypedQuery<Tuple> query = getEntityManager().createQuery(jpql, Tuple.class);
        return query.getResultList();
    }

    public List<Tuple> getInventoryMetrics() {
        String jpql = "SELECT " +
                "COUNT(DISTINCT p.id) AS totalProducts, " +
                "COUNT(DISTINCT c.id) AS totalClients, " +
                "COUNT(CASE WHEN i.quantity < 10 THEN 1 END) AS lowStockItems, " +
                "COALESCE(SUM(i.quantity * p.mrp), 0) AS inventoryValue " +
                "FROM Product p, Client c, Inventory i " +
                "WHERE p.clientId = c.id AND p.id = i.productId";
        TypedQuery<Tuple> query = getEntityManager().createQuery(jpql, Tuple.class);
        return query.getResultList();
    }

    public List<Tuple> getTopSellingProducts(Integer limit) {
        String jpql = "SELECT " +
                "p.name AS productName, " +
                "p.barcode AS barcode, " +
                "c.clientName AS clientName, " +
                "SUM(oi.quantity) AS totalQuantitySold, " +
                "SUM(oi.quantity * oi.sellingPrice) AS totalRevenue, " +
                "i.quantity AS currentStock " +
                "FROM OrderItem oi, Orders o, Product p, Client c, Inventory i " +
                "WHERE oi.orderId = o.id AND oi.productId = p.id AND p.clientId = c.id AND p.id = i.productId AND o.status = 'INVOICED'" +
                "GROUP BY p.id, p.name, p.barcode, c.clientName, i.quantity " +
                "ORDER BY totalQuantitySold DESC";
        TypedQuery<Tuple> query = getEntityManager().createQuery(jpql, Tuple.class);
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public List<Tuple> getRevenueTrend(String period) {
        String jpql = "SELECT " +
                "CAST(o.createdAt AS date) AS period, " +
                "SUM(oi.quantity * oi.sellingPrice) AS revenue, " +
                "COUNT(DISTINCT o.id) AS orderCount, " +
                "SUM(oi.quantity) AS itemsSold " +
                "FROM OrderItem oi, Orders o " +
                "WHERE oi.orderId = o.id AND o.status = 'INVOICED' " +
                "GROUP BY CAST(o.createdAt AS date) " +
                "ORDER BY CAST(o.createdAt AS date) DESC";
        TypedQuery<Tuple> query = getEntityManager().createQuery(jpql, Tuple.class);
        return query.getResultList();
    }
}

