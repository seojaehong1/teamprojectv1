package com.example.boardservice.service;

import com.example.boardservice.model.Notice;
import com.example.boardservice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {
    
    private final NoticeRepository noticeRepository;
    
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }
    
    // 관리자 권한 검증 헬퍼 메서드
    private void validateAdminRole(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }

    // 페이징 처리된 공지사항 조회
    public Page<Notice> getAllNoticesWithPaging(Pageable pageable) {
        return noticeRepository.findAllOrderByPinnedAndCreatedAtPaged(pageable);
    }

    // 제목으로 공지사항 검색
    public Page<Notice> searchNoticesByTitle(String keyword, Pageable pageable) {
        String searchPattern = "%" + keyword + "%";
        System.out.println("[NoticeService] 제목 검색 - keyword: " + keyword + ", pattern: " + searchPattern);
        Page<Notice> result = noticeRepository.searchByTitleContaining(searchPattern, pageable);
        System.out.println("[NoticeService] 검색 결과 - 총 " + result.getTotalElements() + "건, 내용: " + result.getContent().size() + "건");
        return result;
    }

    // 내용으로 공지사항 검색
    public Page<Notice> searchNoticesByContent(String keyword, Pageable pageable) {
        String searchPattern = "%" + keyword + "%";
        System.out.println("[NoticeService] 내용 검색 - keyword: " + keyword + ", pattern: " + searchPattern);
        Page<Notice> result = noticeRepository.searchByContentContaining(searchPattern, pageable);
        System.out.println("[NoticeService] 검색 결과 - 총 " + result.getTotalElements() + "건");
        return result;
    }

    // 작성자로 공지사항 검색
    public Page<Notice> searchNoticesByAuthor(String keyword, Pageable pageable) {
        String searchPattern = "%" + keyword + "%";
        System.out.println("[NoticeService] 작성자 검색 - keyword: " + keyword + ", pattern: " + searchPattern);
        Page<Notice> result = noticeRepository.searchByAuthorContaining(searchPattern, pageable);
        System.out.println("[NoticeService] 검색 결과 - 총 " + result.getTotalElements() + "건");
        return result;
    }
    
    // 공지사항 상세 조회 (조회수 증가)
    @Transactional
    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        
        noticeRepository.incrementViewCount(id);
        notice.setViewCount(notice.getViewCount() + 1);
        
        return notice;
    }
    
    // 공지사항 작성 (관리자 전용)
    @Transactional
    public Notice createNotice(Notice notice, String role) {
        validateAdminRole(role);
        return noticeRepository.save(notice);
    }

    // 공지사항 수정 (관리자 전용)
    @Transactional
    public Notice updateNotice(Long id, Notice updatedNotice, String role) {
        validateAdminRole(role);

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        if (updatedNotice.getIsPinned() != null) {
            notice.setIsPinned(updatedNotice.getIsPinned());
        }

        return noticeRepository.save(notice);
    }

    // 공지사항 삭제 (관리자 전용)
    @Transactional
    public void deleteNotice(Long id, String role) {
        validateAdminRole(role);

        if (!noticeRepository.existsById(id)) {
            throw new RuntimeException("공지사항을 찾을 수 없습니다.");
        }

        noticeRepository.deleteById(id);
    }

    // 공지 고정 토글 (관리자 전용)
    @Transactional
    public Notice togglePinned(Long id, String role) {
        validateAdminRole(role);

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notice.setIsPinned(!notice.getIsPinned());
        return noticeRepository.save(notice);
    }
}
