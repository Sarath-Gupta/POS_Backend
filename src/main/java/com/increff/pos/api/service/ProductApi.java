package com.increff.pos.api.service;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class ProductApi extends AbstractApi<Product>{

    @Autowired
    ProductDao productDao;

    @Transactional
    public void add(Product product) throws ApiException {
        Product exsistingProduct = productDao.findByName(product.getName());
        ifExists(exsistingProduct);
        productDao.add(product);
    }

    public Product findById(Integer id) throws ApiException {
        Product product = productDao.findById(id);
        ifNotExists(product);
        return product;
    }

    public List<Product> getAll() {
        return productDao.findAll();
    }

    @Transactional
    public Product update(Integer id, Product product) throws ApiException{
        Product oldProduct = productDao.findById(id);
        ifNotExists(oldProduct);
        Product sameName = productDao.findByName(product.getName());
        if(!Objects.isNull(sameName)) {
            throw new ApiException("Product with same name already exists");
        }
        oldProduct.setName(product.getName());
        oldProduct.setImgUrl(product.getImgUrl());
        oldProduct.setMrp(product.getMrp());
        productDao.update(oldProduct);
        return oldProduct;
    }





}
