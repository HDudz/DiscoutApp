package com.hdudz.dto;

import java.math.BigDecimal;

public class PaymentDTO {
    private String id;
    private int discount; // Procent
    private BigDecimal limit;

    //Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    public BigDecimal getLimit() { return limit; }
    public void setLimit(BigDecimal limit) { this.limit = limit; }
}