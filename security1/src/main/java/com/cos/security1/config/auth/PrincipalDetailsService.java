package com.cos.security1.config.auth;

import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 시큐리티 설정에서 loginProcessingUrl("/login")을 걸어놨는데,
// login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어 있는
// loadUserByUsername 함수가 실행된다.
@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    // security session에는 Authentication 객체 => userDetails 타입.
    // 여기서 리턴된 userDetails는 Authentication 내부로 들어간다.
    // authentication 역시 마찬가지로 security session으로 들어가서,

    // 결과적으로 약간 이런 형태
    // securitySession(Authentication(userDetails))

    // 함수 종료 시 @AuthenticationPrincipal 어노테이션이 생성된다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 여기서 파라미터가 username으로 되어 있는데,
        // 이때 html파일에서 name 옵션으로 준 username과 동일해야 한다.
        // 만약 거기서 name 조건에 이름을 다르게 주었다면,
        // security 설정 파일에서 .usernameParameter("")로 값을 넘겨야 한다.

        User userEntity = userRepository.findByUsername(username);
        if (userEntity != null)
            return new PrincipalDetails(userEntity);

        return null;
    }
}
