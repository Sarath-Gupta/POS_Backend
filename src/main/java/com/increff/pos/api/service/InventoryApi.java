package com.increff.pos.api.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional
    public void add(Inventory inventory) throws ApiException {
        Inventory existing = inventoryDao.findByProductId(inventory.getProductId());
        AbstractApi.ifExists(existing);
        inventoryDao.add(inventory);
    }

    public Inventory findById(Integer id) throws ApiException {
        Inventory inventory = inventoryDao.findById(id);
        AbstractApi.ifNotExists(inventory);
        return inventory;
    }

    public Inventory findByProductId(Integer productId) throws ApiException {
        Inventory inventory = inventoryDao.findByProductId(productId);
        AbstractApi.ifNotExists(inventory);
        return inventory;
    }

    public List<Inventory> getAll() {
        return inventoryDao.findAll();
    }

    @Transactional
    public Inventory update(Integer id, Inventory updatedInventory) throws ApiException {
        Inventory existing = inventoryDao.findById(id);
        AbstractApi.ifNotExists(existing);
        existing.setQuantity(updatedInventory.getQuantity());
        return existing;
    }


}
