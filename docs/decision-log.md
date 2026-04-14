# Architecture Decision Log

> **아키텍처 결정 기록. 왜 이렇게 했는지를 기록한다.**

---

## 기록 포맷

```markdown
### ADR-XXX: [결정 제목]

**날짜**: YYYY-MM-DD

**배경**:
- 어떤 문제나 요구사항이 있었는지

**결정**:
- 무엇을 결정했는지

**대안**:
- 고려했던 다른 선택지들
- 왜 선택하지 않았는지

**트레이드오프**:
- 이 결정의 장점
- 이 결정의 단점

**관련 PR/커밋**: (선택)
- PR #123 또는 커밋 해시
```

---

## 결정 기록

### ADR-001: 초기 아키텍처 분석 결과

**날짜**: 2024-04-15

**배경**:
- TORI Coffee 주문/재고 관리 시스템 구축 필요
- 독립적 배포, 확장성, 장애 격리 요구사항

**결정**:

#### 1. 마이크로서비스 아키텍처 (9개 서비스)
| 서비스 | 포트 | 책임 |
|--------|------|------|
| eureka-server | 8761 | 서비스 디스커버리 |
| gateway-service | 8000 | API Gateway, 라우팅 |
| member-service | 8004 | 회원, JWT, OAuth2 |
| product-service | 8001 | 상품/메뉴 관리 |
| order-service | 8002 | 주문, RabbitMQ Producer |
| inventory-service | 8008 | 재고, RabbitMQ Consumer |
| board-service | 8006 | 게시판 |
| admin-service | 8007 | 관리자 기능 |
| frontend-service | 8005 | Thymeleaf SSR |

#### 2. Database per Service (부분 적용)
```
product_db (3307)   → product-service 전용
order_db (3308)     → order-service 전용
inventory_db (3309) → inventory-service 전용
member_db (3306)    → member, board, admin 공유
```

#### 3. Event-Driven Architecture (RabbitMQ)
```
order-service → order.exchange → inventory.order.queue → inventory-service
```
- 주문 생성 시 비동기로 재고 차감
- 느슨한 결합, 장애 허용

#### 4. API Gateway 패턴 (Spring Cloud Gateway)
- 17개 라우트 규칙
- Eureka 기반 `lb://` 로드밸런싱
- CORS 중앙 관리

#### 5. 인증 (JWT + OAuth2)
- Access Token: 1시간, HS512
- Refresh Token: 7일
- 소셜 로그인: Google, Naver

#### 6. 인프라
- Container: Docker Multi-stage Build
- Orchestration: Kubernetes (AWS EKS)
- CI/CD: GitHub Actions → ECR → ArgoCD
- HPA: CPU 70%, Memory 80% 기준 자동 스케일링

**대안**:
| 결정 | 대안 | 미선택 이유 |
|------|------|------------|
| Eureka | K8s Service DNS | 로컬/K8s 양쪽 호환성 |
| RabbitMQ | Kafka | 단순한 메시징 요구사항에 적합 |
| Spring Cloud Gateway | Zuul | WebFlux 비동기 논블로킹 |
| ArgoCD | Flux | 직관적 UI, 학습 곡선 낮음 |

**트레이드오프**:

*장점*:
- 서비스별 독립 배포 및 스케일링
- 장애 격리 (한 서비스 다운이 전체 영향 없음)
- 기술적 자율성 (서비스별 DB, 설정)

*단점*:
- 운영 복잡도 증가 (9개 서비스 + 4개 DB + RabbitMQ)
- 분산 트랜잭션 어려움 (Saga 패턴 필요)
- 네트워크 오버헤드
- member_db 공유로 인한 서비스 간 커플링

**알려진 한계점**:
1. **member_db 공유**: member, board, admin 서비스가 DB 공유 → 독립 배포 제약
2. **모니터링 미구성**: Prometheus/Grafana 설정 필요
3. **분산 트레이싱 없음**: Zipkin/Jaeger 미구성
4. **Circuit Breaker 없음**: Resilience4j 미적용
5. **Rate Limiting 없음**: Gateway에서 DDoS 방어 미구현
6. **DB 마이그레이션 도구 없음**: `ddl-auto=update` 사용 중 (프로덕션 위험)
7. **백업/DR 전략 미정의**: MySQL 백업 스케줄, 재해 복구 절차 없음

**관련 문서**: `docs/architecture.md`

---

*새로운 아키텍처 결정은 위 포맷에 따라 추가하세요.*