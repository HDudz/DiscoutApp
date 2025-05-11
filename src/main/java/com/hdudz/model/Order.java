package com.hdudz.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final String id;
    private final BigDecimal value;
    private final List<String> promotions;

    public Order(String id, BigDecimal value, List<String> promotions) {
        this.id = id;
        this.value = value;
        this.promotions = (promotions == null) ? Collections.emptyList() : Collections.unmodifiableList(promotions);
    }

    public String getId() { return id; }
    public BigDecimal getValue() { return value; }
    public List<String> getPromotions() { return promotions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', value=" + value + "}";
    }
}