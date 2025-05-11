package com.hdudz.model;


//Kolejnosc tutaj wplywa na preferencje
public enum PaymentStrategy {
    FULL_PUNKTY, //Najbardziej preferowane jeśli rabat jest taki sam
    PARTIAL_PUNKTY_ONLY_10_PERCENT_DISCOUNT,// Płatność tylko punktami
    PARTIAL_PUNKTY_CARD_10_PERCENT_DISCOUNT,// Punkty + Karta z rabatem 10%
    PROMO_CARD // Najmniej preferowane z opcji rabatowych jeśli rabat jest taki sam
}