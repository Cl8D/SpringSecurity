package com.cos.security1.repository;

import com.cos.security1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 기본적으로 JpaRepository는 CRUD 함수를 들고 있다.
// 특히, @Repository가 없어도 jpaRepository 상속 시 빈으로 등록해준다.
public interface UserRepository extends JpaRepository<User, Integer> {

}
