package com.increff.pos.unit.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import com.increff.pos.factory.*;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.OrderApi;
import com.increff.pos.service.OrderItemApi;
import com.increff.pos.service.ProductApi;
import com.increff.pos.util.AbstractMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays; // Use Arrays.asList instead of List.of
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OrderFlow class.
 * This class mocks all API dependencies and the external HTTP client.
 */
@ExtendWith(MockitoExtension.class)
public class OrderFlowTest {

    @Mock
    private OrderItemApi orderItemApi;
    @Mock
    private OrderApi orderApi;
    @Mock
    private InventoryApi inventoryApi;
    @Mock
    private ProductApi productApi;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AbstractMapper mapper;

    // Mocks for the external HTTP call
    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private CloseableHttpResponse httpResponse;
    @Mock
    private HttpEntity httpEntity;
    @Mock
    private StatusLine statusLine;

    /**
     * This is a static mock for the HttpClients class.
     * It's necessary because `HttpClients.createDefault()` is a static method
     * called from within the finalizeOrder() method.
     */
    private MockedStatic<HttpClients> mockedHttpClients;
    private MockedStatic<EntityUtils> mockedEntityUtils;


    @InjectMocks
    private OrderFlow orderFlow;

    @BeforeEach
    public void setUp() {
        // Intercept the static method call and return our mock client
        mockedHttpClients = mockStatic(HttpClients.class);
        mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);

