package com.cos.security1.model;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private int id;

    private String username;
    private String password;
    private String email;
    // ROLE_USER, ROLE_ADMIN
    private String role;

    // 어떤 방식으로 로그인하였는지
    // ex) 구글 로그인이라면 provider="google", providerId=sub정보가 들어가도록!
    private String provider;
    private String providerId;


    @CreationTimestamp
    private Timestamp createDate;

    // lombok의 builder. 빌드 패턴을 통해 객체 생성 가능.
    @Builder
    public User(String username, String password, String email, String role, String provider, String providerId, Timestamp createDate) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.createDate = createDate;
    }
}
