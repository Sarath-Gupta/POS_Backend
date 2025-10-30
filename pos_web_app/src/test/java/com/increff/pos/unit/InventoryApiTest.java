package com.increff.pos.unit; // Or your unit test package

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.Inventory;
import com.increff.pos.commons.ApiException;
import com.increff.pos.factory.InventoryFactory;
import com.increff.pos.service.InventoryApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the InventoryApi class.
 * This class mocks the InventoryDao to isolate and test all business logic,
 * validation, and edge cases.
 *
 * NOTE: Assumes `ifExists(item)` throws ApiException if item is not null.
 * NOTE: Assumes `ifNotExists(item)` throws ApiException if item is null.
 */
@ExtendWith(MockitoExtension.class)
public class InventoryApiTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApi inventoryApi; // The service class we are testing

    // --- add() Tests ---

    @Test
    @DisplayName("add() should succeed for a new product ID")
    public void add_validNewInventory_shouldSucceed() throws ApiException {
        // Given
        Inventory newInventory = InventoryFactory.mockNewObject(101, 50);
        // Mock: No existing inventory found for this product ID
        when(inventoryDao.findByProductId(101)).thenReturn(null);

        // When
        inventoryApi.add(newInventory);

        // Then
        // Verify the DAO's add method was called exactly once
        verify(inventoryDao, times(1)).add(newInventory);
        verify(inventoryDao, times(1)).findByProductId(101);
    }

    @Test
    @DisplayName("add() should throw ApiException for a duplicate product ID")
    public void add_duplicateProductId_shouldThrowApiException() {
        // Given
        Inventory newInventory = InventoryFactory.mockNewObject(101, 50);
        Inventory existingInventory = InventoryFactory.mockPersistedObject(1, 101, 20);
        // Mock: An existing inventory item *is* found
        when(inventoryDao.findByProductId(101)).thenReturn(existingInventory);

        // When & Then
        // Assumes ifExists(notNull) throws ApiException
        assertThrows(ApiException.class, () -> {
            inventoryApi.add(newInventory);
        });

        // Verify the add method was never called
        verify(inventoryDao, times(1)).findByProductId(101);
        verify(inventoryDao, never()).add(any(Inventory.class));
    }

    // --- findById() Tests ---

    @Test
    @DisplayName("findById() should return inventory when ID exists")
    public void findById_existingId_shouldReturnInventory() throws ApiException {
        // Given
        Integer invId = 1;
        Inventory expected = InventoryFactory.mockPersistedObject(invId, 101, 50);
        when(inventoryDao.findById(invId)).thenReturn(expected);

        // When
        Inventory actual = inventoryApi.findById(invId);

        // Then
        assertNotNull(actual);
        assertEquals(invId, actual.getId());
        verify(inventoryDao, times(1)).findById(invId);
    }

    @Test
    @DisplayName("findById() should throw ApiException when ID does not exist")
    public void findById_nonExistentId_shouldThrowApiException() {
        // Given
        Integer invId = 999;
        when(inventoryDao.findById(invId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        assertThrows(ApiException.class, () -> {
            inventoryApi.findById(invId);
        });
        verify(inventoryDao, times(1)).findById(invId);
    }

    // --- findByProductId() Tests ---

    @Test
    @DisplayName("findByProductId() should return inventory when ID exists")
    public void findByProductId_existingId_shouldReturnInventory() throws ApiException {
        // Given
        Integer productId = 101;
        Inventory expected = InventoryFactory.mockPersistedObject(1, productId, 50);
        when(inventoryDao.findByProductId(productId)).thenReturn(expected);

        // When
        Inventory actual = inventoryApi.findByProductId(productId);

        // Then
        assertNotNull(actual);
        assertEquals(productId, actual.getProductId());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("findByProductId() should throw ApiException when ID does not exist")
    public void findByProductId_nonExistentId_shouldThrowApiException() {
        // Given
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        assertThrows(ApiException.class, () -> {
            inventoryApi.findByProductId(productId);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    // --- getAll() Tests ---

    @Test
    @DisplayName("getAll() should return a list of inventory items")
    public void getAll_inventoriesFound_shouldReturnList() {
        // Given
        List<Inventory> inventoryList = InventoryFactory.createInventoryList();
        when(inventoryDao.getAll()).thenReturn(inventoryList);

        // When
        List<Inventory> result = inventoryApi.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(101, result.get(0).getProductId());
        verify(inventoryDao, times(1)).getAll();
    }

    @Test
    @DisplayName("getAll() should return empty list when no items found")
    public void getAll_noInventoriesFound_shouldReturnEmptyList() {
        // Given
        when(inventoryDao.getAll()).thenReturn(Collections.emptyList());

        // When
        List<Inventory> result = inventoryApi.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inventoryDao, times(1)).getAll();
    }

    // --- update() Tests ---

    @Test
    @DisplayName("update() should update quantity for existing inventory")
    public void update_validUpdate_shouldSucceed() throws ApiException {
        // Given
        Integer invId = 1;
        Integer productId = 101;
        Inventory existing = InventoryFactory.mockPersistedObject(invId, productId, 50);
        Inventory updateData = InventoryFactory.mockNewObject(productId, 100); // New quantity is 100

        when(inventoryDao.findById(invId)).thenReturn(existing);

        // When
        Inventory updated = inventoryApi.update(invId, updateData);

        // Then
        assertNotNull(updated);
        assertEquals(invId, updated.getId()); // Still the same object
        assertEquals(100, updated.getQuantity()); // Quantity is updated
        // Verify the original object was mutated
        assertEquals(100, existing.getQuantity());
        verify(inventoryDao, times(1)).findById(invId);
    }

    @Test
    @DisplayName("update() should throw ApiException when ID does not exist")
    public void update_nonExistentId_shouldThrowApiException() {
        // Given
        Integer invId = 999;
        Inventory updateData = InventoryFactory.mockNewObject(101, 100);
        when(inventoryDao.findById(invId)).thenReturn(null);

        // When & Then
        // Assumes ifNotExists(null) throws ApiException
        assertThrows(ApiException.class, () -> {
            inventoryApi.update(invId, updateData);
        });
        verify(inventoryDao, times(1)).findById(invId);
    }

    // --- checkStock() Tests ---

    @Test
    @DisplayName("checkStock() should succeed when quantity is sufficient")
    public void checkStock_sufficientQuantity_shouldSucceed() {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 10;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When & Then
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should succeed when quantity is exact")
    public void checkStock_exactQuantity_shouldSucceed() {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When & Then
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should throw ApiException when quantity is insufficient")
    public void checkStock_insufficientQuantity_shouldThrowApiException() {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 19;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });

        assertEquals("Insufficient Quantity", e.getMessage());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should throw NullPointerException when product not found (BUG)")
    public void checkStock_productNotFound_shouldThrowNullPointerException_BUG() {
        // Given
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);

        // When & Then
        // This test reveals a bug: the API does not check if 'existing' is null
        // before calling existing.getQuantity()
        assertThrows(NullPointerException.class, () -> {
            inventoryApi.checkStock(productId, 1);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    // --- reduceStock() Tests ---

    @Test
    @DisplayName("reduceStock() should succeed and update quantity")
    public void reduceStock_sufficientQuantity_shouldSucceed() throws ApiException {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 10;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When
        inventoryApi.reduceStock(productId, quantityToReduce);

        // Then
        // Verify the quantity on the object was correctly reduced
        assertEquals(10, existing.getQuantity());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should succeed and set quantity to zero")
    public void reduceStock_exactQuantity_shouldSucceed() throws ApiException {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When
        inventoryApi.reduceStock(productId, quantityToReduce);

        // Then
        // Verify the quantity on the object is now zero
        assertEquals(0, existing.getQuantity());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should throw ApiException for over-reduction")
    public void reduceStock_insufficientQuantity_shouldThrowApiException() {
        // Given
        Integer productId = 101;
        Integer quantityInStock = 19;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        // When
        ApiException e = assertThrows(ApiException.class, () -> {
            inventoryApi.reduceStock(productId, quantityToReduce);
        });

        // Then
        // Verify the quantity was set to -1 before the check
        assertEquals(-1, existing.getQuantity());
        assertEquals("Item out of Stock", e.getMessage());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should throw NullPointerException when product not found (BUG)")
    public void reduceStock_productNotFound_shouldThrowNullPointerException_BUG() {
        // Given
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);

        // When & Then
        // This test reveals a bug: the API does not check if 'existing' is null
        // before calling existing.getQuantity()
        assertThrows(NullPointerException.class, () -> {
            inventoryApi.reduceStock(productId, 1);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }
}
