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

    private final Map<String, Long> lastSendTime = new ConcurrentHashMap<>();
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // 인증코드 이메일 발송
    public void sendVerificationEmail(String toEmail) {
        String code = generateCode();

        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
        verificationCodes.put(toEmail, new VerificationCode(code, expiryTime));
        lastSendTime.put(toEmail, System.currentTimeMillis());

        System.out.println("========================================");
        System.out.println("Email: " + toEmail);
        System.out.println("Verification Code: " + code);
        System.out.println("========================================");


        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[TORI COFFEE] 이메일 인증코드");
            message.setText("안녕하세요!\n\n"
                    + "회원가입 인증코드: " + code + "\n\n"
                    + "이 코드는 5분간 유효합니다.\n"
                    + "본인이 요청하지 않은 경우 이 메일을 무시해주세요.");

            mailSender.send(message);
            System.out.println("Email sent successfully!");

        } catch (Exception e) {
            // 이메일 발송 실패해도 인증코드는 저장됨 (개발 모드에서 콘솔로 확인 가능)
            System.out.println("Email sending failed (check console for code): " + e.getMessage());
        }
    }

    // 임시 비밀번호 발송
    public void sendTempPasswordEmail(String toEmail, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[커피 주문 서비스] 임시 비밀번호 발급");
            message.setText("안녕하세요!\n\n"
                    + "임시 비밀번호가 발급되었습니다.\n\n"
                    + "임시 비밀번호: " + tempPassword + "\n\n"
                    + "로그인 후 반드시 비밀번호를 변경해주세요.");

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage(), e);
        }
    }

    public Long checkResendLimit(String email) {
        Long lastTime = lastSendTime.get(email);
        if (lastTime != null) {
            long elapsed = (System.currentTimeMillis() - lastTime) / 1000;
            if (elapsed < 60) {
                return 60 - elapsed;
            }
        }
        return null;
    }

    /**
     * 인증코드 검증
     * @return "SUCCESS" - 인증 성공
     *         "EXPIRED" - 인증코드 만료
     *         "INVALID" - 인증코드 불일치
     *         "NOT_FOUND" - 인증코드 없음
     */
    public String verifyCode(String email, String code) {
        VerificationCode stored = verificationCodes.get(email);

        if (stored == null) {
            return "NOT_FOUND";
        }

        if (System.currentTimeMillis() > stored.expiryTime) {
            verificationCodes.remove(email);
            return "EXPIRED";
        }

        if (stored.code.equals(code)) {
            verificationCodes.remove(email);
            lastSendTime.remove(email);
            return "SUCCESS";
        }

        return "INVALID";
    }

    private static class VerificationCode {
        String code;
        long expiryTime;

        VerificationCode(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
