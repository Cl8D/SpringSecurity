package com.cos.security1.config;

import com.cos.security1.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터 체인에 등록
// 서비스 계층을 직접 호출할 때 사용할 수 있는 보안 기능
// 컨트롤러에 직접적으로 role을 부여할 수 있게 된다.
// 이때 옵션으로 securedEnabled를 설정하면 @Secured를 통해서 처리가 가능해진다.
// prePostEnabled 설정 시 @PreAuthorize, @PostAuthorize를 사용할 수 있다
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    // bean 등록 시 해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
    // 스프링 빈 컨테이너에 등록하고, 자동 의존관계 주입을 진행해줌.
    @Bean
    // 비밀번호 암호화 (스프링 시큐리티 사용하기 위해)
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    // 커스텀 인증 매커니즘 구성
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        // 요청에 대한 권한 지정
        http.authorizeRequests()
                // 특정 권한을 가진 사용자만 접근할 수 있도록.
                // 특정 경로 지정 (/user로 들어왔을 때)
                .antMatchers("/user/**")
                // 인증된 사용자만 접근을 허용하도록 (로그인 되어 있을 때)
                .authenticated()
                // /manager로 들어왔을 때
                .antMatchers("/manager/**")
                // 해당 manger가 ADMIN이나 USER로 role이 지정되어 있다면 접근 허용
                // 즉, 로그인 + admin or user 역할일 때
                .access("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
                // /admin으로 들어왔을 때
                .antMatchers("/admin/**")
                // role이 admin일 때만 접근 허용 (로그인 + admin)
                .access("hasRole('ROLE_ADMIN')")
                // 그외 나머지는 모두 접근 허용
                .anyRequest().permitAll()
                .and()
                .formLogin()
                // 사용자 정의 로그인 페이지 사용 가능
                .loginPage("/loginForm")
                // UserDetailService의 loadUserByUsername의 파라미터에 대한 설정
                // 여기서 전달해준 애를 저 함수의 파라미터로 바인딩해준다.
                .usernameParameter("username")
                // /login 호출 시 시큐리티가 낚아채서 대신 로그인을 진행해준다.
                .loginProcessingUrl("/login")
                // 만약 성공한다면 메인 페이지로 이동하도록
                .defaultSuccessUrl("/")
                // 구글 로그인 설정 (oauth2)
                .and()
                .oauth2Login()
                .loginPage("/loginForm")
        /**
         * 구글 로그인 이후 후처리가 필요하다
         * 1. 코드 받기(인증이 되었음을 의미) 2. 엑세스 토큰 받기 (권한 생김)
         * 3. security server는 구글 로그인된 사요ㅇ자에 대한 정보를 볼 수 있는 권한이 부여됨.
         * 4-1. 이러한 정보를 바탕으로 회원가입을 자동으로 진행시키기도 함
         * 4-2. 만약, 구글에서 제공하는 정보가 우리의 웹사이트에서 필요한 정보에 비해 부족하다면
         * 추가적인 회원가입 창을 통해서 사용자에게 더 정보를 받은 다음, 회원가입을 진행시켜야 한다.
         *
         * 근데, 구글 로그인의 경우 코드를 받지 않고
         * 엑세스 토큰 + 사용자 프로필 정보를 바로 받는다.
         */
                // oauth2Login에 성공하면 userService의 파라미터에서 설정을 진행하게 된다.
                .userInfoEndpoint()
                .userService(principalOauth2UserService);

    }
}
