package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<InventoryData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {

        Pageable pageable = PageRequest.of(page, size);
        return inventoryDto.getAll(pageable);
    }
}
