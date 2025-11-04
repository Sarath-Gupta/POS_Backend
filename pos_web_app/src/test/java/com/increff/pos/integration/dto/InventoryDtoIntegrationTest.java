package com.increff.pos.integration.dto;

import com.increff.pos.commons.ApiException;
import com.increff.pos.config.SpringConfig;
import com.increff.pos.dto.InventoryDto;
import com.increff.pos.service.ClientApi;
import com.increff.pos.service.InventoryApi;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.Product;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.service.ProductApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringConfig.class})
@WebAppConfiguration
@Transactional
public class InventoryDtoIntegrationTest {

    @Autowired
    private InventoryDto inventoryDto;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;
    private Inventory testInventory;
    private Product testProduct;

    @BeforeEach
    public void setup() throws ApiException {
        Client client = new Client();
        client.setClientName("inventory-client");
        clientApi.add(client);

        Product product = new Product();
        product.setBarcode("inv-barcode-1");
        product.setClientId(client.getId());
        product.setName("Inventory Test Product");
        product.setMrp(150.0);
        productFlow.add(product);

        testProduct = productApi.findByBarcode("inv-barcode-1");

        testInventory = inventoryApi.findByProductId(product.getId());
        assertNotNull(testInventory);
        assertEquals(0, testInventory.getQuantity());
    }


    @Test
    public void testFindById_Success() throws ApiException {
        InventoryData data = inventoryDto.findById(testInventory.getId());

        assertNotNull(data);
        assertEquals(testInventory.getId(), data.getId());
        assertEquals(testProduct.getId(), data.getProductId());
        assertEquals(0, data.getQuantity());
    }

    @Test
    public void testFindById_NotFound_ShouldThrow() {
        ApiException e = assertThrows(ApiException.class, () -> inventoryDto.findById(9999));
        assertEquals("Inventory doesn't exist", e.getMessage());
    }

    @Test
    public void testUpdate_Success() throws ApiException {
        InventoryForm form = new InventoryForm();
        form.setQuantity(50);

        InventoryData updated = inventoryDto.update(testInventory.getId(), form);

        assertNotNull(updated);
        assertEquals(testInventory.getId(), updated.getId());
        assertEquals(50, updated.getQuantity());

        InventoryData fromDb = inventoryDto.findById(testInventory.getId());
        assertEquals(50, fromDb.getQuantity());
    }

    @Test
    public void testUpdate_NotFound_ShouldThrow() {
        InventoryForm form = new InventoryForm();
        form.setQuantity(25);

        ApiException e = assertThrows(ApiException.class, () -> inventoryDto.update(9999, form));
        assertEquals("Inventory doesn't exist", e.getMessage());
    }

    @Test
    public void testUpdate_NullQuantity_ShouldThrowValidationError() {
        InventoryForm form = new InventoryForm();
        form.setQuantity(null);

        Exception e = assertThrows(Exception.class, () -> inventoryDto.update(testInventory.getId(), form));
        assertTrue(e.getMessage().contains("Quantity cannot be empty"));
    }
}
