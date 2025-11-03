package com.increff.pos.unit.flow;

import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.Product;
import com.increff.pos.factory.ClientFactory;
import com.increff.pos.factory.ProductFactory;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.service.ClientApi;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.ProductApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductFlowTest {

    @Mock
    private ClientApi clientApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private ProductFlow productFlow;

    @Test
    @DisplayName("add() should successfully add product and create inventory")
    public void add_success_shouldAddProductAndInventory() throws ApiException {
        Product newProduct = ProductFactory.mockNewObject("Test Product", "barcode123", 100.0);
        newProduct.setClientId(1);

        Client existingClient = ClientFactory.mockPersistedObject(1, "Test Client");

        when(clientApi.getById(1)).thenReturn(existingClient);

        doAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(99);
            return null;
        }).when(productApi).add(newProduct);

        productFlow.add(newProduct);

        verify(clientApi, times(1)).getById(1);

        verify(productApi, times(1)).add(newProduct);
        assertEquals(99, newProduct.getId());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryApi, times(1)).add(inventoryCaptor.capture());

        Inventory addedInventory = inventoryCaptor.getValue();
        assertNull(addedInventory.getId());
        assertEquals(99, addedInventory.getProductId());
        assertEquals(0, addedInventory.getQuantity());
    }

    @Test
    @DisplayName("add() should throw ApiException if client does not exist")
    public void add_clientNotFound_shouldThrowApiException() throws ApiException {
        Product newProduct = ProductFactory.mockNewObject("Test Product", "barcode123", 100.0);
        newProduct.setClientId(1);

        when(clientApi.getById(1)).thenReturn(null);

        ApiException e = assertThrows(ApiException.class, () -> {
            productFlow.add(newProduct);
        });

        assertEquals("Client doesn't exist", e.getMessage());

        verify(productApi, never()).add(any(Product.class));
        verify(inventoryApi, never()).add(any(Inventory.class));
    }

    @Test
    @DisplayName("add() should throw ApiException if productApi.add fails")
    public void add_productApiFails_shouldNotAddInventory() throws ApiException {
        Product newProduct = ProductFactory.mockNewObject("Test Product", "barcode123", 100.0);
        newProduct.setClientId(1);
        Client existingClient = ClientFactory.mockPersistedObject(1, "Test Client");

        when(clientApi.getById(1)).thenReturn(existingClient);

        doThrow(new ApiException("Product already exists")).when(productApi).add(newProduct);

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            productFlow.add(newProduct);
        });

        assertEquals("Product already exists", e.getMessage());

        verify(clientApi, times(1)).getById(1);
        verify(productApi, times(1)).add(newProduct);

        verify(inventoryApi, never()).add(any(Inventory.class));
    }

    @Test
    @DisplayName("add() should throw ApiException if inventoryApi.add fails")
    public void add_inventoryApiFails_shouldStillAddProduct() throws ApiException {
        // Given
        Product newProduct = ProductFactory.mockNewObject("Test Product", "barcode123", 100.0);
        newProduct.setClientId(1);
        Client existingClient = ClientFactory.mockPersistedObject(1, "Test Client");

        when(clientApi.getById(1)).thenReturn(existingClient);

        doAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(99);
            return null;
        }).when(productApi).add(newProduct);

        doThrow(new ApiException("Inventory already exists for this product"))
                .when(inventoryApi).add(any(Inventory.class));

        ApiException e = assertThrows(ApiException.class, () -> {
            productFlow.add(newProduct);
        });
        assertEquals("Inventory already exists for this product", e.getMessage());

        verify(clientApi, times(1)).getById(1);
        verify(productApi, times(1)).add(newProduct);
        verify(inventoryApi, times(1)).add(any(Inventory.class));
    }
}

