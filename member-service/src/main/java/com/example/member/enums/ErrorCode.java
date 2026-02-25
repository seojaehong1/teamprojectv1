package com.example.member.enums;

public enum ErrorCode {
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),
    DUPLICATE_USER_ID("이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    INVALID_VERIFICATION_CODE("이메일 인증번호가 일치하지 않습니다."),
    AUTH_FAILED("아이디 또는 비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    BAD_REQUEST("잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
