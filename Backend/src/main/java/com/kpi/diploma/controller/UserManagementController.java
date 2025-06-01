package com.kpi.diploma.controller;

import com.kpi.diploma.model.ChangeResponse;
import com.kpi.diploma.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("user")
public class UserManagementController {
    private final UserService userService;

    public UserManagementController (UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete () {
        userService.delete();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PatchMapping("/change/username")
    public ResponseEntity<?> changeUsername (@RequestParam("newUsername") String value, Principal principal) {
        ChangeResponse changeResponse = userService.changeUsername(value, principal.getName());
        return ResponseEntity.ok(changeResponse);
    }

    @PatchMapping("/change/email")
    public ResponseEntity<?> changeEmail (@RequestParam("newEmail") String value, Principal principal) {
        ChangeResponse changeResponse = userService.changeEmail(value, principal.getName());
        return ResponseEntity.ok(changeResponse);
    }

    @PatchMapping("/change/password")
    public ResponseEntity<?> changePassword (@RequestParam("newPassword") String value, Principal principal) {
        ChangeResponse changeResponse = userService.changePassword(value, principal.getName());
        return ResponseEntity.ok(changeResponse);
    }
}
