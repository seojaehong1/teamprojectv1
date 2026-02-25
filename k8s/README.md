# Kubernetes 배포 가이드

## 사전 요구사항

1. AWS CLI 설치 및 설정
2. kubectl 설치
3. EKS 클러스터 생성 및 kubeconfig 설정
4. AWS Load Balancer Controller 설치

## 디렉토리 구조

```
k8s/
├── base/           # Namespace, ConfigMap, Secret
├── mysql/          # MySQL StatefulSet (4개)
├── rabbitmq/       # RabbitMQ Deployment
├── services/       # 애플리케이션 서비스 Deployment
├── scripts/        # 배포 스크립트
└── ingress.yaml    # ALB Ingress
```

## 배포 순서

### 1. AWS 설정 변경

`scripts/build-and-push.sh`와 `scripts/deploy.sh`에서 다음 값을 수정:

```bash
AWS_ACCOUNT_ID="YOUR_AWS_ACCOUNT_ID"
AWS_REGION="ap-northeast-2"
```

### 2. ECR 이미지 빌드 및 푸시

```bash
chmod +x k8s/scripts/*.sh
./k8s/scripts/build-and-push.sh
```

### 3. Kubernetes 배포

```bash
./k8s/scripts/deploy.sh
```

### 4. 배포 확인

```bash
kubectl get pods -n tori-app
kubectl get svc -n tori-app
kubectl get ingress -n tori-app
```

## 리소스 삭제

```bash
./k8s/scripts/delete.sh
```

## 서비스 구조

| 서비스 | 포트 | 설명 | 복제본 |
|--------|------|------|--------|
| eureka-server | 8761 | Service Discovery | 1 |
| gateway-service | 8000 | API Gateway | 2 |
| frontend-service | 8005 | Web UI | 2 |
| member-service | 8004 | 회원 관리 | 2 |
| product-service | 8001 | 상품 관리 | 2 |
| order-service | 8002 | 주문 관리 | 2 |
| inventory-service | 8008 | 재고 관리 | 2 |
| board-service | 8006 | 게시판 | 2 |
| admin-service | 8007 | 관리자 | 2 |

## MySQL 데이터베이스

| MySQL | DB명 | 사용 서비스 |
|-------|------|-------------|
| mysql-member | member_db | member, admin, board |
| mysql-product | product_db | product |
| mysql-order | order_db | order |
| mysql-inventory | inventory_db | inventory |

## 스케일링

```bash
# 서비스 스케일 업/다운
kubectl scale deployment frontend-service --replicas=3 -n tori-app
kubectl scale deployment order-service --replicas=5 -n tori-app
```

## 로그 확인

```bash
kubectl logs -f deployment/frontend-service -n tori-app
kubectl logs -f deployment/order-service -n tori-app
```

## 트러블슈팅

```bash
# Pod 상태 확인
kubectl describe pod <pod-name> -n tori-app

# 서비스 엔드포인트 확인
kubectl get endpoints -n tori-app

# ConfigMap 확인
kubectl get configmap app-config -n tori-app -o yaml
```