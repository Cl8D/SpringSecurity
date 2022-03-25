package com.cos.jwt.controller;

import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.config.repository.UserRepository;
import com.cos.jwt.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 모든 사람이 접근 가능
    @GetMapping("/home")
    public String home() {
        return "<h1>home</h1>";
    }


    @PostMapping("/token")
    public String token() {
        return "<h1>token</h1>";
    }


    // ROLE_USER, MANAGER, ADMIN 접근 가능
    @GetMapping("/api/v1/user")
    public String user (Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("principalDetails.getUser().getId() = " + principalDetails.getUser().getId());
        System.out.println("principalDetails.getUser().getUsername() = " + principalDetails.getUser().getUsername());
        System.out.println("principalDetails.getUser().getPassword() = " + principalDetails.getUser().getPassword());
        return "<h1>user</h1>";
    }


    @GetMapping("/api/v1/manager")
    public String manager() {
        return "<h1>manager</h1>";
    }

    @GetMapping("/api/v1/admin")
    public String admin() {
        return "admin";
    }

    // ROLE_MANAGER, ADMIN만 접근 가능
    @GetMapping("/manager/reports")
    public String reports() {
        return "<h1>reports</h1>";
    }


    // ROLE_ADMIN만 접근 가능
    @GetMapping("/admin/users")
    public List<User> users() {
        return userRepository.findAll();
    }

    @PostMapping("/join")
    public String join(@RequestBody User user) {
        // 회원가입 시 비밀번호 암호화
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
        userRepository.save(user);
        return "회원가입 완료";
    }
}
