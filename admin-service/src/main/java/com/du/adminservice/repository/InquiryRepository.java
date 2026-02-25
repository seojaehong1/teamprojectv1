package com.du.adminservice.repository;

import com.du.adminservice.model.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 사용자별 조회 (내 문의 목록)
    Page<Inquiry> findByUserId(String userId, Pageable pageable);

    // 상태별 조회
    Page<Inquiry> findByStatus(String status, Pageable pageable);

    // 전체 조회 (답변대기 우선, 그 다음 작성일 기준 내림차순)
    @Query("SELECT i FROM Inquiry i ORDER BY " +
           "CASE WHEN i.status = 'PENDING' THEN 0 ELSE 1 END, " +
           "i.createdAt DESC")
    Page<Inquiry> findAllOrderByStatusAndCreatedAt(Pageable pageable);

    // 검색 기능 (제목, 내용, 작성자) - 답변대기 우선
    @Query("SELECT i FROM Inquiry i WHERE " +
           "(:keyword IS NULL OR " +
           "i.title LIKE %:keyword% OR " +
           "i.content LIKE %:keyword% OR " +
           "i.userId LIKE %:keyword%) " +
           "ORDER BY CASE WHEN i.status = 'PENDING' THEN 0 ELSE 1 END, " +
           "i.createdAt DESC")
    Page<Inquiry> searchInquiries(@Param("keyword") String keyword, Pageable pageable);

    // 상태별 검색 (제목, 내용, 작성자)
    @Query("SELECT i FROM Inquiry i WHERE i.status = :status AND " +
           "(:keyword IS NULL OR " +
           "i.title LIKE %:keyword% OR " +
           "i.content LIKE %:keyword% OR " +
           "i.userId LIKE %:keyword%) " +
           "ORDER BY i.createdAt DESC")
    Page<Inquiry> searchInquiriesByStatus(@Param("status") String status,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
