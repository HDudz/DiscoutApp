package com.hdudz.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PaymentMethod {
    private final String id;
    private final BigDecimal discountPercent; // Przechowywane jako 0-100
    private BigDecimal currentLimit; // Mutable

    public static final String POINTS_ID = "PUNKTY";

    public PaymentMethod(String id, int discountPercent, BigDecimal initialLimit) {
        this.id = id;
        this.discountPercent = new BigDecimal(discountPercent);
        this.currentLimit = initialLimit;
    }

    public String getId() { return id; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getCurrentLimit() { return currentLimit; }
    public void setCurrentLimit(BigDecimal currentLimit) { this.currentLimit = currentLimit; }

    public void decreaseLimit(BigDecimal amount) {
        this.currentLimit = this.currentLimit.subtract(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}