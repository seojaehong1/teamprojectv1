package com.example.member.config;

import com.example.member.model.Member;
import com.example.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataLoader(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("초기 회원 데이터 확인 중...");

        int created = 0;

        // 관리자 계정
        if (!memberRepository.existsByUserId("admin")) {
            Member admin = new Member();
            admin.setUserId("admin");
            admin.setName("관리자");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUserType("admin");
            memberRepository.save(admin);
            System.out.println("- 관리자 계정 생성: admin / admin123");
            created++;
        }

        // 일반회원 - 홍길동
        if (!memberRepository.existsByUserId("hong")) {
            Member member1 = new Member();
            member1.setUserId("hong");
            member1.setName("홍길동");
            member1.setEmail("hong@example.com");
            member1.setPassword(passwordEncoder.encode("1234"));
            member1.setUserType("member");
            memberRepository.save(member1);
            System.out.println("- 홍길동 계정 생성: hong / 1234");
            created++;
        }

        // 점주 - 김점주
        if (!memberRepository.existsByUserId("kimstore")) {
            Member storeOwner = new Member();
            storeOwner.setUserId("kimstore");
            storeOwner.setName("김점주");
            storeOwner.setEmail("kimstore@example.com");
            storeOwner.setPassword(passwordEncoder.encode("1234"));
            storeOwner.setUserType("store_owner");
            memberRepository.save(storeOwner);
            System.out.println("- 김점주 계정 생성: kimstore / 1234");
            created++;
        }

        // 일반회원 - 이사용자
        if (!memberRepository.existsByUserId("lee")) {
            Member member2 = new Member();
            member2.setUserId("lee");
            member2.setName("이사용자");
            member2.setEmail("lee@example.com");
            member2.setPassword(passwordEncoder.encode("1234"));
            member2.setUserType("member");
            memberRepository.save(member2);
            System.out.println("- 이사용자 계정 생성: lee / 1234");
            created++;
        }

        // 일반회원 - 박고객
        if (!memberRepository.existsByUserId("park")) {
            Member member3 = new Member();
            member3.setUserId("park");
            member3.setName("박고객");
            member3.setEmail("park@example.com");
            member3.setPassword(passwordEncoder.encode("1234"));
            member3.setUserType("member");
            memberRepository.save(member3);
            System.out.println("- 박고객 계정 생성: park / 1234");
            created++;
        }

        if (created > 0) {
            System.out.println("초기 회원 데이터 " + created + "개 생성 완료!");
        } else {
            System.out.println("모든 초기 회원 데이터가 이미 존재합니다.");
        }
    }
}
