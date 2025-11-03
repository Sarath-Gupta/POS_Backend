package com.increff.pos.unit.service; // Or your unit test package

import com.increff.pos.dao.OrdersDao;
import com.increff.pos.entity.Orders;
import com.increff.pos.commons.ApiException;
import com.increff.pos.factory.OrderFactory;
import com.increff.pos.commons.OrderStatus; // <-- IMPORT ADDED
import com.increff.pos.service.OrderApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OrderApi class.
 * This class mocks the OrdersDao to isolate and test all business logic,
 * validation, and edge cases.
 *
 * NOTE: Assumes `ifNotExists(order)` throws ApiException if order is null.
 */
@ExtendWith(MockitoExtension.class)
public class OrderApiTest {

    @Mock
    private OrdersDao ordersDao;

    @InjectMocks
    private OrderApi orderApi; // The service class we are testing

    // --- add() Tests ---

    @Test
    @DisplayName("add() should pass order to DAO")
    public void add_validOrder_shouldSucceed() {
        // Given
        Orders newOrder = OrderFactory.mockNewObject(150.75);

        // When
        orderApi.add(newOrder);

        // Then
        // Verify the DAO's add method was called exactly once with the new order
        verify(ordersDao, times(1)).add(newOrder);
    }

    @Test
    @DisplayName("add() should propagate exceptions from DAO")
    public void add_daoFails_shouldThrowException() {
        // Given
        Orders newOrder = OrderFactory.mockNewObject(150.75);
        // Mock the DAO to throw an exception
        doThrow(new RuntimeException("DAO failure")).when(ordersDao).add(newOrder);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            orderApi.add(newOrder);
        });

        // Verify the DAO was still called
        verify(ordersDao, times(1)).add(newOrder);
    }

    // --- getAll() Tests ---

    @Test
    @DisplayName("getAll() should return a page of orders")
    public void getAll_ordersFound_shouldReturnPaginatedResult() {
        // Given
        Pageable pageable = OrderFactory.createPageable();
        List<Orders> orderList = OrderFactory.createOrderList();
        Page<Orders> orderPage = OrderFactory.createOrderPage(orderList, pageable);

        when(ordersDao.findAll(pageable)).thenReturn(orderPage);

        // When
        Page<Orders> result = orderApi.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(100.50, result.getContent().get(0).getTotal_amount());
        verify(ordersDao, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAll() should return empty page when no orders found")
    public void getAll_noOrdersFound_shouldReturnEmptyPage() {
        // Given
        Pageable pageable = OrderFactory.createPageable();
        when(ordersDao.findAll(pageable)).thenReturn(Page.empty(pageable));

        // When
        Page<Orders> result = orderApi.getAll(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ordersDao, times(1)).findAll(pageable);
    }

    // --- getById() Tests ---

    @Test
    @DisplayName("getById() should return order when ID exists")
    public void getById_existingId_shouldReturnOrder() throws ApiException {
        // Given
        Integer orderId = 1;
        // --- FIX IS HERE: Used OrderStatus enum directly ---
        Orders expectedOrder = OrderFactory.mockPersistedObject(orderId, 100.50, OrderStatus.CREATED);
        when(ordersDao.findById(orderId)).thenReturn(expectedOrder);

        // When
        Orders actualOrder = orderApi.getById(orderId);

        // Then
        assertNotNull(actualOrder);
        assertEquals(orderId, actualOrder.getId());
        verify(ordersDao, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("getById() should throw ApiException when ID does not exist")
    public void getById_nonExistentId_shouldThrowApiException() {
        // Given
        Integer orderId = 999;
        when(ordersDao.findById(orderId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        Exception e = assertThrows(ApiException.class, () -> {
            orderApi.getById(orderId);
        });

        verify(ordersDao, times(1)).findById(orderId);
    }

    // --- updateStatusToInvoiced() Tests ---

    @Test
    @DisplayName("updateStatusToInvoiced() should call DAO method")
    public void updateStatusToInvoiced_validId_shouldSucceed() {
        // Given
        Integer orderId = 1;
        // Mock the DAO to do nothing (void method)
        doNothing().when(ordersDao).updateStatusToInvoiced(orderId);

        // When
        orderApi.updateStatusToInvoiced(orderId);

        // Then
        // Verify the DAO method was called exactly once
        verify(ordersDao, times(1)).updateStatusToInvoiced(orderId);
    }

    // --- cancelOrder() Tests ---

    @Test
    @DisplayName("cancelOrder() should call DAO method")
    public void cancelOrder_validId_shouldSucceed() {
        // Given
        Integer orderId = 1;
        doNothing().when(ordersDao).cancelOrder(orderId);

        // When
        orderApi.cancelOrder(orderId);

        // Then
        verify(ordersDao, times(1)).cancelOrder(orderId);
    }

    // --- update() Tests ---

    @Test
    @DisplayName("update() should call DAO update method")
    public void update_validOrder_shouldSucceed() {
        // Given
        // --- FIX IS HERE: Used OrderStatus enum directly ---
        Orders order = OrderFactory.mockPersistedObject(1, 120.00, OrderStatus.CREATED);
        doNothing().when(ordersDao).update(order);

        // When
        orderApi.update(order);

        // Then
        verify(ordersDao, times(1)).update(order);
    }
}

