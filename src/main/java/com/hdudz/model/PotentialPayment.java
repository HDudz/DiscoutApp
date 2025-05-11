package com.hdudz.model;


import java.math.BigDecimal;
import java.util.Objects;

public class PotentialPayment {
    private final Order order;
    private final PaymentStrategy strategy;
    private final BigDecimal discountAmount; //Kwota rabatu

    //Kwoty do zapłaty poszczególnymi metodami
    private final PaymentMethod primaryPaymentMethod; //Główna metoda platnosci
    private final BigDecimal primaryAmountPaid;

    private final PaymentMethod secondaryPaymentMethod; //Karta do dopłaty (opjonalna
    private final BigDecimal secondaryAmountPaid;


    public PotentialPayment(Order order, PaymentStrategy strategy, BigDecimal discountAmount,
                            PaymentMethod primaryPaymentMethod, BigDecimal primaryAmountPaid,
                            PaymentMethod secondaryPaymentMethod, BigDecimal secondaryAmountPaid) {
        this.order = Objects.requireNonNull(order);
        this.strategy = Objects.requireNonNull(strategy);
        this.discountAmount = Objects.requireNonNull(discountAmount);
        this.primaryPaymentMethod = Objects.requireNonNull(primaryPaymentMethod);
        this.primaryAmountPaid = Objects.requireNonNull(primaryAmountPaid);
        this.secondaryPaymentMethod = secondaryPaymentMethod; // nie daje requireNonNull bo moze byc null
        this.secondaryAmountPaid = (secondaryPaymentMethod == null) ? BigDecimal.ZERO : Objects.requireNonNull(secondaryAmountPaid);
    }

    public Order getOrder() { return order; }
    public PaymentStrategy getStrategy() { return strategy; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public PaymentMethod getPrimaryPaymentMethod() { return primaryPaymentMethod; }
    public BigDecimal getPrimaryAmountPaid() { return primaryAmountPaid; }
    public PaymentMethod getSecondaryPaymentMethod() { return secondaryPaymentMethod; }
    public BigDecimal getSecondaryAmountPaid() { return secondaryAmountPaid; }

    @Override
    public String toString() {
        return "PotentialPayment{" +
                "order=" + order.getId() +
                ", strategyType=" + strategy +
                ", discountAmount=" + discountAmount +
                ", primaryPM=" + primaryPaymentMethod.getId() +
                ", primaryAmount=" + primaryAmountPaid +
                (secondaryPaymentMethod != null ? ", secondaryPM=" + secondaryPaymentMethod.getId() : "") +
                (secondaryPaymentMethod != null ? ", secondaryAmount=" + secondaryAmountPaid : "") +
                '}';
    }
}