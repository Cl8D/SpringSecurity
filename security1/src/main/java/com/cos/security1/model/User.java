package com.cos.security1.model;


import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue
    private int id;

    private String username;
    private String password;
    private String email;
    // ROLE_USER, ROLE_ADMIN
    private String role;

    @CreationTimestamp
    private Timestamp createDate;

}
