package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.config.repository.UserRepository;
import com.cos.jwt.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 시큐리티가 filter를 가지고 있는데, 그 필터 중에 BasicAuthenticationFilter라는 것이 있다.
 * 권한이나 인증이 필요한 특정 주소를 요청하면, 위 필터를 무조건 타게 되는데,
 * 만약 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않는다.
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    // 인증이나 권한이 필요한 주소요청이 있는데 해당 필터를 타게 된다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("인증이나 권한이 필요한 주소 요청");
        // 클라이언트는 authorization에 서버가 생성해준 JWT 토큰을 담아서 보내줄 텐데,
        // 여기서 우리는 getHeader를 통해 jwt 토큰 값을 뽑아낸 다음
        // 검증을 해서 정상적인 사용자인지 확인을 해야 한다.
        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        System.out.println("jwtHeader = " + jwtHeader);

        // header가 있는지 확인하기
        if(jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // 토큰 검증
        // 토큰은 bearer + jwtToken 형식으로 되어 있기 때문에, 앞에 Bearer를 제거해준다.
        String jwtToken = request.getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX, "");

        // JwtAuthenticationFiler에서 했던 jwt 토큰 생성 과정을 역으로 진행한다고 생각하자.
        String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET))
                .build()
                // 서명 진행
                .verify(jwtToken)
                // 이런 식으로 넣었던 username을 가져올 수 있다.
                .getClaim("username")
                .asString();

        /**
         * 삽질한 내용 ㅠ
         * getClaim으로 받아오면 Claim형으로 리턴되는데, 이때 Claim은 interface였다...
         * claim interface에 asString이라고 아예 string을 반환해주는 애가 따로 있었음 ㅠㅠㅠㅠㅠ 하
         * toSring으로 쓰면 안 된다...!
         */

        // 서명이 정상적으로 진행됐다면 username에 값이 들어있을 것.
        if (username != null) {
            User userEntity = userRepository.findByUsername(username);


            PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

            // 강제로 authentication 객체 만들기
            Authentication authentication
                    = new UsernamePasswordAuthenticationToken(
                            // 컨트롤러에서 DI해서 쓸 때 사용하기 편하게 하도록
                            principalDetails,
                            // 패스워드는 모르기 때문에 null, 어차피 여기서 인증하는 것 x
                            null,
                            principalDetails.getAuthorities());

            // 시큐리티를 저장할 수 있는 세션 공간 찾기
            SecurityContextHolder.getContext()
                    // 강제로 시큐리티 세션에 접근하여 authentication 객체를 저장해주기
                    .setAuthentication(authentication);

            chain.doFilter(request, response);
        }
    }
}
