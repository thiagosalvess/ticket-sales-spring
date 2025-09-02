package com.thiagosalvess.ticketsales.common.exception;

public class TicketUnavailableException extends ConflictException {
    public TicketUnavailableException(String message) {
        super(message);
    }
}
