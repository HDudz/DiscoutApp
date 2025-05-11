package com.hdudz.service;

import com.hdudz.model.Order;
import com.hdudz.model.PaymentMethod;
import com.hdudz.model.PaymentStrategy;
import com.hdudz.model.PotentialPayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentCalculator {
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TEN_PERCENT_THRESHOLD_POINTS = new BigDecimal("0.10");
    private static final BigDecimal TEN_PERCENT_DISCOUNT_RATE = new BigDecimal("0.10");

    private final Comparator<PotentialPayment> potentialPaymentComparator = (p1, p2) -> {
        int discountComparison = p2.getDiscountAmount().compareTo(p1.getDiscountAmount());
        if (discountComparison != 0) return discountComparison;
        int strategyComparison = p1.getStrategy().compareTo(p2.getStrategy());
        if (strategyComparison != 0) return strategyComparison;
        int orderIdComparison = p1.getOrder().getId().compareTo(p2.getOrder().getId());
        if (orderIdComparison != 0) return orderIdComparison;
        if (p1.getPrimaryPaymentMethod() != null && p2.getPrimaryPaymentMethod() != null) {
            return p1.getPrimaryPaymentMethod().getId().compareTo(p2.getPrimaryPaymentMethod().getId());
        }
        return 0;
    };

    public Map<String, BigDecimal> calculateOptimalPayments(List<Order> orders, Map<String, PaymentMethod> paymentMethods) {
        Map<String, BigDecimal> paidAmounts = paymentMethods.keySet().stream()
                .collect(Collectors.toMap(id -> id, id -> BigDecimal.ZERO));
        Set<Order> pendingOrders = new HashSet<>(orders);

        //Step 1: Full points payment check
        applyPaymentsForStage(pendingOrders, paymentMethods, paidAmounts, this::generateFullPoints);

        //Step 2: Full card payment check
        applyPaymentsForStage(pendingOrders, paymentMethods, paidAmounts, this::generatePromoCard);

        //Step 3: Partial points+card payment check
        applyPaymentsForStage(pendingOrders, paymentMethods, paidAmounts, this::generatePartialPoints);

        //Step 4: Fallback
        List<Order> fallbackOrders = new ArrayList<>(pendingOrders);
        fallbackOrders.sort(Comparator.comparing(Order::getId));
        for (Order order : fallbackOrders) {
            if (pendingOrders.contains(order)) {
                applyFallbackPayment(order, paymentMethods, paidAmounts);
                pendingOrders.remove(order);
            }
        }

        if (!pendingOrders.isEmpty()) {
            System.err.println("Warning (Final): Some orders could not be processed: "
                    + pendingOrders.stream().map(Order::getId).collect(Collectors.joining(", ")));
        }

        return paidAmounts.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @FunctionalInterface
    private interface PotentialPaymentGeneratorFunction {
        void generate(Order order, Map<String, PaymentMethod> paymentMethods, List<PotentialPayment> potentials);
    }

    private void applyPaymentsForStage(Set<Order> pendingOrders,
                                       Map<String, PaymentMethod> paymentMethods,
                                       Map<String, BigDecimal> paidAmounts,
                                       PotentialPaymentGeneratorFunction generatorFunction) {
        boolean paymentMadeInOuterLoop;
        do {
            paymentMadeInOuterLoop = false;
            if (pendingOrders.isEmpty()) break;

            List<PotentialPayment> allPotentialsForThisStagePass = new ArrayList<>();
            for (Order order : pendingOrders) { //Generuj dla WSZYSTKICH aktualnie oczekujących zamówień
                generatorFunction.generate(order, paymentMethods, allPotentialsForThisStagePass);
            }

            if (allPotentialsForThisStagePass.isEmpty()) {
                break;
            }

            allPotentialsForThisStagePass.sort(potentialPaymentComparator);

            PotentialPayment chosenPaymentThisIteration = null;

            for (PotentialPayment candidate : allPotentialsForThisStagePass) {
                if (pendingOrders.contains(candidate.getOrder())) {
                    chosenPaymentThisIteration = candidate;
                    break;
                }
            }

            if (chosenPaymentThisIteration != null) {
                applyPayment(chosenPaymentThisIteration, paymentMethods, paidAmounts);
                pendingOrders.remove(chosenPaymentThisIteration.getOrder());
                paymentMadeInOuterLoop = true;
            } else {
                break;
            }
        } while (paymentMadeInOuterLoop);
    }

    //Methods generating potential payment methods (generatePromoCard, generateFullPoints, generatePartialPoints)
    private void generatePromoCard(Order order, Map<String, PaymentMethod> paymentMethods, List<PotentialPayment> potentialPayments) {
        BigDecimal orderValue = order.getValue();
        if (order.getPromotions() != null) {
            for (String promoCardId : order.getPromotions()) {
                PaymentMethod card = paymentMethods.get(promoCardId);
                if (card != null && !card.getId().equals(PaymentMethod.POINTS_ID)) {
                    BigDecimal discountRate = card.getDiscountPercent().divide(HUNDRED, 10, RoundingMode.HALF_UP);
                    BigDecimal actualDiscountValue = orderValue.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal amountToPay = orderValue.subtract(actualDiscountValue);
                    if (card.getCurrentLimit().compareTo(amountToPay) >= 0) {
                        potentialPayments.add(new PotentialPayment(order, PaymentStrategy.PROMO_CARD, actualDiscountValue,
                                card, amountToPay, null, BigDecimal.ZERO));
                    }
                }
            }
        }
    }

    private void generateFullPoints(Order order, Map<String, PaymentMethod> paymentMethods, List<PotentialPayment> potentialPayments) {
        BigDecimal orderValue = order.getValue();
        PaymentMethod punktyMethod = paymentMethods.get(PaymentMethod.POINTS_ID);
        if (punktyMethod != null) {
            BigDecimal punktyDiscountRate = punktyMethod.getDiscountPercent().divide(HUNDRED, 10, RoundingMode.HALF_UP);
            BigDecimal actualPunktyDiscountValue = orderValue.multiply(punktyDiscountRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal amountToPayWithPunkty = orderValue.subtract(actualPunktyDiscountValue);
            if (punktyMethod.getCurrentLimit().compareTo(amountToPayWithPunkty) >= 0) {
                potentialPayments.add(new PotentialPayment(order, PaymentStrategy.FULL_PUNKTY, actualPunktyDiscountValue,
                        punktyMethod, amountToPayWithPunkty, null, BigDecimal.ZERO));
            }
        }
    }

    private void generatePartialPoints(Order order, Map<String, PaymentMethod> paymentMethods, List<PotentialPayment> potentialPayments) {
        BigDecimal orderValue = order.getValue();
        PaymentMethod punktyMethod = paymentMethods.get(PaymentMethod.POINTS_ID);

        if (punktyMethod != null) {

            BigDecimal minPointsFor10PercentRule = orderValue.multiply(TEN_PERCENT_THRESHOLD_POINTS).setScale(2, RoundingMode.HALF_UP);
            if (punktyMethod.getCurrentLimit().compareTo(minPointsFor10PercentRule) >= 0) {
                BigDecimal overallDiscountValue = orderValue.multiply(TEN_PERCENT_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalAfterDiscount = orderValue.subtract(overallDiscountValue);
                BigDecimal pointsToUseForThisOption = punktyMethod.getCurrentLimit().min(totalAfterDiscount);
                BigDecimal amountByPointsS3 = pointsToUseForThisOption;
                BigDecimal amountByCardS3 = totalAfterDiscount.subtract(amountByPointsS3);

                if (amountByCardS3.compareTo(BigDecimal.ZERO) <= 0) {
                    potentialPayments.add(new PotentialPayment(order, PaymentStrategy.PARTIAL_PUNKTY_ONLY_10_PERCENT_DISCOUNT,
                            overallDiscountValue, punktyMethod, totalAfterDiscount, null, BigDecimal.ZERO));
                } else {
                    PaymentMethod bestCardForS3Remainder = paymentMethods.values().stream()
                            .filter(c -> !c.getId().equals(PaymentMethod.POINTS_ID))
                            .filter(c -> c.getCurrentLimit().compareTo(amountByCardS3) >= 0)
                            .sorted(Comparator.comparing(PaymentMethod::getCurrentLimit, Comparator.reverseOrder())
                                    .thenComparing(PaymentMethod::getId))
                            .findFirst()
                            .orElse(null);

                    if (bestCardForS3Remainder != null) {
                        potentialPayments.add(new PotentialPayment(order, PaymentStrategy.PARTIAL_PUNKTY_CARD_10_PERCENT_DISCOUNT,
                                overallDiscountValue, punktyMethod, amountByPointsS3, bestCardForS3Remainder, amountByCardS3));
                    }
                }
            }
        }
    }

    //Supporting methods
    private void applyPayment(PotentialPayment payment, Map<String, PaymentMethod> paymentMethods, Map<String, BigDecimal> paidAmounts) {
        PaymentMethod primaryPMModel = paymentMethods.get(payment.getPrimaryPaymentMethod().getId());
        primaryPMModel.decreaseLimit(payment.getPrimaryAmountPaid());
        paidAmounts.merge(primaryPMModel.getId(), payment.getPrimaryAmountPaid(), BigDecimal::add);

        if (payment.getSecondaryPaymentMethod() != null) {
            PaymentMethod secondaryPMModel = paymentMethods.get(payment.getSecondaryPaymentMethod().getId());
            secondaryPMModel.decreaseLimit(payment.getSecondaryAmountPaid());
            paidAmounts.merge(secondaryPMModel.getId(), payment.getSecondaryAmountPaid(), BigDecimal::add);
        }
    }

    private void applyFallbackPayment(Order order, Map<String, PaymentMethod> paymentMethods, Map<String, BigDecimal> paidAmounts) {
        BigDecimal remainingValueToPay = order.getValue();
        PaymentMethod punktyMethod = paymentMethods.get(PaymentMethod.POINTS_ID);

        if (punktyMethod != null && punktyMethod.getCurrentLimit().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal payWithPunkty = remainingValueToPay.min(punktyMethod.getCurrentLimit());
            if (payWithPunkty.compareTo(BigDecimal.ZERO) > 0) {
                punktyMethod.decreaseLimit(payWithPunkty);
                paidAmounts.merge(punktyMethod.getId(), payWithPunkty, BigDecimal::add);
                remainingValueToPay = remainingValueToPay.subtract(payWithPunkty);
            }
        }

        if (remainingValueToPay.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal finalRemainingValueToPay = remainingValueToPay;
            PaymentMethod cardToUse = paymentMethods.values().stream()
                    .filter(c -> !c.getId().equals(PaymentMethod.POINTS_ID))
                    .filter(c -> c.getCurrentLimit().compareTo(finalRemainingValueToPay) >= 0)
                    .sorted(Comparator.comparing(PaymentMethod::getCurrentLimit, Comparator.reverseOrder())
                            .thenComparing(PaymentMethod::getId))
                    .findFirst()
                    .orElse(null);

            if (cardToUse != null) {
                cardToUse.decreaseLimit(remainingValueToPay);
                paidAmounts.merge(cardToUse.getId(), remainingValueToPay, BigDecimal::add);
            } else {
                System.err.println("Error: Cannot fully pay order " + order.getId() +
                        " in fallback. Remaining amount: " + remainingValueToPay +
                        ". This may indicate insufficient funds across all cards for this specific amount with one card.");
            }
        }
    }
}