package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.DashboardDao;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import com.increff.pos.commons.OrderStatus; // Assuming enum path
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class DashboardDaoIntegrationTest {

    @Autowired
    private DashboardDao dashboardDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("getTotalClients() should return 0 when DB is empty")
    public void getTotalClients_whenEmpty_shouldReturnZero() {
        Long count = dashboardDao.getTotalClients();
        assertEquals(0L, count);
    }

    @Test
    @DisplayName("getTotalClients() should return correct count with data")
    public void getTotalClients_withData_shouldReturnCount() {
        setupData();
        Long count = dashboardDao.getTotalClients();
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("getTotalProducts() should return 0 when DB is empty")
    public void getTotalProducts_whenEmpty_shouldReturnZero() {
        Long count = dashboardDao.getTotalProducts();
        assertEquals(0L, count);
    }

    @Test
    @DisplayName("getTotalProducts() should return correct count with data")
    public void getTotalProducts_withData_shouldReturnCount() {
        setupData();
        Long count = dashboardDao.getTotalProducts();
        assertEquals(3L, count);
    }

    @Test
    @DisplayName("getTotalRevenue() should return 0.0 when DB is empty")
    public void getTotalRevenue_whenEmpty_shouldReturnZero() {
        Double revenue = dashboardDao.getTotalRevenue();
        assertEquals(0.0, revenue);
    }

    @Test
    @DisplayName("getTotalRevenue() should return correct sum with data")
    public void getTotalRevenue_withData_shouldReturnSum() {
        setupData();
        Double revenue = dashboardDao.getTotalRevenue();
        assertEquals(150.0, revenue);
    }

    @Test
    @DisplayName("getTotalOrders() should return 0 when DB is empty")
    public void getTotalOrders_whenEmpty_shouldReturnZero() {
        Long count = dashboardDao.getTotalOrders();
        assertEquals(0L, count);
    }

    @Test
    @DisplayName("getTotalOrders() should return correct count with data")
    public void getTotalOrders_withData_shouldReturnCount() {
        setupData();
        Long count = dashboardDao.getTotalOrders();
        assertEquals(1L, count);
    }

    private void setupData() {
        Client c1 = createClient("Client A");
        Client c2 = createClient("Client B");

        createProduct(c1.getId(), "barcode1", "Product 1", 50.0);
        createProduct(c1.getId(), "barcode2", "Product 2", 100.0);
        createProduct(c2.getId(), "barcode3", "Product 3", 75.0);

        createOrder(OrderStatus.INVOICED, ZonedDateTime.now().minusDays(1), 150.0);
        createOrder(OrderStatus.CREATED, ZonedDateTime.now(), 50.0);

        entityManager.flush();
    }

    private Client createClient(String name) {
        Client c = new Client();
        c.setClientName(name);
        entityManager.persist(c);
        return c;
    }

    private Product createProduct(Integer clientId, String barcode, String name, Double mrp) {
        Product p = new Product();
        p.setClientId(clientId);
        p.setBarcode(barcode);
        p.setName(name);
        p.setMrp(mrp);
        entityManager.persist(p);
        return p;
    }

    private Orders createOrder(OrderStatus status, ZonedDateTime createdAt, Double totalAmount) {
        Orders o = new Orders();
        o.setStatus(status);
        o.setTotal_amount(totalAmount);
        entityManager.persist(o);
        return o;
    }
}
