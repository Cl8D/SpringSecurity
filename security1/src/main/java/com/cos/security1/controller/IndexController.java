package com.cos.security1.controller;

import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller // 이렇게 하면 view를 리턴하게 된다.
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // localhost:8080/
    // localhost:8080
    @GetMapping({"", "/"})
    public String index() {
        return "index";
        // 이러면 기본값으로 index.mustache를 찾아가는데, 이를 .html 파일을 찾아가도록 바꾸자.
    }

    /**
     * 머스테치 기본 폴더 /src/main/resources
     * 뷰 리졸버 설정 : templates(prefix), mustache(suffix) -> 생략 가능
     */

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }


    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user) {
        user.setRole("ROLE_USER");
        // 단순히 이렇게만 하면 회원가입은 잘 되지만,
        // 패스워드가 암호화가 안 되어 있어서 시큐리티로 로그인 할 수가 없다.
        // 패스워드 암호화 적용하기
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user);
        // loginForm 함수 호출
        return "redirect:/loginForm";
    }

    /*
    @GetMapping("/joinProc")
    public @ResponseBody String joinProc() {
        return "회원가입 완료!";
    }
     */
}
