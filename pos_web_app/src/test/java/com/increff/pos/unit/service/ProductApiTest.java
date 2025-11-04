package com.increff.pos.unit.service;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Product;
import com.increff.pos.commons.ApiException;
import com.increff.pos.factory.ProductFactory;
import com.increff.pos.service.ProductApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductApiTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductApi productApi;


    @Test
    @DisplayName("add() should save product when name is unique")
    public void add_validProduct_shouldSucceed() throws ApiException {
        Product newProduct = ProductFactory.mockNewObject("new-product");
        when(productDao.findByName("new-product")).thenReturn(null);

        productApi.add(newProduct);

        verify(productDao, times(1)).findByName("new-product");
        verify(productDao, times(1)).add(newProduct);
    }

    @Test
    @DisplayName("add() should throw ApiException when name is duplicate")
    public void add_duplicateName_shouldThrowApiException() {
        Product newProduct = ProductFactory.mockNewObject("duplicate-name");
        Product existingProduct = ProductFactory.mockPersistedObject(1, "duplicate-name");
        when(productDao.findByName("duplicate-name")).thenReturn(existingProduct);

        Exception e = assertThrows(ApiException.class, () -> {
            productApi.add(newProduct);
        });

        verify(productDao, times(1)).findByName("duplicate-name");
        verify(productDao, never()).add(any(Product.class)); // Verify add was never called
    }

    @Test
    @DisplayName("add() should throw NullPointerException when product is null")
    public void add_nullProduct_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            productApi.add(null);
        });
        verify(productDao, never()).findByName(anyString());
        verify(productDao, never()).add(any(Product.class));
    }

    @Test
    @DisplayName("findById() should return product when ID exists")
    public void findById_existingId_shouldReturnProduct() throws ApiException {
        Integer productId = 1;
        Product expectedProduct = ProductFactory.mockPersistedObject(productId, "test-product");
        when(productDao.findById(productId)).thenReturn(expectedProduct);

        Product actualProduct = productApi.findById(productId);

        assertNotNull(actualProduct);
        assertEquals(productId, actualProduct.getId());
        verify(productDao, times(1)).findById(productId);
    }

    @Test
    @DisplayName("findById() should throw ApiException when ID does not exist")
    public void findById_nonExistentId_shouldThrowApiException() {
        Integer productId = 999;
        when(productDao.findById(productId)).thenReturn(null);
        Exception e = assertThrows(ApiException.class, () -> {
            productApi.findById(productId);
        });

        verify(productDao, times(1)).findById(productId);
    }


    @Test
    @DisplayName("getAll() should return a page of products")
    public void getAll_productsFound_shouldReturnPaginatedResult() {
        Pageable pageable = ProductFactory.createPageable();
        List<Product> productList = ProductFactory.createProductList();
        Page<Product> productPage = ProductFactory.createProductPage(productList, pageable);

        when(productDao.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productApi.getAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Product A", result.getContent().get(0).getName());
        verify(productDao, times(1)).findAll(pageable);
    }


    @Test
    @DisplayName("findByBarcode() should return product when barcode exists")
    public void findByBarcode_existingBarcode_shouldReturnProduct() throws ApiException{
        String barcode = "barcode_a";
        Product expectedProduct = ProductFactory.mockPersistedObject(1, "Product A", barcode, 10.99);
        when(productDao.findByBarcode(barcode)).thenReturn(expectedProduct);

        Product actualProduct = productApi.findByBarcode(barcode);

        assertNotNull(actualProduct);
        assertEquals(barcode, actualProduct.getBarcode());
        verify(productDao, times(1)).findByBarcode(barcode);
    }

    @Test
    @DisplayName("findByBarcode() should throw ApiException when barcode does not exist")
    public void findByBarcode_nonExistentBarcode_shouldThrowApiException() {
        String barcode = "fake-barcode";
        when(productDao.findByBarcode(barcode)).thenReturn(null);
        ApiException e = assertThrows(ApiException.class, () -> {
            productApi.findByBarcode(barcode);
        });
        assertEquals("Product doesn't exist", e.getMessage());
        verify(productDao, times(1)).findByBarcode(barcode);
    }


    @Test
    @DisplayName("update() should update correct fields and NOT update barcode")
    public void update_validUpdate_shouldSucceed() throws ApiException {
        Integer productId = 1;
        Product updateData = ProductFactory.mockNewObject("updated-name", "new-barcode-999", 99.99);
        Product oldProduct = ProductFactory.mockPersistedObject(1, "original-name", "old-barcode-123", 10.00);

        when(productDao.findById(productId)).thenReturn(oldProduct);
        when(productDao.findByName("updated-name")).thenReturn(null);

        Product updatedProduct = productApi.update(productId, updateData);

        assertNotNull(updatedProduct);

        assertEquals("updated-name", updatedProduct.getName());
        assertEquals(99.99, updatedProduct.getMrp());
        assertEquals(updateData.getImgUrl(), updatedProduct.getImgUrl());
        assertEquals("old-barcode-123", updatedProduct.getBarcode());

        verify(productDao, times(1)).findById(productId);
        verify(productDao, times(1)).findByName("updated-name");
        verify(productDao, times(1)).update(oldProduct);
    }

    @Test
    @DisplayName("update() should throw ApiException when product ID not found")
    public void update_productNotFound_shouldThrowApiException() {
        Integer productId = 999;
        Product updateData = ProductFactory.mockNewObject("updated-name");
        when(productDao.findById(productId)).thenReturn(null);
        assertThrows(ApiException.class, () -> {
            productApi.update(productId, updateData);
        });

        verify(productDao, times(1)).findById(productId);
        verify(productDao, never()).findByName(anyString());
        verify(productDao, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("update() should throw ApiException when new name duplicates another client")
    public void update_duplicateName_shouldThrowApiException() {
        Integer productId = 1;
        Product updateData = ProductFactory.mockNewObject("duplicate-name");
        Product oldProduct = ProductFactory.mockPersistedObject(productId, "original-name");

        Product duplicateProduct = ProductFactory.mockPersistedObject(2, "duplicate-name");

        when(productDao.findById(productId)).thenReturn(oldProduct);
        when(productDao.findByName("duplicate-name")).thenReturn(duplicateProduct);

        assertThrows(ApiException.class, () -> {
            productApi.update(productId, updateData);
        }, "Product with same name already exists");

        verify(productDao, times(1)).findById(productId);
        verify(productDao, times(1)).findByName("duplicate-name");
        verify(productDao, never()).update(any(Product.class)); // Update is never called
        // Verify original object was not mutated
        assertEquals("original-name", oldProduct.getName());
    }

}
