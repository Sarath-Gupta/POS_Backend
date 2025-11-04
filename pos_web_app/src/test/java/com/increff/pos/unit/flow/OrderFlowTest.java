package com.increff.pos.unit.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.pos.commons.ApiException;
import com.increff.pos.commons.OrderStatus;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import com.increff.pos.factory.OrderFlowFactory; // Using the new factory
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;


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

    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private CloseableHttpResponse httpResponse;
    @Mock
    private HttpEntity httpEntity;
    @Mock
    private StatusLine statusLine;

    private MockedStatic<HttpClients> mockedHttpClients;
    private MockedStatic<EntityUtils> mockedEntityUtils;

    @InjectMocks
    private OrderFlow orderFlow;

    @BeforeEach
    public void setUp() {
        mockedHttpClients = mockStatic(HttpClients.class);
        mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
        mockedEntityUtils = mockStatic(EntityUtils.class);
    }

    @AfterEach
    public void tearDown() {
        mockedHttpClients.close();
        mockedEntityUtils.close();
    }


    @Test
    @DisplayName("finalizeOrder() success should generate invoice")
    public void finalizeOrder_success_shouldReturnInvoiceData() throws Exception {
        Integer orderId = 1;
        String mockJsonPayload = "{\"orderData\":{...}}";
        String mockJsonResponse = "{\"orderId\":1,\"base64Pdf\":\"PDF...\"}";
        String mockBase64Pdf = "PDF...";

        Orders mockOrder = OrderFlowFactory.mockOrder(orderId, OrderStatus.CREATED);
        OrderData mockOrderData = OrderFlowFactory.mockPersistedOrderData(orderId);
        List<OrderItem> mockItems = Arrays.asList(
                OrderFlowFactory.mockOrderItem(1, orderId, 101, 2, 50.0),
                OrderFlowFactory.mockOrderItem(2, orderId, 102, 1, 100.0)
        );
        Product mockProduct1 = OrderFlowFactory.mockProduct(101, "Product A");
        Product mockProduct2 = OrderFlowFactory.mockProduct(102, "Product B");
        InvoiceData mockInvoiceData = OrderFlowFactory.mockInvoiceData(orderId, mockBase64Pdf);

        when(orderApi.getById(orderId)).thenReturn(mockOrder);
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(mockOrderData);
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(mockItems);
        when(productApi.findById(101)).thenReturn(mockProduct1);
        when(productApi.findById(102)).thenReturn(mockProduct2);

        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn(mockJsonPayload);

        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedEntityUtils.when(() -> EntityUtils.toString(httpEntity)).thenReturn(mockJsonResponse);
        when(objectMapper.readValue(mockJsonResponse, InvoiceData.class)).thenReturn(mockInvoiceData);

        InvoiceData result = orderFlow.finalizeOrder(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(mockBase64Pdf, result.getBase64Pdf());

        verify(orderApi, times(1)).updateStatusToInvoiced(orderId);
    }

    @Test
    @DisplayName("finalizeOrder() should throw exception for cancelled order")
    public void finalizeOrder_cancelledOrder_shouldThrowApiException() throws ApiException{
        Integer orderId = 1;
        Orders mockOrder = OrderFlowFactory.mockOrder(orderId, OrderStatus.CANCELLED);
        when(orderApi.getById(orderId)).thenReturn(mockOrder);

        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.finalizeOrder(orderId);
        });
        assertEquals("Cancelled orders cannot be invoiced", e.getMessage());
        verify(orderApi, never()).updateStatusToInvoiced(anyInt());
    }

    @Test
    @DisplayName("finalizeOrder() should throw exception if invoice service fails (500)")
    public void finalizeOrder_invoiceServiceReturns500_shouldThrowApiException() throws Exception {
        Integer orderId = 1;
        when(orderApi.getById(orderId)).thenReturn(OrderFlowFactory.mockOrder(orderId, OrderStatus.CREATED));
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(OrderFlowFactory.mockPersistedOrderData(orderId));
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(Arrays.asList(OrderFlowFactory.mockOrderItem(1, orderId, 101, 1, 50.0)));
        when(productApi.findById(anyInt())).thenReturn(OrderFlowFactory.mockProduct(101, "Product A"));
        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn("{}");

        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(500); // 500 Server Error
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        mockedEntityUtils.when(() -> EntityUtils.toString(httpEntity)).thenReturn("Internal Server Error");

        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.finalizeOrder(orderId);
        });

        assertTrue(e.getMessage().contains("Invoice generation failed on service: Internal Server Error"));
        verify(orderApi, never()).updateStatusToInvoiced(anyInt());
    }

    @Test
    @DisplayName("finalizeOrder() should throw exception if connection fails (IOException)")
    public void finalizeOrder_ioException_shouldThrowApiException() throws Exception {
        Integer orderId = 1;
        when(orderApi.getById(orderId)).thenReturn(OrderFlowFactory.mockOrder(orderId, OrderStatus.CREATED));
        when(mapper.convert(any(Orders.class), eq(OrderData.class))).thenReturn(OrderFlowFactory.mockPersistedOrderData(orderId));
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(Arrays.asList(OrderFlowFactory.mockOrderItem(1, orderId, 101, 1, 50.0)));
        when(productApi.findById(anyInt())).thenReturn(OrderFlowFactory.mockProduct(101, "Test Product"));
        when(objectMapper.writeValueAsString(any(InvoiceRequest.class))).thenReturn("{}");

        when(httpClient.execute(any(HttpPost.class))).thenThrow(new IOException("Connection refused"));

        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.finalizeOrder(orderId);
        });

        assertTrue(e.getMessage().contains("Failed to connect to Invoice Service: Connection refused"));
        verify(orderApi, never()).updateStatusToInvoiced(anyInt());
    }



    @Test
    @DisplayName("cancelOrder() success should restock inventory")
    public void cancelOrder_success_shouldRestockInventory() throws ApiException {
        Integer orderId = 1;
        OrderItem mockItem = OrderFlowFactory.mockOrderItem(1, orderId, 10, 5, 20.0);
        Inventory mockInventory = OrderFlowFactory.mockInventory(1, 10, 100);

        when(orderApi.getById(orderId)).thenReturn(OrderFlowFactory.mockOrder(orderId, OrderStatus.CREATED));

        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(Arrays.asList(mockItem));
        when(inventoryApi.findByProductId(10)).thenReturn(mockInventory);

        orderFlow.cancelOrder(orderId);

        verify(inventoryApi, times(1)).findByProductId(10);

        assertEquals(105, mockInventory.getQuantity());

        verify(orderApi, times(1)).cancelOrder(orderId);
    }

    @Test
    @DisplayName("cancelOrder() should throw exception for invoiced order")
    public void cancelOrder_invoicedOrder_shouldThrowApiException() throws ApiException{
        Integer orderId = 1;
        Orders mockOrder = OrderFlowFactory.mockOrder(orderId, OrderStatus.INVOICED);
        when(orderApi.getById(orderId)).thenReturn(mockOrder);

        ApiException e = assertThrows(ApiException.class, () -> {
            orderFlow.cancelOrder(orderId);
        });

        assertEquals("Invoiced orders cannot be cancelled", e.getMessage());
        verify(orderApi, never()).cancelOrder(anyInt());
        verify(orderItemApi, never()).getAllByOrderId(anyInt());
    }

    @Test
    @DisplayName("cancelOrder() should throw exception if inventory is not found (NPE)")
    public void cancelOrder_inventoryNotFound_shouldThrowNPE() throws ApiException {
        Integer orderId = 1;
        OrderItem mockItem = OrderFlowFactory.mockOrderItem(1, orderId, 10, 5, 20.0);

        when(orderApi.getById(orderId)).thenReturn(OrderFlowFactory.mockOrder(orderId, OrderStatus.CREATED));
        when(orderItemApi.getAllByOrderId(orderId)).thenReturn(Arrays.asList(mockItem));

        when(inventoryApi.findByProductId(10)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            orderFlow.cancelOrder(orderId);
        });

        verify(orderApi, never()).cancelOrder(anyInt());
    }
}