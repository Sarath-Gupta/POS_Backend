package com.increff.pos.test;

import com.increff.pos.config.DbConfig;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.Client;
import com.increff.pos.entity.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DbConfig.class)
public class ProductDaoTest {

    @Autowired
    private ProductDao productRepository;

    @Autowired
    private ClientDao clientRepository;

    private Client createClient(String clientName) {
        Client client = new Client();
        client.setClientName(clientName);
        clientRepository.add(client);
        return client;
    }

    @Test
    @Transactional
    public void testInsert() {
        Client client = createClient("client1");
        Product product = new Product();
        product.setBarcode("barcode1");
        product.setClientId(client.getId());
        product.setName("product1");
        product.setMrp(10.0);
        product.setImgUrl("url1");
        productRepository.add(product);
        assertNotNull(product.getId());
    }

    @Test
    @Transactional
    public void testFindByBarcode() {
        Client client = createClient("client2");
        Product product = new Product();
        product.setBarcode("barcode2");
        product.setClientId(client.getId());
        product.setName("product2");
        product.setMrp(20.0);
        product.setImgUrl("url2");
        productRepository.add(product);

        Product retrievedProduct = productRepository.findByBarcode("barcode2");
        assertNotNull(retrievedProduct);
        assertEquals("barcode2", retrievedProduct.getBarcode());
    }

    @Test
    @Transactional
    public void testFindByClientId() {
        Client client = createClient("client3");
        Product product1 = new Product();
        product1.setBarcode("barcode3");
        product1.setClientId(client.getId());
        product1.setName("product3");
        product1.setMrp(30.0);
        product1.setImgUrl("url3");
        productRepository.add(product1);

        Product product2 = new Product();
        product2.setBarcode("barcode4");
        product2.setClientId(client.getId());
        product2.setName("product4");
        product2.setMrp(40.0);
        product2.setImgUrl("url4");
        productRepository.add(product2);

        List<Product> products = productRepository.findByClientId(client.getId());
        assertEquals(2, products.size());
    }
}