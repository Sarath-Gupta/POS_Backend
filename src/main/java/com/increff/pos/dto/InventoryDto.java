package com.increff.pos.dto;

import com.increff.pos.api.service.InventoryApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.entity.Inventory;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryDto {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;

    public InventoryData update(Integer id, InventoryForm form) throws ApiException {
        validationUtil.validate(form);
        Inventory inventory = mapper.convert(form, Inventory.class);
        Inventory updatedInventory = inventoryApi.update(id, inventory);
        return mapper.convert(updatedInventory, InventoryData.class);
    }

    public InventoryData findById(Integer id) throws ApiException {
        Inventory inventory = inventoryApi.findById(id);
        return mapper.convert(inventory, InventoryData.class);
    }

    public List<InventoryData> getAll() {
        List<Inventory> inventoryList = inventoryApi.getAll();
        return mapper.convert(inventoryList, InventoryData.class);
    }
}
