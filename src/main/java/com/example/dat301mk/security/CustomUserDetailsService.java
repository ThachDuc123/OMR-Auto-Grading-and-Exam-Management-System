package com.example.dat301mk.security;

import com.example.dat301mk.entity.Users;
import com.example.dat301mk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users appUser = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username)));

        // Build authorities based on role field in Users entity (e.g., "student"/"học sinh" or "teacher"/"giáo viên")
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleRaw = appUser.getRole() != null ? appUser.getRole() : "student";
        String normalized = Normalizer.normalize(roleRaw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // strip diacritics
                .replaceAll("\\s", "")
                .toLowerCase(Locale.ROOT);
        boolean isTeacher = normalized.contains("teacher") || normalized.contains("giaovien");
        if (isTeacher) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }

        return new User(appUser.getUsername(), appUser.getPassword(), authorities);
    }
}
