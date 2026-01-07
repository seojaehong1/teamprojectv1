package com.example.boardservice.service;

import com.example.boardservice.model.Board;
import com.example.boardservice.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {
    
    private final BoardRepository boardRepository;
    
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
    
    // 전체 게시글 조회 (최신순)
    public List<Board> getAllBoards() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    // 게시글 상세 조회
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }
    
    // 게시글 작성
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }
    
    // 게시글 수정
    public Board updateBoard(Long id, Board updatedBoard, String userId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (!board.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 게시글만 수정할 수 있습니다.");
        }

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());

        return boardRepository.save(board);
    }

    // 게시글 삭제
    public void deleteBoard(Long id, String userId) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!board.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 게시글만 삭제할 수 있습니다.");
        }

        boardRepository.deleteById(id);
    }

    // 특정 사용자의 게시글 조회
    public List<Board> getBoardsByUserId(String userId) {
        return boardRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
