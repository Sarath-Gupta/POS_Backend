package com.increff.pos.unit; // Or your unit test package

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.commons.ApiException;
import com.increff.pos.factory.OrderItemFactory;
import com.increff.pos.service.OrderItemApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OrderItemApi class.
 * This class mocks the OrderItemDao to isolate and test all business logic,
 * validation, and edge cases.
 *
 * NOTE: Assumes `ifNotExists(item)` throws ApiException if item is null.
 */
@ExtendWith(MockitoExtension.class)
public class OrderItemApiTest {

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private OrderItemApi orderItemApi; // The service class we are testing

    // --- add() Tests ---

    @Test
    @DisplayName("add() should pass item to DAO")
    public void add_validItem_shouldSucceed() {
        // Given
        OrderItem newItem = OrderItemFactory.mockNewObject(1, 101, 5, 9.99);

        // When
        orderItemApi.add(newItem);

        // Then
        // Verify the DAO's add method was called exactly once with the new item
        verify(orderItemDao, times(1)).add(newItem);
    }

    @Test
    @DisplayName("add() should throw exception if DAO fails")
    public void add_nullItem_shouldThrowException() {
        // Given
        OrderItem newItem = OrderItemFactory.mockNewObject(1, 101, 5, 9.99);

        // Mock the DAO to throw an exception (e.g., DataIntegrityViolation)
        doThrow(new RuntimeException("DAO failure")).when(orderItemDao).add(newItem);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            orderItemApi.add(newItem);
        });

        // Verify the DAO was still called
        verify(orderItemDao, times(1)).add(newItem);
    }

    // --- getAll() Tests ---

    @Test
    @DisplayName("getAll() should return a page of order items")
    public void getAll_itemsFound_shouldReturnPaginatedResult() {
        // Given
        Pageable pageable = OrderItemFactory.createPageable();
        List<OrderItem> itemList = OrderItemFactory.createOrderItemList(1);
        Page<OrderItem> itemPage = OrderItemFactory.createOrderItemPage(itemList, pageable);

        when(orderItemDao.findAll(pageable)).thenReturn(itemPage);

        // When
        Page<OrderItem> result = orderItemApi.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getOrderId());
        verify(orderItemDao, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAll() should return empty page when no items found")
    public void getAll_noItemsFound_shouldReturnEmptyPage() {
        // Given
        Pageable pageable = OrderItemFactory.createPageable();
        when(orderItemDao.findAll(pageable)).thenReturn(Page.empty(pageable));

        // When
        Page<OrderItem> result = orderItemApi.getAll(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderItemDao, times(1)).findAll(pageable);
    }

    // --- getById() Tests ---

    @Test
    @DisplayName("getById() should return item when ID exists")
    public void getById_existingId_shouldReturnItem() throws ApiException {
        // Given
        Integer itemId = 1;
        OrderItem expectedItem = OrderItemFactory.mockPersistedObject(itemId, 1, 101, 5, 9.99);
        when(orderItemDao.findById(itemId)).thenReturn(expectedItem);

        // When
        OrderItem actualItem = orderItemApi.getById(itemId);

        // Then
        assertNotNull(actualItem);
        assertEquals(itemId, actualItem.getId());
        verify(orderItemDao, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("getById() should throw ApiException when ID does not exist")
    public void getById_nonExistentId_shouldThrowApiException() {
        // Given
        Integer itemId = 999;
        when(orderItemDao.findById(itemId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        Exception e = assertThrows(ApiException.class, () -> {
            orderItemApi.getById(itemId);
        });

        verify(orderItemDao, times(1)).findById(itemId);
    }

    // --- update() Tests ---

    @Test
    @DisplayName("update() should update quantity and price")
    public void update_validUpdate_shouldSucceed() throws ApiException {
        // Given
        Integer itemId = 1;
        // This object contains the *new* data
        OrderItem updateData = OrderItemFactory.mockNewObject(1, 101, 50, 88.88);
        // This is the *original* object from the database
        OrderItem oldItem = OrderItemFactory.mockPersistedObject(itemId, 1, 101, 1, 10.00);

        when(orderItemDao.findById(itemId)).thenReturn(oldItem);

        // When
        OrderItem updatedItem = orderItemApi.update(itemId, updateData);

        // Then
        assertNotNull(updatedItem);
        // Verify the object in memory was mutated with the new data
        assertEquals(50, updatedItem.getQuantity());
        assertEquals(88.88, updatedItem.getSellingPrice());

        // Verify the OrderID and ProductID were NOT changed
        assertEquals(1, updatedItem.getOrderId());
        assertEquals(101, updatedItem.getProductId());

        // Verify DAO interaction
        verify(orderItemDao, times(1)).findById(itemId);
        // Verify that no DAO.update() method is called (relies on @Transactional)
        verify(orderItemDao, never()).update(any(OrderItem.class));
    }

    @Test
    @DisplayName("update() should throw ApiException when item ID not found")
    public void update_itemNotFound_shouldThrowApiException() {
        // Given
        Integer itemId = 999;
        OrderItem updateData = OrderItemFactory.mockNewObject(1, 101, 50, 88.88);
        when(orderItemDao.findById(itemId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        assertThrows(ApiException.class, () -> {
            orderItemApi.update(itemId, updateData);
        });

        // Verify DAO interactions
        verify(orderItemDao, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("update() should throw NullPointerException when update data is null")
    public void update_nullData_shouldThrowNullPointerException() {
        // Given
        Integer itemId = 1;
        OrderItem oldItem = OrderItemFactory.mockPersistedObject(itemId, 1, 101, 1, 10.00);
        when(orderItemDao.findById(itemId)).thenReturn(oldItem);

        // When & Then
        // This will fail on orderItem.getQuantity()
        assertThrows(NullPointerException.class, () -> {
            orderItemApi.update(itemId, null);
        });

        // Verify DAO interactions
        verify(orderItemDao, times(1)).findById(itemId);
    }

    // --- getAllByOrderId() Tests ---

    @Test
    @DisplayName("getAllByOrderId() should return list of items for a valid order ID")
    public void getAllByOrderId_itemsFound_shouldReturnList() {
        // Given
        Integer orderId = 1;
        List<OrderItem> itemList = OrderItemFactory.createOrderItemList(orderId);
        when(orderItemDao.getByOrderId(orderId)).thenReturn(itemList);

        // When
        List<OrderItem> result = orderItemApi.getAllByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Check that both items belong to the correct order
        assertEquals(orderId, result.get(0).getOrderId());
        assertEquals(orderId, result.get(1).getOrderId());
        verify(orderItemDao, times(1)).getByOrderId(orderId);
    }

    @Test
    @DisplayName("getAllByOrderId() should return empty list for an order with no items")
    public void getAllByOrderId_noItemsFound_shouldReturnEmptyList() {
        // Given
        Integer orderId = 999;
        when(orderItemDao.getByOrderId(orderId)).thenReturn(Collections.emptyList());

        // When
        List<OrderItem> result = orderItemApi.getAllByOrderId(orderId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderItemDao, times(1)).getByOrderId(orderId);
    }
}
