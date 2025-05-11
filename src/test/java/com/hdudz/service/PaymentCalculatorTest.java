package com.hdudz.service;

import com.hdudz.model.Order;
import com.hdudz.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentCalculatorTest {

    private PaymentCalculator paymentCalculator;

    @BeforeEach
     void setUp() {
        paymentCalculator = new PaymentCalculator();
    }

    @Test
    void testExampleScenario() {

        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("mZysk"));
        Order order2 = new Order("ORDER2", new BigDecimal("200.00"), Arrays.asList("BosBankrut"));
        Order order3 = new Order("ORDER3", new BigDecimal("150.00"), Arrays.asList("mZysk", "BosBankrut"));
        Order order4 = new Order("ORDER4", new BigDecimal("50.00"), null); // No promotions
        List<Order> orders = Arrays.asList(order1, order2, order3, order4);

        Map<String, PaymentMethod> paymentMethods = new HashMap<>();
        paymentMethods.put("PUNKTY", new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00")));
        paymentMethods.put("mZysk", new PaymentMethod("mZysk", 10, new BigDecimal("180.00")));
        paymentMethods.put("BosBankrut", new PaymentMethod("BosBankrut", 5, new BigDecimal("200.00")));

        Map<String, BigDecimal> result = paymentCalculator.calculateOptimalPayments(orders, paymentMethods);



        assertNotNull(result.get("mZysk"));
        assertNotNull(result.get("BosBankrut"));
        assertNotNull(result.get("PUNKTY"));


        assertEquals(0, new BigDecimal("165.00").compareTo(result.get("mZysk").setScale(2)));
        assertEquals(0, new BigDecimal("190.00").compareTo(result.get("BosBankrut").setScale(2)));
        assertEquals(0, new BigDecimal("100.00").compareTo(result.get("PUNKTY").setScale(2)));

    }




    @Test
    void testPartialPointsOverNoDiscount() {
        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), null);
        List<Order> orders = Arrays.asList(order1);

        Map<String, PaymentMethod> paymentMethods = new HashMap<>();

        paymentMethods.put("PUNKTY", new PaymentMethod("PUNKTY", 0, new BigDecimal("50.00")));
        paymentMethods.put("CardA", new PaymentMethod("CardA", 0, new BigDecimal("100.00")));


        Map<String, BigDecimal> result = paymentCalculator.calculateOptimalPayments(orders, paymentMethods);
        assertEquals(2, result.size());
        assertNotNull(result.get("PUNKTY"));
        assertNotNull(result.get("CardA"));
        assertEquals(0, new BigDecimal("50.00").compareTo(result.get("PUNKTY").setScale(2)));
        assertEquals(0, new BigDecimal("40.00").compareTo(result.get("CardA").setScale(2)));
    }


    @Test
    void testFullPointsOverLowerCard() {
        Order order1 = new Order("ORDER1", new BigDecimal("100.00"), Arrays.asList("LowCard"));
        List<Order> orders = Arrays.asList(order1);

        Map<String, PaymentMethod> paymentMethods = new HashMap<>();
        paymentMethods.put("PUNKTY", new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00"))); // 15% discount
        paymentMethods.put("LowCard", new PaymentMethod("LowCard", 5, new BigDecimal("100.00")));   // 5% discount

        Map<String, BigDecimal> result = paymentCalculator.calculateOptimalPayments(orders, paymentMethods);

        assertEquals(1, result.size());
        assertNotNull(result.get("PUNKTY"));
        assertEquals(0, new BigDecimal("85.00").compareTo(result.get("PUNKTY").setScale(2)));
    }


}