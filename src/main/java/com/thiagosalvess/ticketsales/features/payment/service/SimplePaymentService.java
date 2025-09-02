package com.thiagosalvess.ticketsales.features.payment.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimplePaymentService implements PaymentService {
    @Override
    public long processPayment(PayerData payer, BigDecimal amount, String cardToken) {
        return ThreadLocalRandom.current().nextLong(1_000_000_000L);
    }
}
