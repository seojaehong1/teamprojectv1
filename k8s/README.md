# Kubernetes 배포 가이드 (AWS EKS)

## 사전 요구사항

### 필수 도구 설치
| 도구 | 용도 | 설치 링크 |
|------|------|-----------|
| AWS CLI | AWS 서비스 접근 | https://aws.amazon.com/cli/ |
| kubectl | Kubernetes 관리 | https://kubernetes.io/docs/tasks/tools/ |
| eksctl | EKS 클러스터 생성 | https://eksctl.io/installation/ |
| Helm | ALB Controller 설치 | https://helm.sh/docs/intro/install/ |
| Docker | 이미지 빌드 | https://www.docker.com/ |

### AWS IAM 권한
```
- AmazonEKSClusterPolicy
- AmazonEKSWorkerNodePolicy
- AmazonEC2ContainerRegistryFullAccess
- ElasticLoadBalancingFullAccess
- IAMFullAccess (서비스 계정 생성용)
```

## 디렉토리 구조

```
k8s/
├── base/           # Namespace, ConfigMap, Secret
├── mysql/          # MySQL StatefulSet (4개)
├── rabbitmq/       # RabbitMQ Deployment
├── services/       # 애플리케이션 서비스 Deployment
├── hpa/            # Horizontal Pod Autoscaler
├── scripts/        # 배포 스크립트
│   ├── .env.example
│   ├── create-eks-cluster.sh
│   ├── install-alb-controller.sh
│   ├── build-and-push.sh
│   ├── deploy.sh
│   └── delete.sh
└── ingress.yaml    # ALB Ingress
```

## 전체 배포 순서

### Step 0: AWS CLI 설정

```bash
aws configure
# AWS Access Key ID: [IAM에서 발급받은 키]
# AWS Secret Access Key: [IAM에서 발급받은 시크릿]
# Default region name: ap-northeast-2
# Default output format: json

# 설정 확인
aws sts get-caller-identity
```

### Step 1: 환경변수 설정

```bash
# .env.example을 복사하여 .env 파일 생성
cp k8s/scripts/.env.example k8s/scripts/.env

# .env 파일 편집 (필수: AWS_ACCOUNT_ID)
# Windows: notepad k8s/scripts/.env
# Linux/Mac: vi k8s/scripts/.env
```

#### 필수 환경변수
| 변수 | 설명 | 예시 |
|------|------|------|
| AWS_ACCOUNT_ID | AWS 계정 ID (12자리) | 123456789012 |
| AWS_REGION | AWS 리전 | ap-northeast-2 |
| EKS_CLUSTER_NAME | 클러스터 이름 | tori-app-cluster |

### Step 2: EKS 클러스터 생성 (15-20분 소요)

```bash
chmod +x k8s/scripts/*.sh
./k8s/scripts/create-eks-cluster.sh
```

생성되는 리소스:
- EKS 클러스터
- VPC, 서브넷, 보안그룹
- 노드 그룹 (t3.medium x 3)
- IAM 역할

### Step 3: AWS Load Balancer Controller 설치

```bash
./k8s/scripts/install-alb-controller.sh
```

### Step 4: Docker 이미지 빌드 및 ECR 푸시

```bash
./k8s/scripts/build-and-push.sh
```

### Step 5: 애플리케이션 배포

```bash
./k8s/scripts/deploy.sh
```

### Step 6: 배포 확인

```bash
# Pod 상태
kubectl get pods -n tori-app

# 서비스 상태
kubectl get svc -n tori-app

# Ingress (ALB 주소 확인)
kubectl get ingress -n tori-app

# HPA 상태
kubectl get hpa -n tori-app
```

## 서비스 구조

| 서비스 | 포트 | 설명 | 복제본 | HPA Max |
|--------|------|------|--------|---------|
| eureka-server | 8761 | Service Discovery | 1 | - |
| gateway-service | 8000 | API Gateway | 2 | 10 |
| frontend-service | 8005 | Web UI | 2 | 10 |
| member-service | 8004 | 회원 관리 | 2 | 8 |
| product-service | 8001 | 상품 관리 | 2 | 8 |
| order-service | 8002 | 주문 관리 | 2 | 10 |
| inventory-service | 8008 | 재고 관리 | 2 | 8 |
| board-service | 8006 | 게시판 | 2 | 6 |
| admin-service | 8007 | 관리자 | 2 | 6 |

## MySQL 데이터베이스

| MySQL | DB명 | 사용 서비스 |
|-------|------|-------------|
| mysql-member | member_db | member, admin, board |
| mysql-product | product_db | product |
| mysql-order | order_db | order |
| mysql-inventory | inventory_db | inventory |

## 자동 스케일링 (HPA)

HPA가 자동으로 Pod 수를 조절합니다:
- **CPU 70%** 초과 시 스케일 업
- **Memory 80%** 초과 시 스케일 업
- 5분간 안정화 후 스케일 다운

```bash
# HPA 상태 확인
kubectl get hpa -n tori-app

# 실시간 모니터링
kubectl get hpa -n tori-app -w
```

## 수동 스케일링

```bash
# 서비스 스케일 업/다운
kubectl scale deployment frontend-service --replicas=5 -n tori-app
kubectl scale deployment order-service --replicas=3 -n tori-app
```

## 로그 확인

```bash
# 특정 서비스 로그
kubectl logs -f deployment/frontend-service -n tori-app
kubectl logs -f deployment/order-service -n tori-app

# 특정 Pod 로그
kubectl logs -f <pod-name> -n tori-app
```

## 리소스 삭제

```bash
# 애플리케이션만 삭제
./k8s/scripts/delete.sh

# EKS 클러스터 전체 삭제 (비용 절약)
eksctl delete cluster --name tori-app-cluster --region ap-northeast-2
```

## 트러블슈팅

```bash
# Pod 상태 상세 확인
kubectl describe pod <pod-name> -n tori-app

# 서비스 엔드포인트 확인
kubectl get endpoints -n tori-app

# ConfigMap 확인
kubectl get configmap app-config -n tori-app -o yaml

# 이벤트 확인
kubectl get events -n tori-app --sort-by='.lastTimestamp'

# ALB Controller 로그
kubectl logs -n kube-system deployment/aws-load-balancer-controller
```

## 예상 비용 (서울 리전 기준)

| 리소스 | 사양 | 월 예상 비용 |
|--------|------|--------------|
| EKS 클러스터 | - | ~$73 |
| EC2 노드 (t3.medium x 3) | 2vCPU, 4GB | ~$125 |
| ALB | - | ~$20+ |
| EBS (PVC) | 각 20GB x 4 | ~$10 |
| **합계** | | **~$230/월** |

> 테스트 후 `eksctl delete cluster`로 삭제하면 비용이 발생하지 않습니다.
