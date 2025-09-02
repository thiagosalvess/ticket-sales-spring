package com.thiagosalvess.ticketsales.common.exception;

public class PaymentFailedException extends UnprocessableEntityException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
