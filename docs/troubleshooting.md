# Troubleshooting Guide

> **트러블슈팅 기록. 한 번 겪은 문제를 다시 겪지 않기 위함.**

---

## 기록 포맷

```markdown
### [서비스명] 문제 제목

**날짜**: YYYY-MM-DD

**증상**:
- 어떤 현상이 발생했는지
- 에러 메시지, 로그 등

**원인**:
- 왜 이 문제가 발생했는지

**해결**:
- 어떻게 해결했는지
- 코드 변경, 설정 변경 등

**주의**:
- 다시 발생하지 않도록 주의할 점
- 관련 규칙이나 체크리스트

**관련 파일**: (선택)
- 변경된 파일 경로
```

---

## 문제 기록

*아직 기록된 트러블슈팅 사례가 없습니다.*

<!--
예시:

### [order-service] RabbitMQ 연결 실패

**날짜**: 2024-XX-XX

**증상**:
```
org.springframework.amqp.AmqpConnectException:
java.net.ConnectException: Connection refused
```
- order-service 시작 시 RabbitMQ 연결 실패
- 주문 생성 API 호출 시 500 에러

**원인**:
- Docker Compose에서 RabbitMQ보다 order-service가 먼저 시작됨
- `depends_on`은 컨테이너 시작만 보장, 서비스 준비 상태는 미보장

**해결**:
1. docker-compose.yml에 RabbitMQ healthcheck 추가
```yaml
rabbitmq:
  healthcheck:
    test: rabbitmq-diagnostics -q ping
    interval: 10s
    timeout: 5s
    retries: 5
```

2. order-service에 depends_on condition 추가
```yaml
order-service:
  depends_on:
    rabbitmq:
      condition: service_healthy
```

**주의**:
- 새 서비스 추가 시 RabbitMQ 의존성 있으면 healthcheck 조건 필수
- 로컬 개발 시 RabbitMQ 먼저 실행 확인

**관련 파일**: `docker-compose.yml`

---

### [gateway-service] 특정 API 404 Not Found

**날짜**: 2024-XX-XX

**증상**:
- `/api/admin/products` 호출 시 404 에러
- admin-service로 라우팅되어야 하는데 실패

**원인**:
- Gateway 라우팅 순서 문제
- `/api/admin/**` 라우트가 `/api/admin/products/**` 보다 먼저 정의됨
- 일반적인 패턴이 구체적인 패턴보다 먼저 매칭됨

**해결**:
- application.properties에서 라우팅 순서 변경
- 구체적인 경로 먼저, 일반적인 경로 나중에

```properties
# 변경 전 (잘못됨)
routes[13].id=admin-service
routes[13].predicates[0]=Path=/api/admin/**
routes[14].id=admin-product-service
routes[14].predicates[0]=Path=/api/admin/products/**

# 변경 후 (올바름)
routes[13].id=admin-product-service
routes[13].predicates[0]=Path=/api/admin/products/**
routes[14].id=admin-service
routes[14].predicates[0]=Path=/api/admin/**
```

**주의**:
- Gateway 라우트 추가 시 순서 확인 필수
- `/**` catch-all 패턴은 반드시 맨 마지막에 위치
- CLAUDE.md 섹션 4.5 (Gateway 라우팅 규칙) 참고

**관련 파일**: `gateway-service/src/main/resources/application.properties`

---

### [member-service] JWT 토큰 검증 실패

**날짜**: 2024-XX-XX

**증상**:
```
io.jsonwebtoken.security.SignatureException:
JWT signature does not match locally computed signature
```
- 다른 서비스에서 JWT 검증 시 401 Unauthorized

**원인**:
- 서비스 간 JWT_SECRET 환경변수 불일치
- member-service에서 발급한 토큰을 inventory-service에서 검증 시 다른 Secret 사용

**해결**:
1. K8s Secrets에 JWT_SECRET 통일
2. 모든 서비스가 동일한 Secret 참조하도록 설정

```yaml
# k8s/base/secrets.yaml
JWT_SECRET: "동일한-32자-이상-시크릿-키"

# 각 서비스 deployment.yaml
- name: JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: db-secrets
      key: JWT_SECRET
```

**주의**:
- JWT_SECRET은 모든 JWT 사용 서비스에서 동일해야 함
- Secret 변경 시 모든 관련 서비스 재배포 필요
- Secret Key 길이: 최소 32자 (HS512 알고리즘)

**관련 파일**:
- `k8s/base/secrets.yaml`
- `member-service/src/main/resources/application.properties`
-->