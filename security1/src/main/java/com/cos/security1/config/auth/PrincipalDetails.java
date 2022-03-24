package com.cos.security1.config.auth;

// 시큐리티가 /login을 낚아채서 로그인을 진행할 텐데,
// 이때 로그인 완료 시 시큐리티 session을 만들어 준다.
// Security ContextHolder에 세션 정보를 저장한다.
// 이때, 이 안에는 Authentication 객체가 들어갈 수 있으며,
// Authentication 객체 안에는 User 정보가 있어야 한다.
// 이때, User 오브젝트의 타입은 UserDetails 타입 객체여야 한다.

import com.cos.security1.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// Security Session 영역 => Authentication 객체 => UserDetails 타입 필요.
// 이런 식으로 진행할 예정.

/**
 * <정리>
 * PrincipalDetails를 만든 이유.
 * 시큐리티 세션이 가질 수 있는 Authentication 객체에 UserDetails와 OAuth2User가 들어갈 수 있는데,
 * 우리가 회원가입을 하게 되면 User 오브젝트가 필요하게 된다.
 * 근데 위의 2가지 타입은 user 오브젝트를 포함하지 않기 때문에,
 * 두 타입을 상속받은 principalDetails를 만들어서 그 안에 user 오브젝트를 넣어두었다.
 * 이러면 세션 정보 접근 시 user 오브젝트에 접근이 가능해진다.
 */

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    // 일반 로그인 시 사용하는 생성자
    public PrincipalDetails(User user) {
        this.user = user;
    }

    // OAuth 로그인 시 사용하는 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return attributes.get("sub").toString();
    }

    // 해당 유저의 권한 리턴
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀 번호 오래 사용했는지
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        // 예를 들어 사이트에서 1년 동안 회원이 로그인하지 않은 경우 휴면 계정으로 전환할 때
        // 현재시간에서 최종 로그인 시간 (이때는 user의 필드에 로그인 시간 관련 필드 추가)
        // 을 뺀 값에 대해 일정 기간을 조건을 줘서 true를 반환해주면 된다!
        return true;
    }


    // 이런 식으로 상속받게 된다면 이제 Authentication 객체 안에는
    // PrincipalDetails 객체를 넣을 수 있게 된다.
}

/**
 * Context Holder 관련 조금 더 찾아본 결과
 * -> 인증된 사용자 정보인 Principal을 Authentication에서 관리하고,
 * 이러한 Authentication은 SecurityContext가 관리하며,
 * SecurityContext는 SecurityContextHolder가 관리한다고 한다.
 * 이때, 이 내부 구조는 threadLocal로서, 한 스레드 내에서 공용으로 저장소가 사용되며,
 * 가장 하위인 authentication을 하나의 스레드 내에서 공유 가능하다는 점이다.
 *
 * 즉, SecurityContextHolder는 Authentication을 담고 있는 Holder이다.
 * cf) https://ohtaeg.tistory.com/8
 *
 */