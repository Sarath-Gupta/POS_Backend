package com.increff.pos.model.form;


public class TsvRowResultClient {
    public ClientForm form;
    public String remarks;
    public boolean isValid;

    public TsvRowResultClient(ClientForm form) {
        this.form = form;
        this.remarks = ""; // Blank for valid rows
        this.isValid = true;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
        this.isValid = false;
    }

    public boolean isValid() {
        return isValid;
    }
}