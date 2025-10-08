package com.increff.pos.dto;

import com.increff.pos.api.service.ProductApi;
import com.increff.pos.api.flow.ProductFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.entity.Product;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.NormalizeUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
