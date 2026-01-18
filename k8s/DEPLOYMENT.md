
# TORI COFFEE - AWS EKS 배포 가이드

> 이 문서는 팀원들이 처음부터 끝까지 따라할 수 있도록 작성되었습니다.
> **마지막 성공 배포일: 2026-01-18**

---

## 목차
1. [사전 준비 (처음 1회만)](#1-사전-준비-처음-1회만)
2. [클러스터 생성](#2-클러스터-생성)
3. [클러스터 접속 설정](#3-클러스터-접속-설정)
4. [애플리케이션 배포](#4-애플리케이션-배포)
5. [데이터 초기화](#5-데이터-초기화)
6. [상태 확인 명령어 모음](#6-상태-확인-명령어-모음)
7. [비용 관리 (중요!)](#7-비용-관리-중요)
8. [클러스터 삭제](#8-클러스터-삭제)
9. [이미지 빌드 & 푸시](#9-이미지-빌드--푸시)
10. [트러블슈팅](#10-트러블슈팅)
11. [자주 쓰는 명령어 요약](#11-자주-쓰는-명령어-요약)

---

## 1. 사전 준비 (처음 1회만)

### 1.1 필수 프로그램 설치

| 프로그램 | 다운로드 링크 | 설명 |
|----------|--------------|------|
| AWS CLI v2 | https://aws.amazon.com/cli/ | AWS 서비스 접근용 |
| kubectl | https://kubernetes.io/docs/tasks/tools/ | K8s 클러스터 관리용 |
| eksctl | https://eksctl.io/ | EKS 클러스터 생성/삭제용 |
| Docker Desktop | https://www.docker.com/products/docker-desktop/ | 이미지 빌드용 |
| Helm | https://helm.sh/docs/intro/install/ | ALB Controller 설치용 |

#### PowerShell 명령어로 설치하기 (권장)

**1단계: Chocolatey 설치** (PowerShell 관리자 권한 실행)
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

> ※ 설치 후 PowerShell 재시작 필요!

**2단계: 필수 프로그램 한 번에 설치**
```powershell
choco install awscli kubectl eksctl docker-desktop kubernetes-helm -y
```

> ※ 설치 후 PowerShell 재시작 필요!

**3단계: 설치 확인**
```powershell
aws --version
kubectl version --client
eksctl version
docker --version
helm version
```

### 1.2 AWS CLI 설정

```powershell
aws configure
```

입력값:
```
AWS Access Key ID: (팀장에게 문의)
AWS Secret Access Key: (팀장에게 문의)
Default region name: ap-northeast-2
Default output format: json
```

### 1.3 설정 확인

```powershell
# AWS 연결 확인
aws sts get-caller-identity

# 정상이면 아래와 같이 출력됨
# {
#     "UserId": "...",
#     "Account": "490866675691",
#     "Arn": "arn:aws:iam::490866675691:user/..."
# }
```

---

## 2. 클러스터 생성

> **주의**: 클러스터가 이미 있으면 이 단계를 건너뛰세요!

### 2.1 클러스터 존재 여부 확인

```powershell
aws eks list-clusters --region ap-northeast-2
```

출력 예시:
```json
{
    "clusters": ["teamproject-cluster"]  // 이미 있으면 생성 불필요
}
```

```json
{
    "clusters": []  // 비어있으면 생성 필요
}
```

### 2.2 클러스터 생성 (약 15-20분 소요)

```powershell
cd k8s
eksctl create cluster -f cluster-config.yaml
```

또는 직접 명령어로:
```powershell
eksctl create cluster --name teamproject-cluster --region ap-northeast-2 --version 1.30 --nodegroup-name tori-nodes --node-type t3.medium --nodes 5 --nodes-min 3 --nodes-max 7
```

> **참고**: Kubernetes 버전은 **1.30** 사용 (1.28은 더 이상 지원 안됨)

### 2.3 클러스터 생성 확인

```powershell
# 클러스터 상태 확인
aws eks describe-cluster --name teamproject-cluster --region ap-northeast-2 --query "cluster.status"

# "ACTIVE" 가 출력되면 성공!
```

### 2.4 EBS CSI Driver 설치 (필수! - MySQL 볼륨용)

> **중요**: EKS 1.23 이상에서는 EBS 볼륨 사용을 위해 CSI Driver가 **반드시** 필요합니다.
> 이 단계를 건너뛰면 MySQL 파드가 Pending 상태에서 멈춥니다!

```powershell
eksctl create addon --name aws-ebs-csi-driver --cluster teamproject-cluster --region ap-northeast-2 --force
```

설치 확인:
```powershell
kubectl get pods -n kube-system -l app.kubernetes.io/name=aws-ebs-csi-driver
```

정상 출력 예시:
```
NAME                                  READY   STATUS    RESTARTS   AGE
ebs-csi-controller-xxxxxxxxx-xxxxx    6/6     Running   0          2m
ebs-csi-node-xxxxx                    3/3     Running   0          2m
```

### 2.5 ALB Ingress Controller 설치

```powershell
# 1. IAM OIDC 프로바이더 생성
eksctl utils associate-iam-oidc-provider --cluster teamproject-cluster --region ap-northeast-2 --approve

# 2. IAM 정책 생성 (이미 있으면 스킵 - 에러나도 괜찮음)
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.7/docs/install/iam_policy.json

aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file://iam_policy.json

# 3. Service Account 생성
eksctl create iamserviceaccount --cluster=teamproject-cluster --namespace=kube-system --name=aws-load-balancer-controller --attach-policy-arn=arn:aws:iam::490866675691:policy/AWSLoadBalancerControllerIAMPolicy --override-existing-serviceaccounts --region ap-northeast-2 --approve

# 4. Helm으로 ALB Controller 설치
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# 4-1. VPC ID 확인
$VPC_ID = aws eks describe-cluster --name teamproject-cluster --query "cluster.resourcesVpcConfig.vpcId" --output text --region ap-northeast-2
echo "VPC ID: $VPC_ID"

# 4-2. Helm 설치 실행
helm install aws-load-balancer-controller eks/aws-load-balancer-controller -n kube-system --set clusterName=teamproject-cluster --set serviceAccount.create=false --set serviceAccount.name=aws-load-balancer-controller --set region=ap-northeast-2 --set vpcId=$VPC_ID

# 5. 설치 확인
kubectl get deployment -n kube-system aws-load-balancer-controller
```

---

## 3. 클러스터 접속 설정

### 3.1 kubeconfig 설정

```powershell
aws eks update-kubeconfig --region ap-northeast-2 --name teamproject-cluster
```

### 3.2 접속 확인

```powershell
# 노드 확인
kubectl get nodes

# 정상 출력 예시:
# NAME                                               STATUS   ROLES    AGE   VERSION
# ip-192-168-xx-xx.ap-northeast-2.compute.internal   Ready    <none>   5m    v1.30.x
```

### 3.3 ECR 로그인 (Docker 이미지 푸시/풀 시 필요)

```powershell
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com
```

---

## 4. 애플리케이션 배포

### 4.1 수동 배포 (권장 - 순서대로 실행)

```powershell
cd k8s

# 1. 네임스페이스 생성
kubectl apply -f base/namespace.yaml

# 2. ConfigMap & Secrets
kubectl apply -f base/configmap.yaml
kubectl apply -f base/secrets.yaml

# 3. MySQL 데이터베이스 (볼륨 생성에 시간이 걸림)
kubectl apply -f mysql/

# MySQL 파드가 Running이 될 때까지 대기 (1-3분)
kubectl get pods -n tori-app -l app=mysql-product -w
# Running 확인 후 Ctrl+C로 종료

# 4. RabbitMQ
kubectl apply -f rabbitmq/

# 5. 애플리케이션 서비스들
kubectl apply -f services/

# 6. Ingress (ALB 생성)
kubectl apply -f ingress.yaml

# 7. HPA (Auto Scaling) - 선택사항
kubectl apply -f hpa/
```

### 4.2 배포 확인

```powershell
# 모든 파드 상태 확인
kubectl get pods -n tori-app

# 모든 파드가 Running 상태가 될 때까지 대기 (2-5분)
kubectl get pods -n tori-app -w
```

**정상 상태 예시:**
```
NAME                                 READY   STATUS    RESTARTS   AGE
admin-service-xxx                    1/1     Running   0          2m
board-service-xxx                    1/1     Running   0          2m
eureka-server-xxx                    1/1     Running   0          2m
frontend-service-xxx                 1/1     Running   0          2m
gateway-service-xxx                  1/1     Running   0          2m
inventory-service-xxx                1/1     Running   0          2m
member-service-xxx                   1/1     Running   0          2m
mysql-inventory-0                    1/1     Running   0          3m
mysql-member-0                       1/1     Running   0          3m
mysql-order-0                        1/1     Running   0          3m
mysql-product-0                      1/1     Running   0          3m
order-service-xxx                    1/1     Running   0          2m
product-service-xxx                  1/1     Running   0          2m
rabbitmq-0                           1/1     Running   0          3m
```

### 4.3 접속 URL 확인

```powershell
# ALB URL 확인 (Ingress 생성 후 2-3분 대기)
kubectl get ingress -n tori-app

# ADDRESS 열에 있는 URL로 접속
# 예: http://k8s-toriapp-xxxxx.ap-northeast-2.elb.amazonaws.com
```

---

## 5. 데이터 초기화

> **참고**: data.sql은 Spring Boot 애플리케이션 시작 시 **자동 실행**됩니다.
> `SPRING_SQL_INIT_MODE=always` 환경변수가 설정되어 있으면 자동으로 데이터가 삽입됩니다.

### 5.1 자동 데이터 초기화 방식 (권장)

product-service와 inventory-service는 애플리케이션 시작 시 `data.sql`이 자동 실행됩니다.
- `data.sql`에는 `INSERT IGNORE`를 사용하여 중복 실행해도 에러가 발생하지 않습니다.
- k8s 배포 시 `SPRING_SQL_INIT_MODE=always`가 설정되어 있어야 합니다.

### 5.2 데이터 초기화 확인

```powershell
# 메뉴 데이터 확인
kubectl exec mysql-product-0 -n tori-app -- mysql -u root -prootpass product_db -e "SELECT COUNT(*) FROM menu;"

# 25개 이상이면 정상
```

### 5.3 데이터가 없는 경우 (자동 실행 실패 시)

```powershell
# 1. 환경변수 확인
kubectl exec deployment/product-service -n tori-app -- env | grep SQL

# SPRING_SQL_INIT_MODE=always 가 있어야 함

# 2. 만약 never로 되어 있다면 product-service.yaml 수정 후 재배포
kubectl apply -f k8s/services/product-service.yaml
kubectl rollout restart deployment/product-service -n tori-app
```

### 5.4 수동 데이터 삽입 (긴급 상황용)

자동 실행이 안 될 경우에만 사용:

```powershell
# MySQL 비밀번호 확인 (secrets.yaml에서)
kubectl get secret db-secrets -n tori-app -o jsonpath='{.data.MYSQL_PRODUCT_PASSWORD}' | base64 -d

# data.sql 수동 실행 (주의: 인코딩 문제 발생 가능)
kubectl exec -i mysql-product-0 -n tori-app -- mysql -u root -prootpass --default-character-set=utf8mb4 product_db < product-service/src/main/resources/data.sql
```

### 5.5 데이터 확인

브라우저에서 메뉴 페이지 (`/menu/drink`) 새로고침하여 메뉴가 표시되는지 확인합니다.

---

## 6. 상태 확인 명령어 모음

### 6.1 클러스터 상태

```powershell
# 클러스터 목록
aws eks list-clusters --region ap-northeast-2

# 클러스터 상태 (ACTIVE/CREATING/DELETING)
aws eks describe-cluster --name teamproject-cluster --region ap-northeast-2 --query "cluster.status"

# 노드그룹 목록
aws eks list-nodegroups --cluster-name teamproject-cluster --region ap-northeast-2

# 노드 상태
kubectl get nodes
```

### 6.2 파드 상태

```powershell
# 모든 파드 목록
kubectl get pods -n tori-app

# 파드 상태 실시간 모니터링 (Ctrl+C로 종료)
kubectl get pods -n tori-app -w

# 특정 파드 상세 정보
kubectl describe pod <파드이름> -n tori-app

# 파드 로그 보기
kubectl logs <파드이름> -n tori-app

# 파드 로그 실시간 보기
kubectl logs -f <파드이름> -n tori-app

# 이전 파드 로그 (재시작된 경우)
kubectl logs <파드이름> -n tori-app --previous
```

### 6.3 서비스 & Ingress 상태

```powershell
# 서비스 목록
kubectl get svc -n tori-app

# Ingress (ALB) 상태
kubectl get ingress -n tori-app

# Ingress 상세 정보 (ALB 프로비저닝 상태 확인)
kubectl describe ingress tori-app-ingress -n tori-app
```

### 6.4 리소스 사용량

```powershell
# 파드별 CPU/메모리 사용량
kubectl top pods -n tori-app

# 노드별 CPU/메모리 사용량
kubectl top nodes

# HPA 상태 (Auto Scaling)
kubectl get hpa -n tori-app
```

### 6.5 문제 파드 찾기

```powershell
# Running이 아닌 파드 찾기
kubectl get pods -n tori-app --field-selector=status.phase!=Running

# 최근 이벤트 (에러 확인용)
kubectl get events -n tori-app --sort-by=".lastTimestamp"
```

---

## 7. 비용 관리 (중요!)

### 7.1 예상 비용

| 리소스 | 월 비용 (USD) | 설명 |
|--------|--------------|------|
| EKS Control Plane | ~$73 | 클러스터 존재하는 동안 계속 발생 |
| EC2 노드 (t3.medium x 5) | ~$170 | 노드 실행 중일 때만 |
| NAT Gateway | ~$32 | 클러스터 존재하는 동안 발생 |
| ALB | ~$20 | Ingress 존재 시 |
| EBS 볼륨 (MySQL) | ~$4 | 40GB x $0.1/GB |
| **총합 (실행 중)** | **~$300/월** | |
| **총합 (노드 0개)** | **~$105/월** | Control Plane + NAT만 |

### 7.2 비용 절약 방법

#### 방법 1: 노드만 축소 (빠른 재시작 가능)

```powershell
# 노드 0개로 축소 (EC2 비용 절약)
eksctl scale nodegroup --cluster=teamproject-cluster --name=tori-nodes --nodes=0 --nodes-min=0 --region=ap-northeast-2

# 다시 시작할 때
eksctl scale nodegroup --cluster=teamproject-cluster --name=tori-nodes --nodes=5 --nodes-min=3 --region=ap-northeast-2
```

**장점**: 5분 내 재시작 가능
**단점**: Control Plane + NAT 비용은 계속 발생 (~$105/월)

#### 방법 2: 클러스터 완전 삭제 (비용 0원)

```powershell
eksctl delete cluster --name teamproject-cluster --region ap-northeast-2
```

**장점**: 비용 0원
**단점**: 재생성 시 15-20분 + ALB Controller/EBS CSI Driver 재설치 필요

### 7.3 권장 운영 방식

| 상황 | 권장 방법 |
|------|----------|
| 오늘 안에 다시 사용 | 노드 축소 |
| 며칠~1주일 쉬는 경우 | 클러스터 삭제 |
| 발표/시연 전날 | 미리 클러스터 생성해두기 |

---

## 8. 클러스터 삭제

### 8.1 K8s 리소스만 삭제 (클러스터 유지)

```powershell
kubectl delete namespace tori-app
```

### 8.2 클러스터 완전 삭제

```powershell
eksctl delete cluster --name teamproject-cluster --region ap-northeast-2
```

### 8.3 삭제 확인

```powershell
# 클러스터가 삭제되었는지 확인
aws eks list-clusters --region ap-northeast-2

# 빈 배열이면 삭제 완료
# { "clusters": [] }
```

---

## 9. 이미지 빌드 & 푸시

### 9.1 전체 서비스 빌드 & 푸시 (수동)

```powershell
# 프로젝트 루트에서 실행
cd C:\dev_4\teamprojectv1

# 1. ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com

# 2. 각 서비스별로 빌드 & 푸시 (예: frontend-service)
./gradlew :frontend-service:clean :frontend-service:build -x test
docker build --no-cache -t frontend-service -f frontend-service/Dockerfile .
docker tag frontend-service:latest 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/tori-app/frontend-service:latest
docker push 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com/tori-app/frontend-service:latest
```

### 9.2 서비스 재배포 (코드 수정 후)

```powershell
# 1. 빌드 & 푸시 (위 9.1 참고)

# 2. K8s 재배포
kubectl rollout restart deployment/frontend-service -n tori-app

# 3. 배포 상태 확인
kubectl rollout status deployment/frontend-service -n tori-app
```

### 9.3 모든 서비스 목록

| 서비스명 | 빌드 명령어 |
|----------|-------------|
| eureka-server | `./gradlew :eureka-server:build -x test` |
| gateway-service | `./gradlew :gateway-service:build -x test` |
| frontend-service | `./gradlew :frontend-service:build -x test` |
| member-service | `./gradlew :member-service:build -x test` |
| product-service | `./gradlew :product-service:build -x test` |
| order-service | `./gradlew :order-service:build -x test` |
| board-service | `./gradlew :board-service:build -x test` |
| admin-service | `./gradlew :admin-service:build -x test` |
| inventory-service | `./gradlew :inventory-service:build -x test` |

---

## 10. 트러블슈팅

### 10.1 MySQL 파드가 Pending 상태

**증상:**
```
mysql-product-0   0/1     Pending   0          5m
```

**원인**: EBS CSI Driver가 설치되지 않음

**해결:**
```powershell
eksctl create addon --name aws-ebs-csi-driver --cluster teamproject-cluster --region ap-northeast-2 --force
```

### 10.2 서비스 파드가 CrashLoopBackOff 상태

**증상:**
```
frontend-service-xxx   0/1     CrashLoopBackOff   5          10m
```

**원인 확인:**
```powershell
kubectl logs <파드이름> -n tori-app
kubectl logs <파드이름> -n tori-app --previous
```

**일반적인 원인들:**
- MySQL 연결 실패 → MySQL 파드가 Running인지 확인
- Eureka 연결 실패 → eureka-server가 Running인지 확인
- 메모리 부족 → 노드 추가 필요

### 10.3 노드 리소스 부족 (Insufficient cpu/memory)

**증상:**
```
Events:
  Warning  FailedScheduling  0/3 nodes are available: 3 Insufficient cpu
```

**해결:**
```powershell
# 노드 추가
eksctl scale nodegroup --cluster=teamproject-cluster --name=tori-nodes --nodes=5 --region=ap-northeast-2
```

### 10.4 파드가 ImagePullBackOff 상태

**원인**: ECR 이미지를 가져올 수 없음

**해결:**
```powershell
# ECR 로그인 확인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com

# 이미지 존재 확인
aws ecr describe-images --repository-name tori-app/frontend-service --region ap-northeast-2
```

### 10.5 메뉴 페이지에 데이터가 안 보임

**원인**: MySQL에 초기 데이터가 없음

**해결**: [5. 데이터 초기화](#5-데이터-초기화) 섹션 참고

### 10.6 한글이 깨져서 보임 (???)

**원인**: MySQL 문자 인코딩 문제

**확인:**
```powershell
kubectl exec -it mysql-product-0 -n tori-app -- mysql -u root -prootpass -e "SHOW VARIABLES LIKE 'character%';"
```

**해결**: MySQL YAML에 이미 UTF-8 설정이 포함되어 있습니다. 문제가 지속되면:
```powershell
# MySQL 재시작
kubectl delete pod mysql-product-0 -n tori-app
# 파드가 자동으로 재생성됨

# 데이터 다시 입력
Get-Content product-service/src/main/resources/data.sql -Raw | kubectl exec -i mysql-product-0 -n tori-app -- mysql -u root -prootpass product_db
```

### 10.7 ALB (Ingress)가 생성 안됨

**원인**: ALB Controller가 설치되지 않음

**확인:**
```powershell
kubectl get deployment -n kube-system aws-load-balancer-controller
```

**해결**: [2.5 ALB Ingress Controller 설치](#25-alb-ingress-controller-설치) 참고

### 10.8 Volume node affinity conflict

**증상:**
```
0/5 nodes are available: 5 node(s) had volume node affinity conflict
```

**원인**: PVC가 특정 가용영역에 바인딩되어 있는데 노드가 다른 가용영역에 있음

**해결:**
```powershell
# 문제가 되는 StatefulSet과 PVC 삭제 후 재생성
kubectl delete statefulset mysql-product -n tori-app
kubectl delete pvc mysql-product-pvc -n tori-app
kubectl apply -f mysql/mysql-product.yaml
```

---

## 11. 자주 쓰는 명령어 요약

### 클러스터 관리

| 작업 | 명령어 |
|------|--------|
| 클러스터 목록 확인 | `aws eks list-clusters --region ap-northeast-2` |
| 클러스터 생성 | `eksctl create cluster -f cluster-config.yaml` |
| 클러스터 삭제 | `eksctl delete cluster --name teamproject-cluster --region ap-northeast-2` |
| kubeconfig 설정 | `aws eks update-kubeconfig --region ap-northeast-2 --name teamproject-cluster` |
| EBS CSI Driver 설치 | `eksctl create addon --name aws-ebs-csi-driver --cluster teamproject-cluster --region ap-northeast-2 --force` |

### 노드 관리

| 작업 | 명령어 |
|------|--------|
| 노드 확인 | `kubectl get nodes` |
| 노드 축소 (0개) | `eksctl scale nodegroup --cluster=teamproject-cluster --name=tori-nodes --nodes=0 --nodes-min=0 --region=ap-northeast-2` |
| 노드 확장 (5개) | `eksctl scale nodegroup --cluster=teamproject-cluster --name=tori-nodes --nodes=5 --nodes-min=3 --region=ap-northeast-2` |

### 파드 관리

| 작업 | 명령어 |
|------|--------|
| 파드 목록 | `kubectl get pods -n tori-app` |
| 파드 로그 | `kubectl logs <파드명> -n tori-app` |
| 파드 재시작 | `kubectl rollout restart deployment/<서비스명> -n tori-app` |
| 파드 내부 접속 | `kubectl exec -it <파드명> -n tori-app -- sh` |
| MySQL 접속 | `kubectl exec -it mysql-product-0 -n tori-app -- mysql -u root -prootpass product_db` |

### 배포 관리

| 작업 | 명령어 |
|------|--------|
| 전체 배포 | 섹션 4 참고 |
| 개별 서비스 재배포 | `kubectl rollout restart deployment/<서비스명> -n tori-app` |
| 배포 상태 확인 | `kubectl rollout status deployment/<서비스명> -n tori-app` |
| 데이터 초기화 | 섹션 5 참고 |

---

## 서비스 포트 정보

| 서비스 | 포트 | 설명 |
|--------|------|------|
| eureka-server | 8761 | 서비스 디스커버리 |
| gateway-service | 8000 | API Gateway |
| frontend-service | 8005 | 웹 UI (메인 진입점) |
| member-service | 8004 | 회원/인증 |
| product-service | 8001 | 상품 관리 |
| order-service | 8002 | 주문 관리 |
| board-service | 8006 | 게시판/공지 |
| admin-service | 8007 | 관리자 |
| inventory-service | 8008 | 재고 관리 |

---

## AWS 리소스 정보

| 항목 | 값 |
|------|-----|
| AWS Account ID | 490866675691 |
| Region | ap-northeast-2 (서울) |
| Cluster Name | teamproject-cluster |
| ECR Registry | 490866675691.dkr.ecr.ap-northeast-2.amazonaws.com |
| Namespace | tori-app |
| Kubernetes Version | 1.30 |

---

## 배포 체크리스트

클러스터 재생성 후 아래 순서대로 진행:

- [ ] 1. 클러스터 생성 (`eksctl create cluster -f cluster-config.yaml`)
- [ ] 2. EBS CSI Driver 설치 (`eksctl create addon --name aws-ebs-csi-driver ...`)
- [ ] 3. ALB Controller 설치 (helm install ...)
- [ ] 4. kubeconfig 설정 (`aws eks update-kubeconfig ...`)
- [ ] 5. ECR 로그인 (`aws ecr get-login-password ...`)
- [ ] 6. 네임스페이스 생성 (`kubectl apply -f base/namespace.yaml`)
- [ ] 7. ConfigMap & Secrets 생성
- [ ] 8. MySQL 배포 및 Running 확인
- [ ] 9. RabbitMQ 배포
- [ ] 10. 서비스 배포
- [ ] 11. Ingress 배포
- [ ] 12. 데이터 초기화 (data.sql 실행)
- [ ] 13. 웹사이트 접속 테스트

---

## 문의

- 팀장: 서재홍
- 이메일: wtme3@naver.com
