package com.thiagosalvess.ticketsales.features.payment.service;

import java.math.BigDecimal;

public interface PaymentService {
    long processPayment(PayerData payer, BigDecimal amount, String cardToken);

    record PayerData(String name, String email, String address, String phone) {}
}
