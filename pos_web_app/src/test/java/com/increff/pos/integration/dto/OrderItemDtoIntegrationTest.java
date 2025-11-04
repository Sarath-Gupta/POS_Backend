package com.increff.pos.integration.dto;

import com.increff.pos.commons.ApiException;
import com.increff.pos.config.SpringConfig;
import com.increff.pos.dto.OrderItemDto;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.Product;
import com.increff.pos.flow.OrderItemFlow;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.service.ClientApi;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.service.OrderItemApi;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
@Transactional
public class OrderItemDtoIntegrationTest {

    @Autowired
    private OrderItemDto orderItemDto;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private OrderItemFlow orderItemFlow;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    private OrderItemForm createOrderItemForm(String barcode, Integer quantity, Double price) {
        OrderItemForm form = new OrderItemForm();
        form.setBarcode(barcode);
        form.setQuantity(quantity);
        form.setSellingPrice(price);
        return form;
    }

    @BeforeEach
    public void setup() throws ApiException{
        Client client = new Client();
        client.setClientName("ordersdto-client");
        clientApi.add(client);

        Product product = new Product();
        product.setBarcode("hello");
        product.setClientId(client.getId());
        product.setName("Order Product");
        product.setMrp(1000.00);
        productApi.add(product);

        Product productNew = productApi.findByBarcode(product.getBarcode());

        Inventory inventory = new Inventory();
        inventory.setProductId(productNew.getId());
        inventory.setQuantity(1000);
        inventoryApi.add(inventory);

    }


    @Test
    public void testAddOrderItem_Success() throws ApiException {
        OrderItemForm form = createOrderItemForm("hello", 10, 120.0);
        List<OrderItemData> addedItems = orderItemDto.add(Arrays.asList(form));
        assertNotNull(addedItems);
        assertEquals(1, addedItems.size());
        OrderItemData added = addedItems.get(0);
        assertEquals("hello", added.getBarcode());
        assertEquals(10, added.getQuantity());
        assertEquals(120.0, added.getSellingPrice());
    }

    @Test
    public void testAddOrderItem_InvalidQuantity_ShouldThrow() {
        OrderItemForm invalidForm = createOrderItemForm("hello", -10, 120.0);

        ApiException e = assertThrows(ApiException.class, () -> orderItemDto.add(Arrays.asList(invalidForm)));
        assertTrue(e.getMessage().toLowerCase().contains("Quantity"));
    }

    @Test
    public void testGetAll_ReturnsPagedResults() throws ApiException {
        orderItemDto.add(Arrays.asList(
                createOrderItemForm("hello",5, 100.0),
                createOrderItemForm("hello",3, 200.0)
        ));

        Page<OrderItemData> resultPage = orderItemDto.getAll(PageRequest.of(0, 10));

        assertNotNull(resultPage);
        assertTrue(resultPage.getTotalElements() >= 2);

        OrderItemData first = resultPage.getContent().get(0);
        assertNotNull(first.getOrderId());
        assertNotNull(first.getProductId());
    }

    @Test
    public void testGetById_ExistingOrderItem() throws ApiException {
        List<OrderItemData> addedList = orderItemDto.add(Arrays.asList(createOrderItemForm("hello", 2, 75.0)));
        OrderItemData added = addedList.get(0);

        OrderItemData found = orderItemDto.getById(added.getId());

        assertNotNull(found);
        assertEquals(added.getId(), found.getId());
        assertEquals(2, found.getOrderId());
        assertEquals(2001, found.getProductId());
    }

    @Test
    public void testGetById_NonExisting_ShouldThrow() {
        ApiException e = assertThrows(ApiException.class, () -> orderItemDto.getById(9999));
        assertTrue(e.getMessage().toLowerCase().contains("not found"));
    }


    @Test
    public void testUpdateOrderItem_Success() throws ApiException {
        List<OrderItemData> addedList = orderItemDto.add(Arrays.asList(createOrderItemForm("hello",  4, 150.0)));
        OrderItemData existing = addedList.get(0);

        OrderItemForm updateForm = createOrderItemForm("hello", 4, 175.0);

        OrderItemData updated = orderItemDto.update(existing.getId(), updateForm);

        assertNotNull(updated);
        assertEquals(existing.getId(), updated.getId());
        assertEquals(10, updated.getQuantity());
        assertEquals(175.0, updated.getSellingPrice());
    }

    @Test
    public void testUpdateOrderItem_InvalidData_ShouldThrow() throws ApiException {
        List<OrderItemData> addedList = orderItemDto.add(Arrays.asList(createOrderItemForm("hello", 5, 120.0)));
        OrderItemData existing = addedList.get(0);

        OrderItemForm invalidForm = createOrderItemForm("hello", 0, -10.0);

        ApiException e = assertThrows(ApiException.class, () ->
                orderItemDto.update(existing.getId(), invalidForm)
        );
        assertTrue(e.getMessage().toLowerCase().contains("invalid"));
    }
}
