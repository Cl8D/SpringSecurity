package com.cos.security1.config.auth;

// 시큐리티가 /login을 낚아채서 로그인을 진행할 텐데,
// 이때 로그인 완료 시 시큐리티 session을 만들어 준다.
// Security ContextHolder에 세션 정보를 저장한다.
// 이때, 이 안에는 Authentication 객체가 들어갈 수 있으며,
// Authentication 객체 안에는 User 정보가 있어야 한다.
// 이때, User 오브젝트의 타입은 UserDetails 타입 객체여야 한다.

import com.cos.security1.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// Security Session 영역 => Authentication 객체 => UserDetails 타입 필요.
// 이런 식으로 진행할 예정.
public class PrincipalDetails implements UserDetails {

    private User user;

    public PrincipalDetails(User user) {
        this.user = user;
    }

    // 해당 유저의 권한 리턴
    // 강의 7:50분까지
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
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