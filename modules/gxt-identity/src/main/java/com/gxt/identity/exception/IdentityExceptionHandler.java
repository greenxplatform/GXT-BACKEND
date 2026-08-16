package com.gxt.identity.exception;

import com.gxt.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IdentityExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex, request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> emailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> userNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex, request);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ApiError> notActive(AccountNotActiveException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE", ex, request);
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ApiError> otpInvalid(OtpInvalidException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "OTP_INVALID", ex, request);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiError> otpExpired(OtpExpiredException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", ex, request);
    }

    @ExceptionHandler(AlreadyVerifiedException.class)
    public ResponseEntity<ApiError> alreadyVerified(AlreadyVerifiedException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "ALREADY_VERIFIED", ex, request);
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ApiError> googleToken(InvalidGoogleTokenException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", ex, request);
    }

    @ExceptionHandler(OAuthEmailRequiredException.class)
    public ResponseEntity<ApiError> oauthEmail(OAuthEmailRequiredException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "OAUTH_EMAIL_REQUIRED", ex, request);
    }

    @ExceptionHandler(OAuthAccountConflictException.class)
    public ResponseEntity<ApiError> oauthConflict(OAuthAccountConflictException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "OAUTH_ACCOUNT_CONFLICT", ex, request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), code, ex.getMessage(), request.getRequestURI(), List.of()));
    }
}
