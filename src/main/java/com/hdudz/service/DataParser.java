package com.hdudz.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdudz.dto.OrderDTO;
import com.hdudz.dto.PaymentDTO;
import com.hdudz.model.Order;
import com.hdudz.model.PaymentMethod;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Order> parseOrders(String filePath) throws IOException {
        List<OrderDTO> dtoList = objectMapper.readValue(new File(filePath), new TypeReference<List<OrderDTO>>() {});
        return dtoList.stream()
                .map(dto -> new Order(dto.getId(), dto.getValue(), dto.getPromotions()))
                .collect(Collectors.toList());
    }

    public Map<String, PaymentMethod> parsePaymentMethods(String filePath) throws IOException {
        List<PaymentDTO> dtoList = objectMapper.readValue(new File(filePath), new TypeReference<List<PaymentDTO>>() {});
        Object PaymentMethodDTO;
        return dtoList.stream()
                .collect(Collectors.toMap(
                        PaymentDTO::getId,
                        dto -> new PaymentMethod(dto.getId(), dto.getDiscount(), dto.getLimit())
                ));
    }
}