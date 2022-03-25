package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;


// spring security에 usernamePasswordAuthenticationFilter가 있는데,
// 이때 /login 요청을 통해 username, password를 전송하면 (post)
// usernamePasswordAuthenticationFilter가 동작한다.
// 근데 우리는 지금 formLogin()을 비활성화해서 동작 x
// 그래서 시큐리티 필터에 수동으로 등록해줘야 함!

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    // /login 요청 시 로그인 시도를 위해 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter 진입");

        // 1. username / pwd를 받아서
        try {
            /* 얘는 x-www-form-urlencoded 방식
            BufferedReader br = request.getReader();
            String input = null;
            while((input = br.readLine()) != null)
                System.out.println(input);
            // 출력 결과: username=jw&password=1234
            // 즉, request에 담겨져 있음.
            */

            // json으로 데이터가 들어온다면 jackson의 objectMapper 사용하기
            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);
            // user = User(id=null, username=jw, password=1234, roles=null)
            System.out.println("user = " + user);


            // 2. 정상인지 로그인 시도해보기
            // 먼저 토큰을 만들어준 다음에,
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), user.getPassword());

            // 이때, authenticationManager로 로그인 시도를 하면, PrincipalDetailsService가 호출된다.
            // 이때 loadUserByUsername이 자동으로 실행됨.
            // 정상이면 authentication이 리턴.
            // -> 인증이 완료되면(토큰을 만들어서 했던 로그인 시도가 성공하면) authentication에 로그인 정보가 담긴다!
            // => 이는 곧 DB에 있는 username과 password가 일치한다는 말
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 3. 리턴된 principalDetails를 세션에 담고 (이래야 권한 관리가 가능)
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            // 출력 결과를 보면, 성공적으로 로그인이 되었음을 알 수 있다.
            // principalDetails.getUsername() = jw
            System.out.println("principalDetails.getUsername() = " + principalDetails.getUsername());

            // 이후, authentication 객체를 session 영역에 저장해야 하는데,
            // 이를 return을 통해서 하는 것.
            // JWT 토큰을 사용하면서 세션을 만들 필요는 사실 없지만, 권한 처리를 위해 session에 넣어주는 것.

            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // attemptAuthentication 실행 후, 정상적으로 인증이 실행되었으면
    // successfulAuthentication 함수 실행.
    // JWT 토큰을 만들어서 request를 요청한 사용자에게 JWT를 response 해주면 된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("JwtAuthenticationFilter.successfulAuthentication");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // JWT 토큰 만들기
        String jwtToken = JWT.create()
                // 토큰 이름
                .withSubject(principalDetails.getUsername())
                // 토큰의 만료 시간 = 현재 시간 + 10분
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
                // 넣고 싶은 key-value 값들은 여기서 넣어주면 된다
                .withClaim("id", principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                // secret은 서버만 알고 있는 고유한 값. -> Hash 암호화 방식 사용
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        // response
        // postman으로 응답 header의 authorization 부분을 보면 다음과 같이 들어와있다.
        // Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqdyB0b2tlbiIsImlkIjoxLCJleHAiOjE2NDgxOTEwNzEsInVzZXJuYW1lIjoiancifQ._ART1t0H2O1oMgytIS8mlzvIEK4VR7jZ4NvVel5Z-rUE4DceARjFcjQuiFybevwbvMijPqQPm8mMb7uXybCIUw
        // 이게 바로 만들어진 jwt token이다.

        // 이제 우리는 이를 이용해서 중요한 정보에 접근할 수 있도록, 필터를 설정해줄 수 있다.
        // 다시 말하면, 우리가 만들어야 하는 필터는 클라이언트가 보내는 JST 필터가 유효한지 판단하는 필터!
        // 지금 흐름) (클) id,pwd 로그인 -> 정상 -> (서) jwt 토큰 생성 및 반환 -> (클) 요청할 때마다 jwt 토큰 함께 보냄 -> (서) jwt 토큰에 대한 확인 필요
        response.addHeader(JwtProperties.HEADER_STRING,
                JwtProperties.TOKEN_PREFIX + jwtToken);
    }
}
