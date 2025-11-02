package com.example.nodo_final.exception;

import com.example.nodo_final.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.query.SemanticException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request,
            Locale locale
    ) {
        String paramName = ex.getName();
        String expectedType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "valid type";

        Object[] args = { paramName, expectedType }; // Tham số {0} và {1}
        String message = messageSource.getMessage("common.param.type_mismatch", args, locale);

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Parameter Type")
                .message(message)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request, Locale locale) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String defaultMsg = error.getDefaultMessage() != null ? error.getDefaultMessage() : "";
            // Dùng messageSource để resolve i18n (nếu defaultMessage là một message code)
            String message = messageSource.getMessage(defaultMsg, null, defaultMsg, locale);
            errors.put(error.getField(), message);
        });

        // Nối các lỗi lại thành một chuỗi
        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(messageSource.getMessage("error.invalid.payload", null, "Invalid Payload", locale))
                .message(errorMessage)
                .build();
    }

    /**
     * Xử lý lỗi validation cho @PathVariable và @RequestParam (ví dụ @NotBlank @PathVariable String maHS).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, WebRequest request, Locale locale) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String msgCode = violation.getMessage() != null ? violation.getMessage() : "";
                    String msg = messageSource.getMessage(msgCode, null, msgCode, locale);
                    return violation.getPropertyPath() + ": " + msg;
                })
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(messageSource.getMessage("error.invalid.parameter", null, "Invalid Parameter", locale))
                .message(errorMessage)
                .build();
    }

    /**
     * Xử lý lỗi thiếu @RequestParam.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestParam(MissingServletRequestParameterException ex, WebRequest request, Locale locale) {

        String defaultMsg = ex.getParameterName() + " is missing";
        String errorMessage = messageSource.getMessage("error.param.missing", new Object[]{ex.getParameterName()}, defaultMsg, locale);

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(messageSource.getMessage("error.invalid.parameter", null, "Invalid Parameter", locale))
                .message(errorMessage)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidation(HandlerMethodValidationException ex, WebRequest request, Locale locale) {
        // Lấy tất cả ConstraintViolation từ tất cả ParameterValidationResult
        String errorMessage = ex.getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError fieldError) {
                        // FieldError sẽ có tên field cụ thể
                        String defaultMsg = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "";
                        String resolved = messageSource.getMessage(defaultMsg, null, defaultMsg, locale);
                        return fieldError.getField() + ": " + resolved;
                    } else if (err instanceof ObjectError objectError) {
                        String defaultMsg = objectError.getDefaultMessage() != null ? objectError.getDefaultMessage() : "";
                        String resolved = messageSource.getMessage(defaultMsg, null, defaultMsg, locale);
                        return objectError.getObjectName() + ": " + resolved;
                    } else {
                        String defaultMsg = err.getDefaultMessage() != null ? err.getDefaultMessage() : "";
                        String resolved = messageSource.getMessage(defaultMsg, null, defaultMsg, locale);
                        return resolved;
                    }
                })
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(messageSource.getMessage("error.invalid.payload", null, "Invalid Payload", locale))
                .message(errorMessage)
                .build();
    }


    // Xử lý ResourceNotFoundException, khi không tìm thấy tài nguyên
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(Exception e, WebRequest request, Locale locale) {

        String code = e.getMessage() != null ? e.getMessage() : "";
        String msg = messageSource.getMessage(code, null, code, locale);

        return ErrorResponse.builder()
                .timestamp(new Date())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(msg)
                .status(NOT_FOUND.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    // Xử lý ResourceAlreadyExistsException, khi tài nguyên đã tồn tại
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleResourceAlreadyExists(ResourceAlreadyExistsException e, WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(CONFLICT.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .build();
    }

    // Xử lý InvalidDataException, khi dữ liệu không hợp lệ
    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateKeyException(InvalidDataException e, WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(CONFLICT.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(SemanticException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDatabaseExceptions(Exception ex, WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Lỗi máy chủ khi xử lý dữ liệu.")
                .build();
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleStorageException(StorageException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .build();
    }

    // Xử lý tất cả các ngoại lệ khác
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex, WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .build();
    }
}
