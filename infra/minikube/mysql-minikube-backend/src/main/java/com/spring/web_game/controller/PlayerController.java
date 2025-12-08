package com.spring.web_game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.web_game.model.PlayerModel;
import com.spring.web_game.service.PasswordResetService;
import com.spring.web_game.service.PlayerService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/player")
public class PlayerController {
    private final PlayerService playerService;
    private final PasswordResetService passwordResetService;

    public PlayerController(PlayerService playerService, PasswordResetService passwordResetService){
        this.playerService = playerService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentialInfo) {
        String emailAddr = credentialInfo.get("emailAddr");
        String password = credentialInfo.get("password");

        boolean isAuthenticated = playerService.isAuthenticated(emailAddr, password);
        Map<String, Object> res = new HashMap<>();

        if(isAuthenticated){
            res.put("loginTry", true);
            res.put("msg", "Succeed to login");
            return ResponseEntity.ok(res);
        }
        else{
            res.put("loginTry", false);
            res.put("msg", "Fail to login");
            return ResponseEntity.status(401).body(res);
        }
    }

    @PostMapping("/registration")
    public ResponseEntity<Map<String, Object>> registration(@RequestBody Map<String, String> registrationInfo) {
        String firstName = registrationInfo.get("firstName");
        String familyName = registrationInfo.get("familyName");
        String emailAddr = registrationInfo.get("emailAddr");
        String password = registrationInfo.get("newPassword");

        PlayerModel newPlayer = new PlayerModel();
        newPlayer.setFirstName(firstName);
        newPlayer.setFamilyName(familyName);
        newPlayer.setEmailAddr(emailAddr);
        newPlayer.setPassword(password);

        boolean isRegistrationCompleted = playerService.isRegistrationCompleted(newPlayer);
        Map<String, Object> res = new HashMap<>();

        if(isRegistrationCompleted){
            res.put("registrationTry", true);
            res.put("msg", "Succeed to register");
            return ResponseEntity.ok(res);
        }
        else{
            res.put("registrationTry", false);
            res.put("msg", "Fail to register");
            return ResponseEntity.status(401).body(res);
        }
    }

    @PostMapping("/password-reset-email-check")
    public ResponseEntity<Map<String, Object>> passwordResetEmailCheck(@RequestBody Map<String, String> passwordResetCheckInfo) {
        String targetEmailAddr = passwordResetCheckInfo.get("targetInfo");

        boolean isPasswordResetEmailSent = passwordResetService.sendPasswordResetEmail(targetEmailAddr);
        Map<String, Object> res = new HashMap<>();

        if(isPasswordResetEmailSent){
            res.put("passwordResetMailSendTry", true);
            return ResponseEntity.ok(res);
        }
        else{
            res.put("passwordResetMailSendTry", false);
            return ResponseEntity.status(401).body(res);
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, Object>> passwordReset(@RequestBody Map<String, String> passwordResetInfo) {
        String tokenInfo = passwordResetInfo.get("tokenInfo");
        String newPassword = passwordResetInfo.get("newPassword");

        boolean isPasswordReset = passwordResetService.resetPassword(tokenInfo, newPassword);
        Map<String, Object> res = new HashMap<>();

        if(isPasswordReset){
            res.put("passwordResetTry", true);
            return ResponseEntity.ok(res);
        }
        else{
            res.put("passwordResetTry", false);
            return ResponseEntity.status(401).body(res);
        }
    }
}
