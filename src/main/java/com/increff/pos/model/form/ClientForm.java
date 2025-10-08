package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ClientForm {

    @NotNull(message = "Client Name cannot be empty")
    @Size(max = 255, message = "Client Name is too long.")
    private String clientName;
}
