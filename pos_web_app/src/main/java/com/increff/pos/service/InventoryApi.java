package com.increff.pos.service;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryApi extends AbstractApi<Inventory> {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional
    public void add(Inventory inventory) throws ApiException {
        Inventory existing = inventoryDao.findByProductId(inventory.getProductId());
        ifExists(existing);
        inventoryDao.add(inventory);
    }

    public Inventory findById(Integer id) throws ApiException {
        Inventory inventory = inventoryDao.findById(id);
        ifNotExists(inventory);
        return inventory;
    }

    public Inventory findByProductId(Integer productId) throws ApiException {
        Inventory inventory = inventoryDao.findByProductId(productId);
        ifNotExists(inventory);
        return inventory;
    }

    public List<Inventory> getAll() {
        return inventoryDao.getAll();
    }

    @Transactional
    public Inventory update(Integer id, Inventory updatedInventory) throws ApiException {
        Inventory existing = inventoryDao.findById(id);
        ifNotExists(existing);
        existing.setQuantity(updatedInventory.getQuantity());
        return existing;
    }


    public void checkStock(Integer productId, Integer quantityToBeReduced) throws ApiException {
        Inventory existing = inventoryDao.findByProductId(productId);
        if(existing.getQuantity() < quantityToBeReduced) {
            throw new ApiException("Insufficient Quantity");
        }
    }

    public void reduceStock(Integer productId, Integer quantityToBeReduced) throws ApiException{
        Inventory existing = inventoryDao.findByProductId(productId);
        existing.setQuantity(existing.getQuantity() - quantityToBeReduced);
        if(existing.getQuantity() < 0) {
            throw new ApiException("Item out of Stock");
        }
    }


}
