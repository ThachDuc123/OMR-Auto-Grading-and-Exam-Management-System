package com.example.dat301mk.repository;

import com.example.dat301mk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUsernameAndDeletedFalse(String username);
    Optional<Users> findByEmailAndDeletedFalse(String email);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUsername(String username);
}
