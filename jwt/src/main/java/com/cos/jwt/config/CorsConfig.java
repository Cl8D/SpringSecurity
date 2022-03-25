package com.cos.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CorsFilter;


/**
 * CORS - Cross-Origin Resource Sharing.
 * 추가적인 http 헤더를 통해 한 origin(protocol-domain-host)에서 실행 중인
 * 웹 어플리케이션이 다른 origin의 선택한 자원에 접근할 수 있는 권한을 부여하도록 하는 것.
 *
 * = 쉽게 말해서, 서로 다른 origin 간에 리소스를 전달하는 방식을 제어하는 체제.
 * = cors를 해결하려면 서버의 특정 헤더인 access-control-allow-origin과 함께 응답해야 함.
 * = 근데 이건 복잡하니까 그냥 filter를 이용해서 해결해보자는 것!
 */

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 내 서버가 응답 시 json을 자바스크립트에서 처리할 수 있도록 할지 설정
        config.setAllowCredentials(true);
        // 모든 ip에 응답을 허용
        config.addAllowedOrigin("*"); // e.g. http://domain1.com
        // 모든 header에 응답을 허용
        config.addAllowedHeader("*");
        // 모든 post/get/put/delete/patch 요청 허용
        config.addAllowedMethod("*");

        // /api로 들어오는 주소들은 모두 이 config 설정 적용
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

}