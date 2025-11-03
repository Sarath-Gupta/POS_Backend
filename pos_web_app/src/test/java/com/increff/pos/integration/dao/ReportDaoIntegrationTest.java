package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.*;
import com.increff.pos.entity.*;
import com.increff.pos.commons.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class ReportDaoIntegrationTest {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private ClientDao clientDao;
    @Autowired
    private ProductDao productDao;
    @Autowired
    private InventoryDao inventoryDao;
    @Autowired
    private OrdersDao ordersDao;
    @Autowired
    private OrderItemDao orderItemDao;

    private Client c1, c2;
    private Product p1, p2, p3;
    private Inventory i1, i2, i3;
    private Orders o1, o2;
    private OrderItem oi1, oi2, oi3;

    private final Double EXPECTED_SALES_REVENUE = 990.0;
    private final Long EXPECTED_SALES_ORDERS = 2L;

    private final Long EXPECTED_INV_PRODUCTS = 3L;
    private final Long EXPECTED_INV_CLIENTS = 2L;
    private final Long EXPECTED_LOW_STOCK = 1L;
    private final Double EXPECTED_INV_VALUE = 3750.0;

    private final Double EXPECTED_DASH_REVENUE = 990.0;
    private final Long EXPECTED_DASH_ORDERS = 2L;
    private final Long EXPECTED_DASH_PRODUCTS = 2L;
    private final Long EXPECTED_DASH_CLIENTS = 1L;
    private final Double EXPECTED_DASH_AVG_ORDER = 495.0;
    private final Long EXPECTED_DASH_LOW_STOCK = 2L;
    private final Double EXPECTED_DASH_INV_VALUE = 2500.0;


    public void setupData() {
        c1 = createClient("Client A");
        c2 = createClient("Client B");
        p1 = createProduct(c1.getId(), "barcode1", "Product 1", 50.0);
        p2 = createProduct(c1.getId(), "barcode2", "Product 2", 100.0);
        p3 = createProduct(c2.getId(), "barcode3", "Product 3", 75.0);

        i1 = createInventory(p1.getId(), 5);
        i2 = createInventory(p2.getId(), 20);
        i3 = createInventory(p3.getId(), 20);

        // FIX 2: Use stable, hard-coded dates instead of ZonedDateTime.now()
        ZonedDateTime date1 = ZonedDateTime.of(2025, 10, 20, 10, 0, 0, 0, ZonedDateTime.now().getZone());
        ZonedDateTime date2 = ZonedDateTime.of(2025, 10, 21, 10, 0, 0, 0, ZonedDateTime.now().getZone());
        o1 = createOrder(OrderStatus.INVOICED, date1); // Oct 20
        o2 = createOrder(OrderStatus.CREATED, date2); // Oct 21

        oi1 = createOrderItem(o1.getId(), p1.getId(), 10, 45.0);
        oi2 = createOrderItem(o1.getId(), p2.getId(), 5, 90.0);
        oi3 = createOrderItem(o2.getId(), p1.getId(), 2, 45.0);
    }

    @Test
    public void getDashboardSummary_whenDbEmpty_shouldReturnZeroedTuple() {
        List<Tuple> results = reportDao.getDashboardSummary();

        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);
        assertEquals(0.0, result.get("totalRevenue", Number.class).doubleValue());
        assertEquals(0L, result.get("totalOrders", Number.class).longValue());
        assertEquals(0L, result.get("totalProducts", Number.class).longValue());
        assertEquals(0L, result.get("totalClients", Number.class).longValue());
        assertEquals(0.0, result.get("averageOrderValue", Number.class).doubleValue());
        assertEquals(0L, result.get("lowStockItems", Number.class).longValue());
        assertEquals(0.0, result.get("inventoryValue", Number.class).doubleValue());
    }

    @Test
    public void getDashboardSummary_withData_shouldReturnAggregatedTuple() {
        setupData();
        List<Tuple> results = reportDao.getDashboardSummary();

        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);

        assertEquals(EXPECTED_DASH_REVENUE, result.get("totalRevenue", Double.class));
        assertEquals(EXPECTED_DASH_ORDERS, result.get("totalOrders", Long.class));
        assertEquals(EXPECTED_DASH_PRODUCTS, result.get("totalProducts", Long.class));
        assertEquals(EXPECTED_DASH_CLIENTS, result.get("totalClients", Long.class));
        assertEquals(EXPECTED_DASH_AVG_ORDER, result.get("averageOrderValue", Double.class));
        assertEquals(EXPECTED_DASH_LOW_STOCK, result.get("lowStockItems", Long.class));
        assertEquals(EXPECTED_DASH_INV_VALUE, result.get("inventoryValue", Double.class));
    }

    @Test
    public void getSalesMetrics_whenDbEmpty_shouldReturnZeroedTuple() {
        List<Tuple> results = reportDao.getSalesMetrics();

        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);
        assertEquals(0.0, result.get("totalRevenue", Number.class).doubleValue());
        assertEquals(0L, result.get("totalOrders", Number.class).longValue());
    }

    @Test
    public void getSalesMetrics_withData_shouldReturnAggregatedTuple() {
        setupData();
        List<Tuple> results = reportDao.getSalesMetrics();

        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);
        assertEquals(EXPECTED_SALES_REVENUE, result.get("totalRevenue", Double.class));
        assertEquals(EXPECTED_SALES_ORDERS, result.get("totalOrders", Long.class));
    }

    @Test
    public void getInventoryMetrics_whenDbEmpty_shouldReturnZeroedTuple() {
        List<Tuple> results = reportDao.getInventoryMetrics();
        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);
        assertEquals(0L, result.get("totalProducts", Number.class).longValue());
        assertEquals(0L, result.get("totalClients", Number.class).longValue());
        assertEquals(0L, result.get("lowStockItems", Number.class).longValue());
        assertEquals(0.0, result.get("inventoryValue", Number.class).doubleValue());
    }

    @Test
    public void getInventoryMetrics_withData_shouldReturnAggregatedTuple() {
        setupData();
        List<Tuple> results = reportDao.getInventoryMetrics();

        assertNotNull(results);
        assertEquals(1, results.size());
        Tuple result = results.get(0);
        assertEquals(EXPECTED_INV_PRODUCTS, result.get("totalProducts", Long.class));
        assertEquals(EXPECTED_INV_CLIENTS, result.get("totalClients", Long.class));
        assertEquals(EXPECTED_LOW_STOCK, result.get("lowStockItems", Long.class));
        assertEquals(EXPECTED_INV_VALUE, result.get("inventoryValue", Double.class));
    }

    @Test
    public void getTopSellingProducts_whenDbEmpty_shouldReturnEmptyList() {
        List<Tuple> results = reportDao.getTopSellingProducts(5);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getTopSellingProducts_withData_shouldReturnRankedList() {
        setupData();
        List<Tuple> results = reportDao.getTopSellingProducts(5);

        assertNotNull(results);
        assertEquals(2, results.size());

        Tuple r1 = results.get(0);
        assertEquals("Product 1", r1.get("productName", String.class));
        assertEquals(12L, r1.get("totalQuantitySold", Long.class));
        assertEquals(540.0, r1.get("totalRevenue", Double.class));
        assertEquals(5, r1.get("currentStock", Integer.class));

        Tuple r2 = results.get(1);
        assertEquals("Product 2", r2.get("productName", String.class));
        assertEquals(5L, r2.get("totalQuantitySold", Long.class));
        assertEquals(450.0, r2.get("totalRevenue", Double.class));
        assertEquals(20, r2.get("currentStock", Integer.class));
    }

    @Test
    public void getTopSellingProducts_withLimit_shouldReturnLimitedList() {
        setupData();
        List<Tuple> results = reportDao.getTopSellingProducts(1);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Product 1", results.get(0).get("productName", String.class));
    }

    private Client createClient(String name) {
        Client c = new Client();
        c.setClientName(name);
        clientDao.add(c);
        return c;
    }

    private Product createProduct(Integer clientId, String barcode, String name, Double mrp) {
        Product p = new Product();
        p.setClientId(clientId);
        p.setBarcode(barcode);
        p.setName(name);
        p.setMrp(mrp);
        productDao.add(p);
        return p;
    }

    private Inventory createInventory(Integer productId, Integer quantity) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(quantity);
        inventoryDao.add(i);
        return i;
    }

    private Orders createOrder(OrderStatus status, ZonedDateTime createdAt) {
        Orders o = new Orders();
        o.setStatus(status);
        o.setTotal_amount(0.0);
        ordersDao.add(o);
        return o;
    }

    private OrderItem createOrderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(quantity);
        oi.setSellingPrice(sellingPrice);
        orderItemDao.add(oi);
        return oi;
    }
}

