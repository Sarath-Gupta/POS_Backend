package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.OrdersDao;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Product;
import com.increff.pos.commons.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class OrderItemDaoIntegrationTest {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrdersDao ordersDao;

    private Client testClient;
    private Product testProduct1;
    private Product testProduct2;
    private Orders testOrder1;
    private Orders testOrder2;

    @BeforeEach
    public void setup() {
        testClient = new Client();
        testClient.setClientName("Test Client");
        clientDao.add(testClient);

        testProduct1 = createTestProduct("barcode-1", "Product 1");
        testProduct2 = createTestProduct("barcode-2", "Product 2");

        testOrder1 = createTestOrder(OrderStatus.CREATED);
        testOrder2 = createTestOrder(OrderStatus.INVOICED);
    }

    @Test
    public void getByOrderId_whenItemsExist_shouldReturnList() {
        createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 5, 10.0);
        createTestOrderItem(testOrder1.getId(), testProduct2.getId(), 2, 20.0);
        createTestOrderItem(testOrder2.getId(), testProduct1.getId(), 1, 10.0);

        List<OrderItem> items = orderItemDao.getByOrderId(testOrder1.getId());

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    public void getByOrderId_whenNoItemsExist_shouldReturnEmptyList() {
        createTestOrderItem(testOrder2.getId(), testProduct1.getId(), 1, 10.0);
        List<OrderItem> items = orderItemDao.getByOrderId(testOrder1.getId());
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void getByOrderId_whenOrderIdDoesNotExist_shouldReturnEmptyList() {
        Integer nonExistentOrderId = -99;
        List<OrderItem> items = orderItemDao.getByOrderId(nonExistentOrderId);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void add_and_findById_shouldPersistOrderItem() {
        OrderItem item = createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 10, 15.50);

        assertNotNull(item.getId());
        OrderItem found = orderItemDao.findById(item.getId());
        assertNotNull(found);
        assertEquals(testOrder1.getId(), found.getOrderId());
        assertEquals(testProduct1.getId(), found.getProductId());
        assertEquals(10, found.getQuantity());
    }

    @Test
    public void findById_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemDao.findById(null);
        });
    }

    @Test
    public void add_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemDao.add(null);
        });
    }

    @Test
    public void update_shouldModifyOrderItem() {
        OrderItem item = createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 5, 10.0);
        Integer id = item.getId();

        OrderItem toUpdate = orderItemDao.findById(id);
        assertNotNull(toUpdate);
        toUpdate.setQuantity(15);
        toUpdate.setSellingPrice(12.0);
        orderItemDao.update(toUpdate);

        OrderItem updated = orderItemDao.findById(id);
        assertNotNull(updated);
        assertEquals(15, updated.getQuantity());
        assertEquals(12.0, updated.getSellingPrice());
    }

    @Test
    public void delete_shouldRemoveOrderItem() {
        OrderItem item = createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 5, 10.0);
        Integer id = item.getId();
        assertNotNull(orderItemDao.findById(id));

        orderItemDao.delete(item);

        OrderItem deleted = orderItemDao.findById(id);
        assertNull(deleted);
    }

    @Test
    public void delete_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemDao.delete(null);
        });
    }

    @Test
    public void getAll_whenItemsExist_shouldReturnList() {
        createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 5, 10.0);
        createTestOrderItem(testOrder1.getId(), testProduct2.getId(), 2, 20.0);
        createTestOrderItem(testOrder2.getId(), testProduct1.getId(), 1, 10.0);

        List<OrderItem> allItems = orderItemDao.getAll();

        assertNotNull(allItems);
        assertEquals(3, allItems.size());
    }

    @Test
    public void getAll_whenNoItemsExist_shouldReturnEmptyList() {
        List<OrderItem> allItems = orderItemDao.getAll();
        assertNotNull(allItems);
        assertTrue(allItems.isEmpty());
    }

    @Test
    public void findAll_withPagination_shouldReturnPage() {
        createTestOrderItem(testOrder1.getId(), testProduct1.getId(), 5, 10.0);
        createTestOrderItem(testOrder1.getId(), testProduct2.getId(), 2, 20.0);
        createTestOrderItem(testOrder2.getId(), testProduct1.getId(), 1, 10.0);

        Pageable pageable = PageRequest.of(0, 2);
        Page<OrderItem> page = orderItemDao.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void findAll_whenNoItems_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> page = orderItemDao.findAll(pageable);

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void findAll_withNullPageable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            orderItemDao.findAll(null);
        });
    }

    private Product createTestProduct(String barcode, String name) {
        Product p = new Product();
        p.setClientId(testClient.getId());
        p.setName(name);
        p.setBarcode(barcode);
        p.setMrp(10.0);
        productDao.add(p);
        return p;
    }

    private Orders createTestOrder(OrderStatus status) {
        Orders o = new Orders();
        o.setStatus(status);
        o.setTotal_amount(0.0);
        ordersDao.add(o);
        return o;
    }

    private OrderItem createTestOrderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(sellingPrice);
        orderItemDao.add(item);
        return item;
    }
}

