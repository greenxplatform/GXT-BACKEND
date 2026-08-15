package com.gxt.identity.controller;

import com.gxt.common.gxtNotifyMe.NotifyMeRequest;
import com.gxt.common.gxtNotifyMe.NotifyMeResponse;
import com.gxt.identity.service.NotifyMeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notify-me")
@RequiredArgsConstructor
public class NotifyMeController {

    private final NotifyMeService notifyMeService;

    @PostMapping
    public ResponseEntity<NotifyMeResponse> notifyMe(@Valid @RequestBody NotifyMeRequest req) {
        NotifyMeResponse body = notifyMeService.registerInterest(req);
        HttpStatus status = body.alreadyRegistered() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(body);
    }
}
