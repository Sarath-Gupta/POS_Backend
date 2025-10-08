package com.increff.pos.api.flow;

import com.increff.pos.api.service.AbstractApi;
import com.increff.pos.api.service.ClientApi;
import com.increff.pos.api.service.InventoryApi;
import com.increff.pos.api.service.ProductApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductFlow {

    @Autowired
    ClientApi clientApi;

    @Autowired
    ProductApi productApi;

    @Autowired
    InventoryApi inventoryApi;

    public void add(Product product) throws ApiException {
        Client clientPojo = clientApi.getById(product.getClientId());
        AbstractApi.ifNotExists(clientPojo);
        productApi.add(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventoryApi.add(inventory);
    }
}
