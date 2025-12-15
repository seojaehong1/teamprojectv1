package com.example.member.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    
    // 인증코드 임시 저장 (실제 서비스에서는 Redis 권장)
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 6자리 인증코드 생성
    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // 인증코드 이메일 발송
    public void sendVerificationEmail(String toEmail) {
        try {
            String code = generateCode();
            
            // 5분 후 만료
            long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
            verificationCodes.put(toEmail, new VerificationCode(code, expiryTime));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@coffeeservice.com"); // 발신자 설정 (선택사항)
            message.setTo(toEmail);
            message.setSubject("[커피 주문 서비스] 이메일 인증코드");
            message.setText("안녕하세요!\n\n"
                    + "회원가입 인증코드: " + code + "\n\n"
                    + "이 코드는 5분간 유효합니다.\n"
                    + "본인이 요청하지 않은 경우 이 메일을 무시해주세요.");

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }

    // 인증코드 검증
    public boolean verifyCode(String email, String code) {
        VerificationCode stored = verificationCodes.get(email);
        
        if (stored == null) {
            return false;
        }
        
        // 만료 체크
        if (System.currentTimeMillis() > stored.expiryTime) {
            verificationCodes.remove(email);
            return false;
        }
        
        // 코드 일치 체크
        if (stored.code.equals(code)) {
            verificationCodes.remove(email);  // 사용 후 삭제
            return true;
        }
        
        return false;
    }

    // 내부 클래스: 인증코드 + 만료시간
    private static class VerificationCode {
        String code;
        long expiryTime;

        VerificationCode(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
