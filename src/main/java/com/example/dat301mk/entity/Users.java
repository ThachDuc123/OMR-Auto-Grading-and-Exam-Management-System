package com.example.dat301mk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.management.relation.Role;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "id_omr", length = 6, unique = true)
    private String idOmr;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "class_name")
    private String className;

    @Column(name = "role",nullable = false)
    private String role;

    @Column(name = "created_at",insertable = false)
    private Timestamp createdAt;

    @Column(name = "is_deleted",insertable = false)
    private boolean deleted;

    public int getId() {
        return userId;
    }
}
