package com.gxt.identity.controller;

import com.gxt.common.gxtIdentity.EmailVerifyConfirmRequest;
import com.gxt.common.gxtIdentity.VerificationStatusResponse;
import com.gxt.identity.security.GxtUserPrincipal;
import com.gxt.identity.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmailOtp(@AuthenticationPrincipal GxtUserPrincipal principal) {
        verificationService.requestEmailVerification(principal.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<VerificationStatusResponse> confirm(
            @AuthenticationPrincipal GxtUserPrincipal principal,
            @Valid @RequestBody EmailVerifyConfirmRequest req) {
        return ResponseEntity.ok(
                verificationService.confirmEmailVerification(principal.userId(), req.code()));
    }

    @GetMapping("/status")
    public ResponseEntity<VerificationStatusResponse> status(
            @AuthenticationPrincipal GxtUserPrincipal principal) {
        return ResponseEntity.ok(verificationService.getStatus(principal.userId()));
    }
}
