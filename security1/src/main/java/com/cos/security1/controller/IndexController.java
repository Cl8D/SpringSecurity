package com.cos.security1.controller;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping("/test/login")
    public @ResponseBody String testLogin(
            Authentication authentication,
            //@AuthenticationPrincipal UserDetails userDetails
            @AuthenticationPrincipal PrincipalDetails userDetails) {
        System.out.println("/test/login ===================");

        // 리턴 타입이 object이기 때문에 캐스팅해주기
        // authentication.getPrincipal() = com.cos.security1.config.auth.PrincipalDetails@507fa85c
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // authentication = User(id=7, username=hi, password=$2a$10$0k96g8QVRnI0KgMXvHJVTO1VsU7QHan8VtUg.Q4ocUHn0zlJmBSVS, email=hi@daum.net, role=ROLE_USER, createDate=2022-03-24 12:12:11.961)
        // 로그인한 user 정보 확인이 가능하다.
        System.out.println("authentication = " + principalDetails.getUser());

        // userDetails = hi
        // 혹은 어노테이션(@Authentication) 을 통해서 세션 정보를 확인할 수 있다!
        // System.out.println("userDetails = " + userDetails.getUsername());
        System.out.println("userDetails = " + userDetails.getUser());
        return "세션 정보 확인하기";
    }

    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOAuthLogin(
            Authentication authentication,
            @AuthenticationPrincipal OAuth2User oauth) {
        System.out.println("/test/oauth/login ===================");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // authentication = {sub=107959218152023539623, name=지원, given_name=지원, picture=https://lh3.googleusercontent.com/a/AATXAJzlOwc3adxPWlBm9cNzi5PH5Dp6HgCiJKnpoqte=s96-c, email=ljwon77@gmail.com, email_verified=true, locale=ko}
        System.out.println("authentication = " + oAuth2User.getAttributes());

        // 마찬가지로 어노테이션을 통해서도 받아올 수 있다.
        // oAuth2User.getAttributes() = {sub=107959218152023539623, name=지원, given_name=지원, picture=https://lh3.googleusercontent.com/a/AATXAJzlOwc3adxPWlBm9cNzi5PH5Dp6HgCiJKnpoqte=s96-c, email=ljwon77@gmail.com, email_verified=true, locale=ko}
        System.out.println("oAuth2User.getAttributes() = " + oAuth2User.getAttributes());
        return "OAuth 세션 정보 확인하기";
    }


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

    // 최종적으로 Oauth 로그인이든, 일반 로그인이든
    // principalDetails 타입으로 받을 수 있어지는 것이다.
    // 아까 test 코드처럼 따로 나눠서 구현할 필요가 없어짐!
    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 일반적인 로그인 했을 때
        //principalDetails.getUser() = User(id=7, username=hi, password=$2a$10$0k96g8QVRnI0KgMXvHJVTO1VsU7QHan8VtUg.Q4ocUHn0zlJmBSVS, email=hi@daum.net, role=ROLE_USER, provider=null, providerId=null, createDate=2022-03-24 12:12:11.961)

        // oauth2 로그인 했을 때
        // principalDetails.getUser() = User(id=10, username=169291646771-olb05fpt9eb4bdv27ii1kbgnstb90sld.apps.googleusercontent.com_107959218152023539623, password=$2a$10$xUKtj2PUK2Lk/dysxWZ64.5kQrZRi6t3Mhf9V0MWGmwgavFlYDrZy, email=ljwon77@gmail.com, role=ROLE_USER, provider=169291646771-olb05fpt9eb4bdv27ii1kbgnstb90sld.apps.googleusercontent.com, providerId=107959218152023539623, createDate=2022-03-24 15:58:14.226)
        System.out.println("principalDetails.getUser() = " + principalDetails.getUser());
        return "user";
    }

    /**
     * 정리하자면)
     * 스프링 시큐리티에는 시큐레티 세션이 존재한다.
     * 시큐리티 세션에는 Authentication 객체만 들어갈 수 있는데,
     * 이 객체가 들어간 순간 로그인이 되었다고 말할 수 있다.
     * Authentication 안에는 UserDetails와 OAuth2User 타입이 들어갈 수 있다.
     * 일반적인 로그인 시 UserDetails로,
     * Oauth로 로그인하면 OAuth2User 타입이 들어가게 된다.
     *
     * 그래서, 컨트롤러 단위에서 하나로 선언하기 힘드니까
     * 하나의 클래스를 새로 생성해서 두 개를 상속받아서
     * Authentication 안에 넣어서 사용해준다.
     * 우리 코드에서는 principalDetails를 사용해주면 된다.
     */



    @GetMapping("/admin")
    public @ResponseBody  String admin() {
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

    // 특정 메서드에 대한 권한을 걸 수 있게 된다 (secured)
    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody String info() {
        return "개인정보";
    }



    // 여러 개를 권한을 걸 고 싶다면 preAuthorize 사용하기
    @PreAuthorize("hasRole(ROLE_MANAGER) or hasRole(ROLE_ADMIN)")
    @GetMapping("/data")
    public @ResponseBody String data() {
        return "데이터 정보";
    }

    /*
    @GetMapping("/joinProc")
    public @ResponseBody String joinProc() {
        return "회원가입 완료!";
    }
     */
}
