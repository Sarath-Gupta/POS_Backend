package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class OrderItemForm {
    @NotNull(message = "Product ID cannot be empty")
    @Positive(message = "Product ID must be positive")
    private Integer productId;

    @NotNull(message = "Quantity cannot be empty")
    @Max(value = 250, message = "Quantity cannot exceed 250")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @Max(value = 100000, message = "Selling Price cannot exceed one lakh")
    @Positive(message = "Selling Price must be positive")
    private Double sellingPrice;
}
