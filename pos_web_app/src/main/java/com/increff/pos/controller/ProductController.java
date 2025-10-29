package com.increff.pos.controller;

import com.increff.pos.commons.ApiException;
import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductUpdateForm;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {
    @Autowired
    ProductDto productDto;

    @RequestMapping(method = RequestMethod.POST)
    public ProductData addProduct(@RequestBody ProductForm productForm) throws ApiException {
        return productDto.add(productForm);
    }

    @PostMapping(value = "/file/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Validate TSV file with products and return results with remarks")
    @ApiImplicitParam(name = "file", dataType = "file", paramType = "form", required = true, value = "TSV file to validate")
    public String addBulkProducts(@RequestParam("file") MultipartFile file) {
        String tsvContent;
        try {
            tsvContent = productDto.processTsvWithRemarks(file);
        } catch (ApiException e) {
            tsvContent = "barcode\tclientId\tname\tmrp\tRemarks\n" + "\t\t\t\t" + e.getMessage();
        }
        return tsvContent;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ProductData getById(@PathVariable(value = "id") Integer id) throws ApiException {
        return productDto.findById(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<ProductData> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {

        Pageable pageable = PageRequest.of(page, size);
        return productDto.getAll(pageable);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ProductData update(@PathVariable(value = "id") Integer id, @RequestBody ProductUpdateForm productForm) throws ApiException {
        return productDto.update(id, productForm);
    }
}
