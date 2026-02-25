package com.example.boardservice.config;

import com.example.boardservice.model.Notice;
import com.example.boardservice.repository.NoticeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final NoticeRepository noticeRepository;

    public DataLoader(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    public void run(String... args) {
        // 이미 데이터가 있으면 추가하지 않음
        if (noticeRepository.count() > 0) {
            return;
        }

        // 임시 공지사항 11개 추가
        createNotice("[중요] 서비스 이용약관 변경 안내",
            "안녕하세요. 서비스 이용약관이 2024년 1월 1일부터 변경됩니다.\n\n주요 변경 내용:\n1. 개인정보 처리방침 업데이트\n2. 결제 관련 조항 수정\n3. 환불 정책 변경\n\n자세한 내용은 이용약관 페이지에서 확인해 주세요.",
            "관리자", true);

        createNotice("[공지] 시스템 정기 점검 안내",
            "시스템 안정화를 위한 정기 점검이 예정되어 있습니다.\n\n- 일시: 매주 화요일 03:00 ~ 05:00\n- 내용: 서버 점검 및 업데이트\n\n점검 시간 동안 서비스 이용이 일시적으로 제한될 수 있습니다.",
            "관리자", true);

        createNotice("신규 메뉴 출시 안내",
            "고객님들의 성원에 힘입어 신규 메뉴를 출시합니다!\n\n신메뉴:\n- 프리미엄 아메리카노\n- 딸기 라떼\n- 초코 브라우니\n\n많은 관심 부탁드립니다.",
            "관리자", false);

        createNotice("여름 시즌 이벤트 안내",
            "무더운 여름을 시원하게! 여름 시즌 이벤트를 진행합니다.\n\n- 기간: 7월 1일 ~ 8월 31일\n- 혜택: 아이스 음료 전품목 10% 할인\n\n시원한 여름 되세요!",
            "관리자", false);

        createNotice("회원 등급 제도 변경 안내",
            "더 나은 혜택을 위해 회원 등급 제도가 변경됩니다.\n\n변경 내용:\n- 브론즈: 월 3만원 이상 구매\n- 실버: 월 5만원 이상 구매\n- 골드: 월 10만원 이상 구매\n- VIP: 월 20만원 이상 구매",
            "관리자", false);

        createNotice("배달 서비스 지역 확대",
            "고객님들의 요청에 따라 배달 서비스 지역이 확대됩니다.\n\n추가 지역:\n- 강남구 전역\n- 서초구 전역\n- 송파구 일부\n\n더 가까이에서 만나뵙겠습니다.",
            "관리자", false);

        createNotice("앱 업데이트 안내 (v2.0)",
            "더 편리한 사용을 위해 앱이 업데이트 되었습니다.\n\n주요 변경사항:\n- UI/UX 개선\n- 주문 속도 향상\n- 버그 수정\n\n최신 버전으로 업데이트 해주세요.",
            "관리자", false);

        createNotice("추석 연휴 운영 안내",
            "추석 연휴 기간 운영 안내드립니다.\n\n- 연휴 기간: 9월 28일 ~ 10월 1일\n- 운영 시간: 10:00 ~ 18:00 (단축 운영)\n\n즐거운 한가위 되세요!",
            "관리자", false);

        createNotice("포인트 적립률 변경 안내",
            "포인트 적립률이 변경됩니다.\n\n- 변경 전: 구매금액의 3%\n- 변경 후: 구매금액의 5%\n- 적용일: 다음 달 1일부터\n\n더 많은 혜택을 누려보세요!",
            "관리자", false);

        createNotice("고객센터 운영시간 변경",
            "고객센터 운영시간이 변경됩니다.\n\n- 변경 전: 평일 09:00 ~ 18:00\n- 변경 후: 평일 08:00 ~ 20:00, 주말 10:00 ~ 17:00\n\n더 나은 서비스로 보답하겠습니다.",
            "관리자", false);

        createNotice("개인정보 처리방침 개정 안내",
            "개인정보 처리방침이 개정되었습니다.\n\n주요 개정 내용:\n- 수집하는 개인정보 항목 명확화\n- 개인정보 보관 기간 조정\n- 제3자 제공 동의 절차 개선\n\n자세한 내용은 개인정보처리방침 페이지를 확인해 주세요.",
            "관리자", false);

        System.out.println(">>> 임시 공지사항 11개 생성 완료");
    }

    private void createNotice(String title, String content, String author, boolean isPinned) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setAuthor(author);
        notice.setIsPinned(isPinned);
        notice.setViewCount((int) (Math.random() * 100));
        noticeRepository.save(notice);
    }
}
