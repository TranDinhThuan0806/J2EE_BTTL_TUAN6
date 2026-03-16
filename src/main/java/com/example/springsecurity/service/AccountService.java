package com.example.springsecurity.service;

import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.springsecurity.model.Account;
import com.example.springsecurity.model.Role;
import com.example.springsecurity.repository.AccountRepository;
import com.example.springsecurity.repository.RoleRepository;

@Service
@Primary
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initData() {
        // Tạo role USER nếu chưa có
        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> {
                Role r = new Role();
                r.setName("USER");
                return roleRepository.save(r);
            });

        // Tạo role ADMIN nếu chưa có
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> {
                Role r = new Role();
                r.setName("ADMIN");
                return roleRepository.save(r);
            });

        // Tạo hoặc cập nhật tài khoản user/123456
        accountRepository.findByLoginName("user").ifPresentOrElse(
            existingUser -> {
                boolean needsSave = false;

                // Fix mật khẩu nếu không phải BCrypt
                if (!existingUser.getPassword().startsWith("$2a$") &&
                    !existingUser.getPassword().startsWith("$2b$")) {
                    existingUser.setPassword(passwordEncoder.encode("123456"));
                    needsSave = true;
                    System.out.println("✅ Đã cập nhật mật khẩu BCrypt cho: user");
                }

                // Fix role USER nếu chưa được gán
                boolean hasUserRole = existingUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("USER"));
                if (!hasUserRole) {
                    existingUser.getRoles().add(userRole);
                    needsSave = true;
                    System.out.println("✅ Đã gán role USER cho: user");
                }

                if (needsSave) accountRepository.save(existingUser);
            },
            () -> {
                Account user = new Account();
                user.setLogin_name("user");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRoles(Set.of(userRole));
                accountRepository.save(user);
                System.out.println("✅ Đã tạo tài khoản: user / 123456");
            }
        );

        // Tạo hoặc cập nhật tài khoản admin/admin123
        accountRepository.findByLoginName("admin").ifPresentOrElse(
            existingAdmin -> {
                boolean needsSave = false;

                // Fix mật khẩu nếu không phải BCrypt
                if (!existingAdmin.getPassword().startsWith("$2a$") &&
                    !existingAdmin.getPassword().startsWith("$2b$")) {
                    existingAdmin.setPassword(passwordEncoder.encode("admin123"));
                    needsSave = true;
                    System.out.println("✅ Đã cập nhật mật khẩu BCrypt cho: admin");
                }

                // Xóa tất cả role cũ và chỉ gán role ADMIN
                existingAdmin.getRoles().clear();
                existingAdmin.getRoles().add(adminRole);
                needsSave = true;
                System.out.println("✅ Đã gán role ADMIN cho: admin");

                if (needsSave) accountRepository.save(existingAdmin);
            },
            () -> {
                Account admin = new Account();
                admin.setLogin_name("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                accountRepository.save(admin);
                System.out.println("✅ Đã tạo tài khoản: admin / admin123");
            }
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Account account = accountRepository.findByLoginName(username)
            .orElseThrow(() -> new RuntimeException("Could not find user: " + username));

        Set<SimpleGrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
            account.getLogin_name(),
            account.getPassword(),
            authorities
        );
    }
}
