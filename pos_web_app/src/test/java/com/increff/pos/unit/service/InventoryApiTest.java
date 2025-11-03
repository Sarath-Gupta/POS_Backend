package com.increff.pos.unit.service; // Or your unit test package

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

@ExtendWith(MockitoExtension.class)
public class InventoryApiTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApi inventoryApi; // The service class we are testing


    @Test
    @DisplayName("add() should succeed for a new product ID")
    public void add_validNewInventory_shouldSucceed() throws ApiException {
        Inventory newInventory = InventoryFactory.mockNewObject(101, 50);
        when(inventoryDao.findByProductId(101)).thenReturn(null);
        inventoryApi.add(newInventory);
        verify(inventoryDao, times(1)).add(newInventory);
        verify(inventoryDao, times(1)).findByProductId(101);
    }

    @Test
    @DisplayName("add() should throw ApiException for a duplicate product ID")
    public void add_duplicateProductId_shouldThrowApiException() {
        Inventory newInventory = InventoryFactory.mockNewObject(101, 50);
        Inventory existingInventory = InventoryFactory.mockPersistedObject(1, 101, 20);
        when(inventoryDao.findByProductId(101)).thenReturn(existingInventory);
        assertThrows(ApiException.class, () -> {
            inventoryApi.add(newInventory);
        });

        verify(inventoryDao, times(1)).findByProductId(101);
        verify(inventoryDao, never()).add(any(Inventory.class));
    }

    @Test
    @DisplayName("findById() should return inventory when ID exists")
    public void findById_existingId_shouldReturnInventory() throws ApiException {
        Integer invId = 1;
        Inventory expected = InventoryFactory.mockPersistedObject(invId, 101, 50);
        when(inventoryDao.findById(invId)).thenReturn(expected);
        Inventory actual = inventoryApi.findById(invId);

        assertNotNull(actual);
        assertEquals(invId, actual.getId());
        verify(inventoryDao, times(1)).findById(invId);
    }

    @Test
    @DisplayName("findById() should throw ApiException when ID does not exist")
    public void findById_nonExistentId_shouldThrowApiException() {
        Integer invId = 999;
        when(inventoryDao.findById(invId)).thenReturn(null);
        assertThrows(ApiException.class, () -> {
            inventoryApi.findById(invId);
        });
        verify(inventoryDao, times(1)).findById(invId);
    }


    @Test
    @DisplayName("findByProductId() should return inventory when ID exists")
    public void findByProductId_existingId_shouldReturnInventory() throws ApiException {
        Integer productId = 101;
        Inventory expected = InventoryFactory.mockPersistedObject(1, productId, 50);
        when(inventoryDao.findByProductId(productId)).thenReturn(expected);
        Inventory actual = inventoryApi.findByProductId(productId);
        assertNotNull(actual);
        assertEquals(productId, actual.getProductId());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("findByProductId() should throw ApiException when ID does not exist")
    public void findByProductId_nonExistentId_shouldThrowApiException() {
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);
        assertThrows(ApiException.class, () -> {
            inventoryApi.findByProductId(productId);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("getAll() should return a list of inventory items")
    public void getAll_inventoriesFound_shouldReturnList() {
        List<Inventory> inventoryList = InventoryFactory.createInventoryList();
        when(inventoryDao.getAll()).thenReturn(inventoryList);
        List<Inventory> result = inventoryApi.getAll();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(101, result.get(0).getProductId());
        verify(inventoryDao, times(1)).getAll();
    }

    @Test
    @DisplayName("getAll() should return empty list when no items found")
    public void getAll_noInventoriesFound_shouldReturnEmptyList() {
        when(inventoryDao.getAll()).thenReturn(Collections.emptyList());
        List<Inventory> result = inventoryApi.getAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inventoryDao, times(1)).getAll();
    }

    @Test
    @DisplayName("update() should update quantity for existing inventory")
    public void update_validUpdate_shouldSucceed() throws ApiException {
        Integer invId = 1;
        Integer productId = 101;
        Inventory existing = InventoryFactory.mockPersistedObject(invId, productId, 50);
        Inventory updateData = InventoryFactory.mockNewObject(productId, 100);

        when(inventoryDao.findById(invId)).thenReturn(existing);
        Inventory updated = inventoryApi.update(invId, updateData);

        assertNotNull(updated);
        assertEquals(invId, updated.getId());
        assertEquals(100, updated.getQuantity());
        assertEquals(100, existing.getQuantity());
        verify(inventoryDao, times(1)).findById(invId);
    }

    @Test
    @DisplayName("update() should throw ApiException when ID does not exist")
    public void update_nonExistentId_shouldThrowApiException() {
        Integer invId = 999;
        Inventory updateData = InventoryFactory.mockNewObject(101, 100);
        when(inventoryDao.findById(invId)).thenReturn(null);
        assertThrows(ApiException.class, () -> {
            inventoryApi.update(invId, updateData);
        });
        verify(inventoryDao, times(1)).findById(invId);
    }

    @Test
    @DisplayName("checkStock() should succeed when quantity is sufficient")
    public void checkStock_sufficientQuantity_shouldSucceed() {
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 10;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        assertDoesNotThrow(() -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should succeed when quantity is exact")
    public void checkStock_exactQuantity_shouldSucceed() {
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        assertDoesNotThrow(() -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should throw ApiException when quantity is insufficient")
    public void checkStock_insufficientQuantity_shouldThrowApiException() {
        Integer productId = 101;
        Integer quantityInStock = 19;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        ApiException e = assertThrows(ApiException.class, () -> {
            inventoryApi.checkStock(productId, quantityToReduce);
        });

        assertEquals("Insufficient Quantity", e.getMessage());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("checkStock() should throw NullPointerException when product not found (BUG)")
    public void checkStock_productNotFound_shouldThrowNullPointerException_BUG() {
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            inventoryApi.checkStock(productId, 1);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should succeed and update quantity")
    public void reduceStock_sufficientQuantity_shouldSucceed() throws ApiException {
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 10;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        inventoryApi.reduceStock(productId, quantityToReduce);

        assertEquals(10, existing.getQuantity());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should succeed and set quantity to zero")
    public void reduceStock_exactQuantity_shouldSucceed() throws ApiException {
        Integer productId = 101;
        Integer quantityInStock = 20;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        inventoryApi.reduceStock(productId, quantityToReduce);

        assertEquals(0, existing.getQuantity());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should throw ApiException for over-reduction")
    public void reduceStock_insufficientQuantity_shouldThrowApiException() {
        Integer productId = 101;
        Integer quantityInStock = 19;
        Integer quantityToReduce = 20;
        Inventory existing = InventoryFactory.mockPersistedObject(1, productId, quantityInStock);
        when(inventoryDao.findByProductId(productId)).thenReturn(existing);

        ApiException e = assertThrows(ApiException.class, () -> {
            inventoryApi.reduceStock(productId, quantityToReduce);
        });

        assertEquals(-1, existing.getQuantity());
        assertEquals("Item out of Stock", e.getMessage());
        verify(inventoryDao, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("reduceStock() should throw NullPointerException when product not found (BUG)")
    public void reduceStock_productNotFound_shouldThrowNullPointerException_BUG() {
        Integer productId = 999;
        when(inventoryDao.findByProductId(productId)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            inventoryApi.reduceStock(productId, 1);
        });
        verify(inventoryDao, times(1)).findByProductId(productId);
    }
}
