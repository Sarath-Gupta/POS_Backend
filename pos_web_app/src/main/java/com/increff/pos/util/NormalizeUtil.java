package com.increff.pos.util;

import com.increff.pos.model.form.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


public class NormalizeUtil {

    public static void normalize(ClientForm clientForm) {
        clientForm.setClientName(clientForm.getClientName().trim().toLowerCase());
    }

    public static void normalize(ProductForm productForm) {
        productForm.setBarcode(productForm.getBarcode().trim().toLowerCase());
        productForm.setName(productForm.getName().trim().toLowerCase());
        productForm.setImgUrl(productForm.getImgUrl().trim().toLowerCase());
    }

    public static void normalize(ProductUpdateForm productForm) {
        productForm.setImgUrl(productForm.getImgUrl().trim().toLowerCase());
    }

    public static void normalize(UserForm userForm) {
        userForm.setEmail(userForm.getEmail().trim().toLowerCase());
        userForm.setPassword(userForm.getPassword().trim());
    }

    public static void normalize(List<OrderItemForm> orderItemList) {
        for(OrderItemForm orderItemForm : orderItemList) {
            orderItemForm.setBarcode(orderItemForm.getBarcode().trim().toLowerCase());
        }
    }

}
