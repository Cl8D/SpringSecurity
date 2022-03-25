package com.cos.jwt.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter3 implements Filter {
    /**
     * 우리는 이제 id/pw가 정상적으로 들어와서 로그인이 완료되면 토큰을 만들어서
     * 그거를 응답해줄 예정.
     * 요청할 때마다 header의 Authorization의 value값으로 토큰을 가져올 텐데,
     * 이때 이 토큰이 내가 만든 토큰이 맞는지만 검증해주면 된다! (RSA, HS256)
     */

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if(req.getMethod().equals("POST")) {
            // "authorization"에 있는 값 출력해보기
            // headerAuth = hello
            String headerAuth = req.getHeader("Authorization");
            System.out.println("headerAuth = " + headerAuth);
            System.out.println("Filter 3");

            // 임시적으로 token의 값이 cos라고 가정하자.
            if(headerAuth.equals("cos")) {
                // 이때만 필터 체인 발동
                // 이래야 controller가 동작하게 된다!
                filterChain.doFilter(req, res);
            } else {
                PrintWriter out = res.getWriter();
                out.println("인증 안 됨");
            }
        }

        //filterChain.doFilter(servletRequest, servletResponse);
    }
}
