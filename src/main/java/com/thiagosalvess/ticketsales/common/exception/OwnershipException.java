package com.thiagosalvess.ticketsales.common.exception;

public class OwnershipException extends ForbiddenException {
    public OwnershipException(String message) {
        super(message);
    }
}
