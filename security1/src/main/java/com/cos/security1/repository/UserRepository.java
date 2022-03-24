package com.cos.security1.repository;

import com.cos.security1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 기본적으로 JpaRepository는 CRUD 함수를 들고 있다.
// 특히, @Repository가 없어도 jpaRepository 상속 시 빈으로 등록해준다.
public interface UserRepository extends JpaRepository<User, Integer> {
    // 여기서 jpa 사용.
    // findBy 규칙 -> +username
    // 이렇게 되면 자동으로 다음과 같은 쿼리가 날라간다.
    // select * from user where username= 1?
    // ? 자리에는 이 함수의 파라미터인 username이 들어간다.
    public User findByUsername(String username);

    /**
     * cf)
     * public User findByEmail()
     * => select * from user where email = ? 호출.
     */
}
