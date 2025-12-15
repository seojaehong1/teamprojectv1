package com.du.boardservice.service;

import com.du.boardservice.model.Board;
import com.du.boardservice.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {
    
    private final BoardRepository boardRepository;
    
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
    
    // 전체 게시글 조회 (고정 게시글이 먼저)
    public List<Board> getAllBoards() {
        List<Board> pinnedBoards = boardRepository.findByIsPinnedTrueOrderByCreatedAtDesc();
        List<Board> normalBoards = boardRepository.findByIsPinnedFalseOrderByCreatedAtDesc();
        
        List<Board> allBoards = new ArrayList<>();
        allBoards.addAll(pinnedBoards);
        allBoards.addAll(normalBoards);
        
        return allBoards;
    }
    
    // 게시글 상세 조회 (조회수 증가)
    @Transactional
    public Board getBoardById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 조회수 증가
        boardRepository.incrementViewCount(id);
        board.setViewCount(board.getViewCount() + 1);
        
        return board;
    }
    
    // 게시글 작성
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }
    
    // 게시글 수정
    public Board updateBoard(Long id, Board updatedBoard, String author) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 작성자 본인만 수정 가능
        if (!board.getAuthor().equals(author)) {
            throw new RuntimeException("본인의 게시글만 수정할 수 있습니다.");
        }
        
        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        
        return boardRepository.save(board);
    }
    
    // 게시글 삭제
    public void deleteBoard(Long id, String author) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 작성자 본인만 삭제 가능
        if (!board.getAuthor().equals(author)) {
            throw new RuntimeException("본인의 게시글만 삭제할 수 있습니다.");
        }
        
        boardRepository.deleteById(id);
    }
    
    // 상단 고정 토글 (관리자 기능)
    @Transactional
    public Board togglePin(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        board.setIsPinned(!board.getIsPinned());
        return boardRepository.save(board);
    }
    
    // 좋아요 증가
    @Transactional
    public void increaseLikeCount(Long id) {
        boardRepository.incrementLikeCount(id);
    }
    
    // 특정 작성자의 게시글 조회
    public List<Board> getBoardsByAuthor(String author) {
        return boardRepository.findByAuthorOrderByCreatedAtDesc(author);
    }
}
