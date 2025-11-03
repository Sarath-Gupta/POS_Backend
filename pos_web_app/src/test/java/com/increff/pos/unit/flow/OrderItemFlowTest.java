package com.increff.pos.unit.flow;

import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import com.increff.pos.factory.*;
import com.increff.pos.flow.OrderItemFlow;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.OrderApi;
import com.increff.pos.service.OrderItemApi;
import com.increff.pos.service.ProductApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OrderItemFlow class.
 * This class mocks all API dependencies to isolate and test the flow logic.
 */
@ExtendWith(MockitoExtension.class)
public class OrderItemFlowTest {

    @Mock
    private ProductApi productApi;
    @Mock
    private OrderItemApi orderItemApi;
    @Mock
    private OrderApi orderApi;
    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private OrderItemFlow orderItemFlow;

    private Product mockProduct;
    private Orders mockOrder;

    @BeforeEach
    public void setUp() {
        // Create common mock objects used in multiple tests
        mockProduct = ProductFactory.mockPersistedObject(1, "Test Product", "barcode123", 100.0);
        mockOrder = OrderFactory.mockPersistedObject(1);
    }

    // --- convert() Tests ---

    @Test
    @DisplayName("convert() should successfully convert form to POJO list")
    public void convert_success_shouldReturnOrderItemList() throws ApiException {
        // Given
        OrderItemForm form = OrderItemFlowFactory.mockOrderItemForm("barcode123", 5, 90.0);
        when(productApi.findByBarcode("barcode123")).thenReturn(mockProduct);

        // When
        List<OrderItem> resultList = orderItemFlow.convert(Arrays.asList(form));

        // Then
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        OrderItem resultItem = resultList.get(0);
        assertEquals(mockProduct.getId(), resultItem.getProductId());
        assertEquals(5, resultItem.getQuantity());
        assertEquals(90.0, resultItem.getSellingPrice());
        verify(productApi, times(1)).findByBarcode("barcode123");
    }

    @Test
    @DisplayName("convert() should throw NullPointerException if product not found")
    public void convert_productNotFound_shouldThrowNullPointerException() throws ApiException {
        // Given
        // This test finds a bug: The code doesn't check if product is null
        OrderItemForm form = OrderItemFlowFactory.mockOrderItemForm("fake-barcode", 5, 90.0);
        when(productApi.findByBarcode("fake-barcode")).thenReturn(null);

        // When & Then
        // The method will fail on `product.getId()`
        assertThrows(NullPointerException.class, () -> {
            orderItemFlow.convert(Arrays.asList(form));
        });

        verify(productApi, times(1)).findByBarcode("fake-barcode");
    }

    // --- add() Tests ---

    @Test
    @DisplayName("add() should succeed on happy path")
    public void add_success_shouldCreateOrderAndReduceStock() throws ApiException {
        // Given
        // FIX: Pass null for orderId, as required by 4-arg method
        OrderItem item1 = OrderItemFactory.mockNewObject(null, 10, 2, 80.0); // Product 10, Qty 2, Price 80
        OrderItem item2 = OrderItemFactory.mockNewObject(null, 20, 1, 50.0); // Product 20, Qty 1, Price 50
        List<OrderItem> itemList = Arrays.asList(item1, item2);

        Product product1 = ProductFactory.mockPersistedObject(10, "Product A", "barcodeA", 100.0);
        Product product2 = ProductFactory.mockPersistedObject(20, "Product B", "barcodeB", 100.0);

        // Mock validation and stock checks (first loop)
        when(productApi.findById(10)).thenReturn(product1);
        when(productApi.findById(20)).thenReturn(product2);
        // inventoryApi.checkStock() will pass (no exception)

        // Mock order creation
        // We need to use doAnswer to simulate the order getting an ID after being added
        doAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setId(1); // Set the generated ID
            return null;
        }).when(orderApi).add(any(Orders.class));

        when(orderApi.getById(1)).thenReturn(mockOrder);

        // When
        orderItemFlow.add(itemList);

        // Then
        // Verify validation and stock checks
        verify(productApi, times(1)).findById(10);
        verify(productApi, times(1)).findById(20);
        verify(inventoryApi, times(1)).checkStock(10, 2);
        verify(inventoryApi, times(1)).checkStock(20, 1);

        // Verify order creation
        verify(orderApi, times(1)).add(any(Orders.class));
        verify(orderApi, times(1)).getById(1);

        // Verify second loop (adding items, reducing stock)
        verify(orderItemApi, times(1)).add(item1);
        verify(orderItemApi, times(1)).add(item2);
        assertEquals(1, item1.getOrderId()); // Check orderId was set
        assertEquals(1, item2.getOrderId());
        verify(inventoryApi, times(1)).reduceStock(10, 2);
        verify(inventoryApi, times(1)).reduceStock(20, 1);

        // Verify final grand total update
        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderApi, times(1)).update(orderCaptor.capture());
        Double expectedTotal = (2 * 80.0) + (1 * 50.0); // 160 + 50 = 210
        assertEquals(expectedTotal, orderCaptor.getValue().getTotal_amount());
    }

    @Test
    @DisplayName("add() should throw ApiException if validation fails (price > mrp)")
    public void add_validationFails_shouldThrowApiException() throws ApiException {
        // Given
        // FIX: Pass null for orderId, as required by 4-arg method
        OrderItem item = OrderItemFactory.mockNewObject(null, 10, 2, 150.0); // Price 150
        List<OrderItem> itemList = Arrays.asList(item);

        Product product = ProductFactory.mockPersistedObject(10, "Product A", "barcodeA", 100.0); // MRP 100

        // Mock validation
        when(productApi.findById(10)).thenReturn(product);

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            orderItemFlow.add(itemList);
        });

        assertEquals("Selling Price cannot be greater than MRP", e.getMessage());

        // Verify that no further methods were called
        verify(inventoryApi, never()).checkStock(anyInt(), anyInt());
        verify(orderApi, never()).add(any(Orders.class));
        verify(orderItemApi, never()).add(any(OrderItem.class));
    }

    @Test
    @DisplayName("add() should throw ApiException if checkStock fails")
    public void add_checkStockFails_shouldThrowApiException() throws ApiException {
        // Given
        // FIX: Pass null for orderId, as required by 4-arg method
        OrderItem item = OrderItemFactory.mockNewObject(null, 10, 5, 80.0); // Requesting 5
        List<OrderItem> itemList = Arrays.asList(item);

        Product product = ProductFactory.mockPersistedObject(10, "Product A", "barcodeA", 100.0);

        when(productApi.findById(10)).thenReturn(product);
        // Mock inventory check to fail
        doThrow(new ApiException("Insufficient Quantity")).when(inventoryApi).checkStock(10, 5);

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            orderItemFlow.add(itemList);
        });

        assertEquals("Insufficient Quantity", e.getMessage());

        // Verify that no order was created
        verify(orderApi, never()).add(any(Orders.class));
        verify(orderItemApi, never()).add(any(OrderItem.class));
    }
}

