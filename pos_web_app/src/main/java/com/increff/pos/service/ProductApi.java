package com.increff.pos.service;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<Product> getAll(Pageable pageable) {
        return productDao.findAll(pageable);
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

    public Product findByBarcode(String barcode) throws ApiException{
        Product product = productDao.findByBarcode(barcode);
        ifNotExists(product);
        return product;
    }




}
