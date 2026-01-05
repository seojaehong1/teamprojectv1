package com.example.member.service;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegisterService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Member register(String name, String userId, String password, String email) {
        Member member = new Member();
        member.setName(name);
        member.setUserId(userId);
        member.setPassword(passwordEncoder.encode(password));
        member.setEmail(email);
        member.setUserType("member");

        return memberRepository.save(member);
    }
}
