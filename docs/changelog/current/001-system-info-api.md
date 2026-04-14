# 서버 정보 조회 API 추가

## 요약 (비개발자용)
서버의 현재 상태와 버전 정보를 확인할 수 있는 기능이 추가되었습니다.
운영팀에서 서버가 정상 동작하는지, 어떤 버전이 배포되어 있는지 확인할 때 사용할 수 있습니다.

## 변경 내용

### 추가된 API
- **GET /api/system/info**: 서버 정보 조회

### 응답 예시
```json
{
  "success": true,
  "message": "서버 정보 조회 성공",
  "data": {
    "serviceName": "product-service",
    "version": "1.0.0",
    "javaVersion": "17.0.1",
    "activeProfile": "default",
    "serverPort": 8001
  }
}
```

## 기술 상세

### 추가된 파일
| 파일 | 설명 |
|------|------|
| `SystemController.java` | API 엔드포인트 |
| `SystemInfoDto.java` | 응답 데이터 구조 |
| `SystemControllerTest.java` | 단위 테스트 |

### 테스트 커버리지
- 서버 정보 조회 성공 테스트
- Java 버전 형식 검증 테스트

## 영향 범위
- product-service만 해당
- 기존 API에 영향 없음
- 인증 불필요 (공개 엔드포인트)