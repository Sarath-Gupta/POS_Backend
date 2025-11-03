package com.increff.pos.integration.dao;

import com.increff.pos.config.TestDbConfig;
import com.increff.pos.dao.OrdersDao;
import com.increff.pos.entity.Orders;
import com.increff.pos.commons.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDbConfig.class})
@Transactional
public class OrdersDaoIntegrationTest {

    @Autowired
    private OrdersDao ordersDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void updateStatusToInvoiced_whenOrderExists_shouldUpdateStatus() {
        Orders order = createTestOrder(OrderStatus.CREATED, 0.0);
        Integer id = order.getId();

        ordersDao.updateStatusToInvoiced(id);

        entityManager.flush();
        entityManager.clear();

        Orders updatedOrder = ordersDao.findById(id);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.INVOICED, updatedOrder.getStatus());
    }

    @Test
    public void updateStatusToInvoiced_whenOrderDoesNotExist_shouldNotThrow() {
        Integer nonExistentId = -99;
        Orders otherOrder = createTestOrder(OrderStatus.CREATED, 0.0);
        Integer otherId = otherOrder.getId();

        assertDoesNotThrow(() -> ordersDao.updateStatusToInvoiced(nonExistentId));

        Orders retrievedOther = ordersDao.findById(otherId);
        assertEquals(OrderStatus.CREATED, retrievedOther.getStatus());
    }

    @Test
    public void cancelOrder_whenOrderExists_shouldUpdateStatus() {
        Orders order = createTestOrder(OrderStatus.CREATED, 0.0);
        Integer id = order.getId();

        entityManager.flush();
        entityManager.clear();

        ordersDao.cancelOrder(id);

        Orders updatedOrder = ordersDao.findById(id);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    }

    @Test
    public void cancelOrder_whenOrderDoesNotExist_shouldNotThrow() {
        Integer nonExistentId = -99;
        createTestOrder(OrderStatus.CREATED, 0.0);

        assertDoesNotThrow(() -> ordersDao.cancelOrder(nonExistentId));
    }

    @Test
    public void add_and_findById_shouldPersistOrder() {
        Orders order = createTestOrder(OrderStatus.CREATED, 99.99);

        assertNotNull(order.getId());
        Orders found = ordersDao.findById(order.getId());
        assertNotNull(found);
        assertEquals(OrderStatus.CREATED, found.getStatus());
        assertEquals(99.99, found.getTotal_amount());
    }

    @Test
    public void findById_whenDoesNotExist_shouldReturnNull() {
        Orders found = ordersDao.findById(-99);
        assertNull(found);
    }

    @Test
    public void findById_withNullId_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ordersDao.findById(null);
        });
    }

    @Test
    public void add_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ordersDao.add(null);
        });
    }

    @Test
    public void update_shouldModifyOrder() {
        Orders order = createTestOrder(OrderStatus.CREATED, 50.0);
        Integer id = order.getId();

        Orders toUpdate = ordersDao.findById(id);
        assertNotNull(toUpdate);
        toUpdate.setTotal_amount(150.0);
        toUpdate.setStatus(OrderStatus.INVOICED);
        ordersDao.update(toUpdate);

        Orders updated = ordersDao.findById(id);
        assertNotNull(updated);
        assertEquals(150.0, updated.getTotal_amount());
        assertEquals(OrderStatus.INVOICED, updated.getStatus());
    }

    @Test
    public void delete_shouldRemoveOrder() {
        Orders order = createTestOrder(OrderStatus.CREATED, 0.0);
        Integer id = order.getId();
        assertNotNull(ordersDao.findById(id));

        ordersDao.delete(order);

        Orders deleted = ordersDao.findById(id);
        assertNull(deleted);
    }

    @Test
    public void delete_withNullEntity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ordersDao.delete(null);
        });
    }

    @Test
    public void getAll_whenOrdersExist_shouldReturnList() {
        createTestOrder(OrderStatus.CREATED, 0.0);
        createTestOrder(OrderStatus.INVOICED, 0.0);

        List<Orders> allOrders = ordersDao.getAll();

        assertNotNull(allOrders);
        assertEquals(2, allOrders.size());
    }

    @Test
    public void getAll_whenNoOrdersExist_shouldReturnEmptyList() {
        List<Orders> allOrders = ordersDao.getAll();
        assertNotNull(allOrders);
        assertTrue(allOrders.isEmpty());
    }

    @Test
    public void findAll_withPagination_shouldReturnPage() {
        createTestOrder(OrderStatus.CREATED, 0.0);
        createTestOrder(OrderStatus.INVOICED, 0.0);
        createTestOrder(OrderStatus.CANCELLED, 0.0);

        Pageable pageable = PageRequest.of(0, 2);
        Page<Orders> page = ordersDao.findAll(pageable);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    public void findAll_whenNoOrders_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Orders> page = ordersDao.findAll(pageable);

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void findAll_withNullPageable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            ordersDao.findAll(null);
        });
    }

    private Orders createTestOrder(OrderStatus status, Double totalAmount) {
        Orders o = new Orders();
        o.setStatus(status);
        o.setTotal_amount(totalAmount);
        ordersDao.add(o);
        return o;
    }
}

