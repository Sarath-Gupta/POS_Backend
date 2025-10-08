package com.increff.pos.controller;

import com.google.protobuf.Api;
import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductDto productDto;

    @RequestMapping(method = RequestMethod.POST)
    public ProductData addProduct(@RequestBody ProductForm productForm) throws ApiException {
        return productDto.add(productForm);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ProductData getById(@PathVariable(value = "id") Integer id) throws ApiException {
        return productDto.findById(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ProductData> getAll() {
        return productDto.getAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ProductData update(@PathVariable(value = "id") Integer id, @RequestBody ProductForm productForm) throws ApiException {
        return productDto.update(id, productForm);
    }
}
