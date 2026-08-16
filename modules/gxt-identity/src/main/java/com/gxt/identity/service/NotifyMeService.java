package com.gxt.identity.service;

import com.gxt.common.gxtNotifyMe.NotifyMeRequest;
import com.gxt.common.gxtNotifyMe.NotifyMeResponse;
import com.gxt.common.gxtNotifyMe.NotifyMeStatus;
import com.gxt.identity.entity.NotifyMeInterest;
import com.gxt.identity.repository.NotifyMeInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyMeService {

    private static final String REGISTERED_MESSAGE =
            "Your interest has been registered. We'll email you when there are updates.";
    private static final String ALREADY_MESSAGE =
            "This email is already on the list. We'll be in touch.";

    private final NotifyMeInterestRepository notifyMeInterestRepository;
    private final NotifyMeMailService notifyMeMailService;

    @Transactional
    public NotifyMeResponse registerInterest(NotifyMeRequest req) {
        String email = req.email().trim().toLowerCase();
        String name = req.name().trim();

        return notifyMeInterestRepository
                .findByEmail(email)
                .map(existing -> toResponse(existing, true, ALREADY_MESSAGE))
                .orElseGet(() -> createAndNotify(name, email));
    }

    private NotifyMeResponse createAndNotify(String name, String email) {
        NotifyMeInterest saved = notifyMeInterestRepository.save(
                NotifyMeInterest.builder()
                        .name(name)
                        .email(email)
                        .status(NotifyMeStatus.INTERESTED)
                        .build());
        try {
            notifyMeMailService.sendInterestRegistered(name, email);
        } catch (Exception ex) {
            log.error("Failed to send notify-me confirmation to {}", email, ex);
        }
        return toResponse(saved, false, REGISTERED_MESSAGE);
    }

    private NotifyMeResponse toResponse(NotifyMeInterest row, boolean already, String message) {
        return new NotifyMeResponse(
                row.getId(), row.getName(), row.getEmail(), row.getStatus(), already, message);
    }
}
