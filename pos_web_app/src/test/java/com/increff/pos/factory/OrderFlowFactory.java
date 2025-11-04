package com.increff.pos.factory;

import com.increff.pos.commons.OrderStatus;
import com.increff.pos.entity.Inventory;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.Product;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.OrderData;
import java.time.ZonedDateTime;

public class OrderFlowFactory {

    public static Orders mockOrder(Integer id, OrderStatus status) {
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    public static OrderData mockPersistedOrderData(Integer id) {
        OrderData orderData = new OrderData();
        orderData.setId(id);
        orderData.setCreatedAt(ZonedDateTime.now());
        return orderData;
    }

    public static OrderItem mockOrderItem(Integer id, Integer orderId, Integer productId, int quantity, double sellingPrice) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(sellingPrice);
        return item;
    }

    public static Product mockProduct(Integer id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setBarcode("barcode-" + id);
        p.setMrp(100.0);
        return p;
    }

    public static Inventory mockInventory(Integer id, Integer productId, int quantity) {
        Inventory inv = new Inventory();
        inv.setId(id);
        inv.setProductId(productId);
        inv.setQuantity(quantity);
        return inv;
    }

    public static InvoiceData mockInvoiceData(Integer orderId, String base64Pdf) {
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setBase64Pdf(base64Pdf);
        return invoiceData;
    }
}