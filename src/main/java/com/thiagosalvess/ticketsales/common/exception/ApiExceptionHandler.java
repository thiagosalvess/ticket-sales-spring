package com.thiagosalvess.ticketsales.common.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.AuthenticationException;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req, List<ApiError.FieldErrorItem> fields) {
        var body = ApiError.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.FieldErrorItem> details = ex.getBindingResult().getAllErrors().stream()
                .map(err -> {
                    String field = (err instanceof FieldError fe) ? fe.getField() : err.getObjectName();
                    return new ApiError.FieldErrorItem(field, err.getDefaultMessage());
                }).toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, details);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", req, null);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso", req, null);
    }

    @ExceptionHandler({ ForbiddenException.class, OwnershipException.class })
    public ResponseEntity<ApiError> handleForbiddenDomain(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler({ ConflictException.class, OptimisticLockException.class, DataIntegrityViolationException.class })
    public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler({ UnprocessableEntityException.class, PaymentFailedException.class })
    public ResponseEntity<ApiError> handleUnprocessable(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado", req, null);
    }
}
