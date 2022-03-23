package com.cos.security1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터 체인에 등록
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
                // /login 호출 시 시큐리티가 낚아채서 대신 로그인을 진행해준다.
                .loginProcessingUrl("/login")
                // 만약 성공한다면 메인 페이지로 이동하도록
                .defaultSuccessUrl("/");
    }
}
