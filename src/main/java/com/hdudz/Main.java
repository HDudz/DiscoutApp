package com.hdudz;

import com.hdudz.model.Order;
import com.hdudz.model.PaymentMethod;
import com.hdudz.service.DataParser;
import com.hdudz.service.PaymentCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <orders_file_path> <payment_methods_file_path>");
            System.exit(1);
        }

        String ordersFilePath = args[0];
        String paymentMethodsFilePath = args[1];

        DataParser dataParser = new DataParser();
        PaymentCalculator paymentCalculator = new PaymentCalculator();

        try {
            List<Order> orders = dataParser.parseOrders(ordersFilePath);
            Map<String, PaymentMethod> paymentMethods = dataParser.parsePaymentMethods(paymentMethodsFilePath);

            Map<String, BigDecimal> result = paymentCalculator.calculateOptimalPayments(orders, paymentMethods);

            result.forEach((methodId, amount) ->
                    System.out.printf("%s %.2f%n", methodId, amount)
            );

        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}