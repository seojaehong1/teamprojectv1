package com.du.adminservice.config;

import com.du.adminservice.model.Inquiry;
import com.du.adminservice.repository.InquiryRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataInitializer {

    private final InquiryRepository inquiryRepository;

    public TestDataInitializer(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        // 문의 데이터 추가
        if (inquiryRepository.count() == 0) {
            Inquiry inquiry1 = new Inquiry();
            inquiry1.setTitle("배송 문의드립니다");
            inquiry1.setContent("언제 배송되는지 궁금합니다.");
            inquiry1.setUserId("qwer123");
            inquiry1.setStatus("PENDING");
            inquiryRepository.save(inquiry1);

            Inquiry inquiry2 = new Inquiry();
            inquiry2.setTitle("회원 정보 수정 문의");
            inquiry2.setContent("회원 정보를 수정하고 싶은데 어떻게 해야 하나요?");
            inquiry2.setUserId("qwer123");
            inquiry2.setStatus("ANSWERED");
            inquiry2.setAnswer("회원정보 페이지에서 수정 가능합니다.");
            inquiry2.setAnsweredBy("admin");
            inquiryRepository.save(inquiry2);
        }
    }
}
