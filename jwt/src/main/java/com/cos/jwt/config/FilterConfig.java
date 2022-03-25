package com.cos.jwt.config;

import com.cos.jwt.filter.MyFilter1;
import com.cos.jwt.filter.MyFilter2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    // 이런 식으로 필터를 걸어줄 수도 있음! - MyFilter1 실행
    @Bean
    public FilterRegistrationBean<MyFilter1> filter1() {
        FilterRegistrationBean<MyFilter1> bean
                = new FilterRegistrationBean<>(new MyFilter1());

        bean.addUrlPatterns("/*");
        // 낮은 번호가 필터층에서 가장 먼저 실행된다.
        bean.setOrder(0);
        return bean;
    }

    // 필터 1개 더.
    // setOrder 순서로 실행이 된다.
    @Bean
    public FilterRegistrationBean<MyFilter2> filter2() {
        FilterRegistrationBean<MyFilter2> bean
                = new FilterRegistrationBean<>(new MyFilter2());

        bean.addUrlPatterns("/*");
        // 낮은 번호가 필터층에서 가장 먼저 실행된다.
        bean.setOrder(1);
        return bean;
    }

    /**
     * sout 출력)
     * Filter 3
     * Filter 1
     * Filter 2
     *
     * 이를 통해, 시큐리티 필터에 등록한 필터가 먼저 실행되는 걸 알 수 있다.
     * (참고로, addfilterBefore나 atfer나 어떤 것이든 시큐리티 필터가 먼저 실행됨!)
     *
     */
}
