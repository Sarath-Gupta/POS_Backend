package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import javax.validation.constraints.*;

@Getter
@Setter
public class ProductForm {

    @NotBlank(message = "Barcode is empty")
    @Size(max = 255, message = "Barcode is too long")
    private String barcode;

    @NotBlank(message = "Product Name is empty")
    @Size(max = 255, message = "Product Name is too long")
    private String name;

    @NotNull(message = "Client ID is empty")
    @Min(value = 1, message = "Client ID must be a positive number")
    private Integer clientId;

    @NotNull(message = "MRP is empty")
    @Positive(message = "MRP must be a positive value")
    private Double mrp;

    @Size(max = 255, message = "Image URL is too long")
    @URL(message = "Image URL is not valid")
    private String imgUrl;
}
