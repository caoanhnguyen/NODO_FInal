package com.example.nodo_final.exception;

import com.example.nodo_final.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.query.SemanticException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý các ngoại lệ xác thực dữ liệu, dùng valid trong DTO
//    @ExceptionHandler({ConstraintViolationException.class,
//                    MissingServletRequestParameterException.class,
//                    MethodArgumentNotValidException.class,
//                    HandlerMethodValidationException.class})
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
//
//        ErrorResponse errorResponse = new ErrorResponse();
//        errorResponse.setTimestamp(new Date());
//        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
//        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
//
//        if (e instanceof MethodArgumentNotValidException ex) {
//            String errs = ex.getBindingResult().getFieldErrors().stream()
//                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
//                    .collect(Collectors.joining("; "));
//            errorResponse.setError("Invalid Payload");
//            errorResponse.setMessage(errs);
//        } else if (e instanceof MissingServletRequestParameterException ex) {
//            errorResponse.setError("Invalid Parameter");
//            errorResponse.setMessage(ex.getParameterName() + " is missing");
//        } else if (e instanceof ConstraintViolationException ex) {
//            String errs = ex.getConstraintViolations().stream()
//                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
//                    .collect(Collectors.joining("; "));
//            errorResponse.setError("Invalid Parameter");
//            errorResponse.setMessage(errs);
//        } else {
//            errorResponse.setError("Invalid Data");
//            errorResponse.setMessage(e.getMessage());
//        }
//
//        return errorResponse;
//    }

    /**
     * Xử lý lỗi validation cho @Valid @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {

        // Tạo map để chứa các lỗi: { "fieldName": "errorMessage" }
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Nối các lỗi lại thành một chuỗi
        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Payload") // Lỗi rõ ràng
                .message(errorMessage) // Thông báo lỗi chi tiết
                .build();
    }

    /**
     * Xử lý lỗi validation cho @PathVariable và @RequestParam (ví dụ @NotBlank @PathVariable String maHS).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Parameter")
                .message(errorMessage)
                .build();
    }

    /**
     * Xử lý lỗi thiếu @RequestParam.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestParam(MissingServletRequestParameterException ex, WebRequest request) {

        String errorMessage = ex.getParameterName() + " is missing";

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Parameter")
                .message(errorMessage)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidation(HandlerMethodValidationException ex, WebRequest request) {
        // Lấy tất cả ConstraintViolation từ tất cả ParameterValidationResult
        String errorMessage = ex.getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        // FieldError sẽ có tên field cụ thể
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    } else {
                        // ObjectError (không phải field) thường dùng cho object/global error
                        return error.getClass() + ": " + error.getDefaultMessage();
                    }
                })
                .collect(Collectors.joining("; "));

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Payload")
                .message(errorMessage)
                .build();
    }


    // Xử lý ResourceNotFoundException, khi không tìm thấy tài nguyên
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(Exception e, WebRequest request) {

        return ErrorResponse.builder()
                .timestamp(new Date())
                .status(NOT_FOUND.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(NOT_FOUND.getReasonPhrase())
                .message(e.getMessage())
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

//    @ExceptionHandler(SemanticException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ErrorResponse handleSemanticException(SemanticException ex, WebRequest request) {
//
//        return ErrorResponse.builder()
//                .timestamp(new Date())
//                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .path(request.getDescription(false).replace("uri=", ""))
//                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
//                .message("Lỗi máy chủ khi xử lý dữ liệu.")
//                .build();
//    }

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
