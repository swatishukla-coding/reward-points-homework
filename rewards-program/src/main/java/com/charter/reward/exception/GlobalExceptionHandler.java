package com.charter.reward.exception;

import com.charter.reward.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String REQUEST_VALIDATION_FAILED = "Request validation failed";

    /**
     * Converts a missing customer error into a structured 404 response.
     *
     * @param ex exception raised when the customer does not exist
     * @param request current web request metadata
     * @return 404 response payload
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex, WebRequest request) {
        log.warn("Customer not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Converts validation and request parsing errors into a structured 400 response.
     *
     * @param ex invalid request exception
     * @param request current web request metadata
     * @return 400 response payload
     */
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            ValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, validationMessage(ex), request);
    }

    /**
     * Converts unknown routes into a structured 404 response.
     *
     * @param ex exception describing the missing route
     * @param request current web request metadata
     * @return structured 404 response
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "The requested endpoint was not found", request);
    }

    /**
     * Converts unsupported HTTP methods into a structured 405 response.
     *
     * @param ex exception describing the unsupported method
     * @param request current web request metadata
     * @return structured 405 response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported for this endpoint", request);
    }

    /**
     * Converts transaction lookup failures into a structured 503 response.
     *
     * @param ex transaction fetch exception
     * @param request current web request metadata
     * @return 503 response payload
     */
    @ExceptionHandler(TransactionFetchException.class)
    public ResponseEntity<ErrorResponse> handleTransactionFetch(TransactionFetchException ex, WebRequest request) {
        log.error("Transaction fetch failed", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    /**
     * Converts wrapped asynchronous failures into the same response as their root cause.
     *
     * @param ex wrapper exception raised by asynchronous execution
     * @param request current web request metadata
     * @return response payload matching the wrapped application exception
     */
    @ExceptionHandler({CompletionException.class, ExecutionException.class})
    public ResponseEntity<ErrorResponse> handleAsyncException(Exception ex, WebRequest request) {
        Throwable cause = unwrap(ex);
        if (cause instanceof CustomerNotFoundException) {
            return handleCustomerNotFound((CustomerNotFoundException) cause, request);
        }
        if (cause instanceof ValidationException) {
            return handleBadRequest((ValidationException) cause, request);
        }
        if (cause instanceof ConstraintViolationException) {
            return handleBadRequest((ConstraintViolationException) cause, request);
        }
        if (cause instanceof TransactionFetchException) {
            return handleTransactionFetch((TransactionFetchException) cause, request);
        }
        log.error("Unexpected async error handling request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    /**
     * Converts unexpected application failures into a structured 500 response.
     *
     * @param ex unexpected exception
     * @param request current web request metadata
     * @return 500 response payload
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error handling request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse body = new ErrorResponse(status, message, path);
        return ResponseEntity.status(status).body(body);
    }

    private String validationMessage(Exception ex) {
        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException mismatch = (MethodArgumentTypeMismatchException) ex;
            return "Invalid value for parameter '" + mismatch.getName() + "'";
        }
        if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException missing = (MissingServletRequestParameterException) ex;
            return "Required parameter '" + missing.getParameterName() + "' is missing";
        }
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolation = (ConstraintViolationException) ex;
            String message = constraintViolation.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .filter(value -> value != null && !value.trim().isEmpty())
                    .collect(Collectors.joining("; "));
            return normalizeValidationMessage(message);
        }
        if (ex instanceof MethodArgumentNotValidException) {
            return REQUEST_VALIDATION_FAILED;
        }
        if (ex instanceof ValidationException) {
            return normalizeValidationMessage(ex.getMessage());
        }
        return REQUEST_VALIDATION_FAILED;
    }

    private String normalizeValidationMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return REQUEST_VALIDATION_FAILED;
        }
        return message;
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while ((throwable instanceof CompletionException || throwable instanceof ExecutionException) && cause != null) {
            throwable = cause;
            cause = throwable.getCause();
        }
        return throwable;
    }
}