        // We also need to mock the static EntityUtils.toString() method
        mockedEntityUtils = mockStatic(EntityUtils.class);
    }

    @AfterEach
    public void tearDown() {
        // Close the static mocks after each test to prevent test pollution
        mockedHttpClients.close();
        mockedEntityUtils.close();
    }

    // --- finalizeOrder() Tests ---

    @Test
    @DisplayName("finalizeOrder() should successfully generate invoice")
    public void finalizeOrder_success_shouldReturnInvoiceData() throws Exception {
        // Given
        Integer orderId = 1;
        String mockJsonPayload = "{\"orderId\":1}";
        String mockJsonResponse = "{\"orderId\":1,\"base64Pdf\":\"PDF...\"}";
        String mockBase64Pdf = "PDF...";

        // 1. Setup mock data from factories
        Orders mockOrder = OrderFactory.mockPersistedObject(orderId);
        OrderData mockOrderData = OrderFlowFactory.mockPersistedOrderData(orderId);
        List<OrderItem> mockItems = Arrays.asList( // FIX: Use Arrays.asList for Java 8
                OrderItemFactory.mockPersistedObject(1, orderId, 10, 2, 50.0),
                OrderItemFactory.mockPersistedObject(2, orderId, 20, 1, 100.0)
        );
        Product mockProduct1 = ProductFactory.mockPersistedObject(10, "Product A"); // FIX: Use existing factory method
        Product mockProduct2 = ProductFactory.mockPersistedObject(20, "Product B"); // FIX: Use existing factory method

        InvoiceData mockInvoiceData = OrderFlowFactory.mockInvoiceData(orderId, mockBase64Pdf);

        // 2. Mock all API calls
        when(orderApi.getById(orderId)).thenReturn(mockOrder);
        // FIX: Be specific with mapper stub
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(mockOrderData);
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        when(productApi.findById(10)).thenReturn(mockProduct1);
        when(productApi.findById(20)).thenReturn(mockProduct2);

        // 3. Mock JSON serialization
        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn(mockJsonPayload);

        // 4. Mock the entire HTTP call chain
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedEntityUtils.when(() -> EntityUtils.toString(httpEntity)).thenReturn(mockJsonResponse);
        when(objectMapper.readValue(mockJsonResponse, InvoiceData.class)).thenReturn(mockInvoiceData);

        // When
        InvoiceData result = orderFlow.finalizeOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(mockBase64Pdf, result.getBase64Pdf());

        // Verify the final, critical step was called
        verify(orderApi, times(1)).updateStatusToInvoiced(orderId);
    }

    @Test
    @DisplayName("finalizeOrder() should throw ApiException if invoice service fails")
    public void finalizeOrder_invoiceServiceReturns500_shouldThrowApiException() throws Exception {
        // Given
        Integer orderId = 1;
        Orders mockOrder = OrderFactory.mockPersistedObject(orderId);
        // FIX: Use Arrays.asList for Java 8
        List<OrderItem> mockItems = Arrays.asList(OrderItemFactory.mockPersistedObject(1, orderId, 10, 1, 50.0));
        Product mockProduct1 = ProductFactory.mockPersistedObject(10, "Product A"); // FIX: Use existing factory method

        // Mock setup up to the HTTP call
        when(orderApi.getById(orderId)).thenReturn(mockOrder);
        // FIX: Be specific with mapper stub
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(OrderFlowFactory.mockPersistedOrderData(orderId));
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        when(productApi.findById(anyInt())).thenReturn(mockProduct1);
        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn("{}");

        // Mock the HTTP call failing
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(500); // 500 Server Error
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedEntityUtils.when(() -> EntityUtils.toString(httpEntity)).thenReturn("Internal Server Error");


        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.finalizeOrder(orderId);
        });

        assertTrue(e.getMessage().contains("Invoice generation failed on service: Internal Server Error"));

        // Verify the order status was NOT updated
        verify(orderApi, never()).updateStatusToInvoiced(anyInt());
    }

    @Test
    @DisplayName("finalizeOrder() should throw ApiException if connection fails")
    public void finalizeOrder_ioException_shouldThrowApiException() throws Exception {
        // Given
        Integer orderId = 1;
        // Mock setup up to the HTTP call
        when(orderApi.getById(orderId)).thenReturn(OrderFactory.mockPersistedObject(orderId));
        // FIX: Be specific with mapper stub
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(OrderFlowFactory.mockPersistedOrderData(orderId));
        // FIX: Use Arrays.asList for Java 8
        List<OrderItem> mockItems = Arrays.asList(OrderItemFactory.mockPersistedObject(1, orderId, 10, 1, 50.0));
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        when(productApi.findById(anyInt())).thenReturn(ProductFactory.mockPersistedObject(10, "Test Product")); // FIX: Use existing factory method
        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn("{}");

        // Mock the HTTP call throwing an IOException
        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection refused"));

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.finalizeOrder(orderId);
        });

        assertTrue(e.getMessage().contains("Failed to connect to Invoice Service: Connection refused"));

        // Verify the order status was NOT updated
        verify(orderApi, never()).updateStatusToInvoiced(anyInt());
    }


    // --- cancelOrder() Tests ---

    @Test
    @DisplayName("cancelOrder() should restock inventory and cancel order")
    public void cancelOrder_success_shouldRestockInventory() throws ApiException {
        // Given
        Integer orderId = 1;
        // Order item has quantity 5
        OrderItem mockItem = OrderItemFactory.mockPersistedObject(1, orderId, 10, 5, 20.0);
        List<OrderItem> mockItems = Arrays.asList(mockItem); // FIX: Use Arrays.asList for Java 8
        // Inventory has quantity 100
        Inventory mockInventory = InventoryFactory.mockPersistedObject(1, 10, 100);

        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        when(inventoryApi.findByProductId(10)).thenReturn(mockInventory);

        // When
        orderFlow.cancelOrder(orderId);

        // Then
        // 1. Verify inventory was found
        verify(inventoryApi, times(1)).findByProductId(10);

        // 2. Verify the quantity was restocked (100 + 5 = 105)
        assertEquals(105, mockInventory.getQuantity());

        // 3. Verify the order was cancelled
        verify(orderApi, times(1)).cancelOrder(orderId);
    }

    @Test
    @DisplayName("cancelOrder() should throw exception if inventory is not found")
    public void cancelOrder_inventoryNotFound_shouldThrowException() throws ApiException {
        // Given
        Integer orderId = 1;
        OrderItem mockItem = OrderItemFactory.mockPersistedObject(1, orderId, 10, 5, 20.0);
        List<OrderItem> mockItems = Arrays.asList(mockItem); // FIX: Use Arrays.asList for Java 8

        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        // Mock inventory service to throw an exception
        when(inventoryApi.findByProductId(10)).thenThrow(new ApiException("Inventory not found"));

        // When & Then
        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.cancelOrder(orderId);
        });

        assertEquals("Inventory not found", e.getMessage());

        // Verify the order was NOT cancelled
        verify(orderApi, never()).cancelOrder(anyInt());
    }
}

