package com.increff.pos.dto;

import com.increff.pos.api.service.ProductApi;
import com.increff.pos.api.flow.ProductFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.entity.Product;
import com.increff.pos.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductDto {

    @Autowired
    ProductApi productApi;

    @Autowired
    ProductFlow productFlow;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;

    public ProductData add(ProductForm productForm) throws ApiException {
        NormalizeUtil.normalize(productForm);
        validationUtil.validate(productForm);
        Product pojo = mapper.convert(productForm, Product.class);
        productFlow.add(pojo);
        return mapper.convert(pojo, ProductData.class);
    }

    public List<ProductData> addFile(MultipartFile file) throws ApiException {
        List<ProductForm> productForms = ProductUtil.parseTSV(file);
        List<ProductData> addedProducts = new ArrayList<>();
        for (ProductForm productForm : productForms) {
            try {
                validationUtil.validate(productForm);
                NormalizeUtil.normalize(productForm);
                Product productPojo = mapper.convert(productForm, Product.class);
                productFlow.add(productPojo);
                addedProducts.add(mapper.convert(productPojo, ProductData.class));
            } catch (ApiException e) {
                System.out.println("Skipping invalid row: " + productForm + " Reason: " + e.getMessage());
            }
        }
        return addedProducts;
    }


    public ProductData findById(Integer id) throws ApiException {
        Product productPojo = productApi.findById(id);
        return mapper.convert(productPojo, ProductData.class);
    }

    public List<ProductData> getAll() {
        List<Product> productList = productApi.getAll();
        return mapper.convert(productList, ProductData.class);
    }

    public ProductData update(Integer id, ProductForm productForm) throws ApiException {
        NormalizeUtil.normalize(productForm);
        validationUtil.validate(productForm);
        Product pojo = mapper.convert(productForm, Product.class);
        productApi.update(id,pojo);
        return mapper.convert(pojo, ProductData.class);
    }
}
