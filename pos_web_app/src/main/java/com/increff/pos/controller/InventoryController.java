package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public InventoryData updateInventory(@PathVariable("id") Integer id, @RequestBody InventoryForm form) throws ApiException {
        return inventoryDto.update(id, form);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public InventoryData getInventoryById(@PathVariable("id") Integer id) throws ApiException {
        return inventoryDto.findById(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<InventoryData> getAllInventory() {
        return inventoryDto.getAll();
    }
}
