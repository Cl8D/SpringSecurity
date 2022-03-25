package com.cos.jwt.config;

import com.cos.jwt.config.jwt.JwtAuthenticationFilter;
import com.cos.jwt.config.jwt.JwtAuthorizationFilter;
import com.cos.jwt.config.repository.UserRepository;
import com.cos.jwt.filter.MyFilter1;
import com.cos.jwt.filter.MyFilter3;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CorsConfig corsConfig;
    private final UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 필터 적용
        // basicAuthenticationFilter가 동작하기 전에 우리가 만든 필터가 동작할 수 있도록.
        // 근데 여기서 이런 식으로 걸어주지 않아도 된다.
        // 가장 먼저 실행되도록 하고 싶다면 SecurityContextPersistenceFilter보다 더 이전에 등록해주면 된다.

        // http.addFilterBefore(new MyFilter3(), SecurityContextPersistenceFilter.class);

        // api 이용 시 disable 설정
        http.csrf().disable();
        // 인증 정보를 서버에 담아두지 않음. (원래는 서버 측에서 세션에 넣어두는데 이를 비활성화)
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 이러면 내 서버는 cors 정책에서 벗어남 -> 다 허용해주는 것
                // 사실 컨트롤러에 @CrossOrigin을 걸 수 있지만, 인증이 필요하지 않은 요청만 되어서 x
                // 아무튼 인증이 있을 때는, 시큐리티 필터에 등록을 해줘야 한다.
                .addFilter(corsConfig.corsFilter())
                // 기본으로 제공하는 formLogin 비활성화
                .formLogin().disable()
                // http basic auth 사용 x
                .httpBasic().disable()
                // 수동으로 필터 등록
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                // 내부에서 username을 검증하기 위해 userRepository도 보내주기
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository))
                .authorizeRequests()
                .antMatchers("/api/v1/user/**")
                .access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/manager/**")
                .access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
                .antMatchers("/api/v1/admin/**")
                .access("hasRole('ROLE_ADMIN')")
                .anyRequest()
                .permitAll();


    }
}
