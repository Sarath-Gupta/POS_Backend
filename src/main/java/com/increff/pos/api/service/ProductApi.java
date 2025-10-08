package com.increff.pos.api.service;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductApi {

    @Autowired
    ProductDao productDao;

    @Transactional
    public void add(Product product) throws ApiException {
        Product exsistingProduct = productDao.findByName(product.getName());
        AbstractApi.ifExists(exsistingProduct);
        productDao.add(product);
    }

    public Product findById(Integer id) throws ApiException {
        Product product = productDao.findById(id);
        AbstractApi.ifNotExists(product);
        return product;
    }

    public List<Product> getAll() {
        return productDao.findAll();
    }

    @Transactional
    public Product update(Integer id, Product product) throws ApiException{
        Product oldClient = productDao.findById(id);
        oldClient.setBarcode(product.getBarcode());
        oldClient.setName(product.getName());
        oldClient.setImgUrl(product.getImgUrl());
        oldClient.setClientId(product.getClientId());
        oldClient.setMrp(product.getMrp());
        productDao.update(oldClient);
        return product;
    }





}
