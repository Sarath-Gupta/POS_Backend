package com.increff.pos.integration.dto;

import com.increff.pos.commons.ApiException;
import com.increff.pos.config.SpringConfig;
import com.increff.pos.dto.OrdersDto;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.Orders;
import com.increff.pos.flow.OrderItemFlow;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Product;
import com.increff.pos.service.ClientApi;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.OrderApi;
import com.increff.pos.service.ProductApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
@Transactional
public class OrdersDtoIntegrationTest {

    @Autowired
    private OrdersDto ordersDto;

    @Autowired
    private OrderItemFlow orderItemFlow;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private OrderApi orderApi;

    private Integer createdOrderId;

    @BeforeEach
    public void setup() throws ApiException {
        Client client = new Client();
        client.setClientName("ordersdto-client");
        clientApi.add(client);

        Product product = new Product();
        product.setBarcode("order-barcode-1");
        product.setClientId(client.getId());
        product.setName("Order Product");
        product.setMrp(120.0);
        productApi.add(product);

        Product productNew = productApi.findByBarcode(product.getBarcode());

        Inventory inventory = new Inventory();
        inventory.setProductId(productNew.getId());
        inventory.setQuantity(100);
        inventoryApi.add(inventory);

        OrderItemForm itemForm = new OrderItemForm();
        itemForm.setBarcode("order-barcode-1");
        itemForm.setQuantity(2);
        itemForm.setSellingPrice(100.0);

        orderItemFlow.add(orderItemFlow.convert(Collections.singletonList(itemForm)));

        List<Orders> orderIds = orderApi.findAll();
        assertTrue(orderIds.size() >= 1, "At least one order should exist after setup");
        createdOrderId = orderIds.get(orderIds.size() - 1).getId();
    }

    @Test
    public void testGetById_Success() throws ApiException {
        OrderData data = ordersDto.getById(createdOrderId);
        assertEquals(createdOrderId, data.getId());
        assertEquals("CREATED", data.getStatus());
    }

    @Test
    public void testGetById_NotFound_ShouldThrow() {
        ApiException e = assertThrows(ApiException.class, () -> ordersDto.getById(9999));
        assertEquals("Orders doesn't exist", e.getMessage());
    }


    @Test
    public void testFinalizeOrder_NotFound_ShouldThrow() {
        ApiException e = assertThrows(ApiException.class, () -> ordersDto.finalizeOrder(9999));
        assertEquals("Orders doesn't exist", e.getMessage());
    }

    @Test
    public void testCancelOrder_Success() throws ApiException {
        ordersDto.cancelOrder(createdOrderId);
        entityManager.flush();
        entityManager.clear();
        OrderData updated = ordersDto.getById(createdOrderId);
        assertEquals("CANCELLED", updated.getStatus());
    }

    @Test
    public void testCancelOrder_NotFound_ShouldThrow() {
        ApiException e = assertThrows(ApiException.class, () -> ordersDto.cancelOrder(9999));
        assertEquals("Orders doesn't exist", e.getMessage());
    }
}
