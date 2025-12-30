package com.example.member.config;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DataInitializer {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (memberRepository.count() == 0) {
            // 관리자 계정
            Member admin = new Member();
            admin.setUsername("관리자");
            admin.setUserId("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setBirthDate(LocalDate.of(1990, 1, 1));
            admin.setPhoneNum("010-0000-0000");
            admin.setProvider(Member.Provider.DEFAULT);
            admin.setRole(Member.Role.ADMIN);
            memberRepository.save(admin);
            //일반 사용자 2개 제거 *관리자만 필요*
        }
    }
}
