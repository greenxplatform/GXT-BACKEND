package com.gxt.identity.controller;

import com.gxt.common.gxtIdentity.UserSummaryResponse;
import com.gxt.identity.dal.UserDal;
import com.gxt.identity.entity.User;
import com.gxt.identity.exception.UserNotFoundException;
import com.gxt.identity.security.GxtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserDal userDal;

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> me(@AuthenticationPrincipal GxtUserPrincipal principal) {
        User user = userDal.findById(principal.userId()).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(new UserSummaryResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getIsVerified()));
    }
}
