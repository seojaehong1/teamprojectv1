#!/bin/bash

# ===========================================
# TORI COFFEE - EKS 전체 배포 스크립트
# 사용법: ./deploy.sh
# ===========================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# AWS 설정
AWS_ACCOUNT_ID="490866675691"
AWS_REGION="ap-northeast-2"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
NAMESPACE="tori-app"

# 스크립트 디렉토리 기준으로 k8s 폴더 경로 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}🚀 TORI COFFEE EKS 배포 시작${NC}"
echo -e "${GREEN}==========================================${NC}"

# ECR 로그인
echo -e "\n${YELLOW}📦 [1/8] ECR 로그인...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY} 2>/dev/null || true

cd "$K8S_DIR"

# Namespace 생성
echo -e "\n${YELLOW}📁 [2/8] Namespace 생성...${NC}"
kubectl apply -f namespace.yaml

# ConfigMap, Secret 생성
echo -e "\n${YELLOW}🔧 [3/8] ConfigMap & Secrets 생성...${NC}"
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

# MySQL 배포
echo -e "\n${YELLOW}🗄️ [4/8] MySQL 배포...${NC}"
kubectl apply -f mysql/
echo "⏳ MySQL 준비 대기 중 (최대 5분)..."
kubectl wait --for=condition=ready pod -l app=mysql-member -n ${NAMESPACE} --timeout=300s 2>/dev/null || true
kubectl wait --for=condition=ready pod -l app=mysql-product -n ${NAMESPACE} --timeout=300s 2>/dev/null || true
kubectl wait --for=condition=ready pod -l app=mysql-order -n ${NAMESPACE} --timeout=300s 2>/dev/null || true
kubectl wait --for=condition=ready pod -l app=mysql-inventory -n ${NAMESPACE} --timeout=300s 2>/dev/null || true

# RabbitMQ 배포
echo -e "\n${YELLOW}🐰 [5/8] RabbitMQ 배포...${NC}"
kubectl apply -f rabbitmq/
echo "⏳ RabbitMQ 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=rabbitmq -n ${NAMESPACE} --timeout=180s 2>/dev/null || true

# Eureka Server 배포
echo -e "\n${YELLOW}🔍 [6/8] Eureka Server 배포...${NC}"
kubectl apply -f services/eureka-server.yaml
echo "⏳ Eureka 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=eureka-server -n ${NAMESPACE} --timeout=180s 2>/dev/null || true

# 나머지 서비스 배포
echo -e "\n${YELLOW}🚀 [7/8] 애플리케이션 서비스 배포...${NC}"
kubectl apply -f services/gateway-service.yaml
kubectl apply -f services/member-service.yaml
kubectl apply -f services/product-service.yaml
kubectl apply -f services/order-service.yaml
kubectl apply -f services/board-service.yaml
kubectl apply -f services/admin-service.yaml
kubectl apply -f services/inventory-service.yaml
kubectl apply -f services/frontend-service.yaml

# HPA 배포
echo "📈 HPA (Auto Scaling) 배포..."
kubectl apply -f hpa/ 2>/dev/null || true

# Ingress 배포
echo -e "\n${YELLOW}🌐 [8/8] Ingress (ALB) 배포...${NC}"
kubectl apply -f ingress.yaml

# 배포 완료 대기
echo -e "\n⏳ 전체 서비스 시작 대기 (2분)..."
sleep 120

# 상태 확인
echo -e "\n${GREEN}==========================================${NC}"
echo -e "${GREEN}📊 배포 상태 확인${NC}"
echo -e "${GREEN}==========================================${NC}"

echo -e "\n${YELLOW}🔹 Pods:${NC}"
kubectl get pods -n ${NAMESPACE}

echo -e "\n${YELLOW}🔹 Services:${NC}"
kubectl get svc -n ${NAMESPACE}

echo -e "\n${YELLOW}🔹 Ingress:${NC}"
kubectl get ingress -n ${NAMESPACE}

echo -e "\n${YELLOW}🔹 HPA:${NC}"
kubectl get hpa -n ${NAMESPACE} 2>/dev/null || echo "HPA not configured"

# ALB URL 출력
ALB_URL=$(kubectl get ingress -n ${NAMESPACE} -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
if [ -n "$ALB_URL" ]; then
    echo -e "\n${GREEN}==========================================${NC}"
    echo -e "${GREEN}✅ 배포 완료!${NC}"
    echo -e "${GREEN}==========================================${NC}"
    echo -e "\n🌐 접속 URL: ${YELLOW}http://${ALB_URL}${NC}\n"
else
    echo -e "\n${YELLOW}⚠️ ALB URL을 아직 가져올 수 없습니다.${NC}"
    echo "잠시 후 다음 명령어로 확인하세요:"
    echo "kubectl get ingress -n tori-app"
fi