package com.increff.pos.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.OrderApi;
import com.increff.pos.service.OrderItemApi;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType; // For setting the content type to JSON
import org.apache.http.entity.StringEntity; // For wrapping the JSON payload
import org.apache.http.impl.client.CloseableHttpClient; // For the client object
import org.apache.http.impl.client.HttpClients; // For creating the default client
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class OrderFlow {

    @Autowired
    OrderItemApi orderItemApi;

    @Autowired
    OrderApi orderApi;

    @Autowired
    InventoryApi inventoryApi;

    @Autowired
    ObjectMapper objectMapper;


    public InvoiceData finalizeOrder(Integer orderId) throws ApiException {
        Orders order = orderApi.getById(orderId);
        List<OrderItem> items = orderItemApi.getAllByOrderId(orderId);
        System.out.println(items);

        InvoiceRequest requestPayload = new InvoiceRequest();
        requestPayload.setOrder(order);
        requestPayload.setOrderItems(items);

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(requestPayload);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize invoice data: " + e.getMessage());
        }

        String invoiceAppUrl = "http://localhost:9090/invoice/api/invoice/generate";

        InvoiceData invoiceData;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(invoiceAppUrl);
            post.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {

                String responseBody = EntityUtils.toString(response.getEntity());

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new ApiException("Invoice generation failed on service: " + responseBody);
                }

                invoiceData = objectMapper.readValue(responseBody, InvoiceData.class);

            }
        } catch (IOException e) {
            throw new ApiException("Failed to connect to Invoice Service: " + e.getMessage());
        }
        orderApi.updateStatusToInvoiced(orderId);
        return invoiceData;
    }

    public void cancelOrder(Integer orderId) throws ApiException{
        List<OrderItem> listItems = orderItemApi.getAllByOrderId(orderId);
        for(OrderItem orderItem : listItems) {
            Integer quantity = orderItem.getQuantity();
            Inventory inventory = inventoryApi.findByProductId(orderItem.getProductId());
            inventory.setQuantity(inventory.getQuantity() + quantity);
        }
        orderApi.cancelOrder(orderId);
    }

}
