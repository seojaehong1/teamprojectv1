#!/bin/bash

# ===========================================
# TORI COFFEE - 전체 이미지 빌드 & 푸시 스크립트
# 사용법: ./build-and-push.sh
# ===========================================

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# AWS 설정
AWS_ACCOUNT_ID="490866675691"
AWS_REGION="ap-northeast-2"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# 서비스 목록
SERVICES=(
  "eureka-server"
  "gateway-service"
  "frontend-service"
  "member-service"
  "product-service"
  "order-service"
  "inventory-service"
  "board-service"
  "admin-service"
)

# 프로젝트 루트 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"
cd "$PROJECT_DIR"

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}🚀 TORI COFFEE 전체 빌드 & 푸시${NC}"
echo -e "${GREEN}==========================================${NC}"
echo "프로젝트 경로: $PROJECT_DIR"

# ECR 로그인
echo -e "\n${YELLOW}📦 [1/3] ECR 로그인...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

# Gradle 빌드
echo -e "\n${YELLOW}🔨 [2/3] Gradle 빌드...${NC}"
./gradlew clean build -x test

# Docker 이미지 빌드 및 푸시
echo -e "\n${YELLOW}🐳 [3/3] Docker 이미지 빌드 & 푸시...${NC}"

TOTAL=${#SERVICES[@]}
CURRENT=0

for SERVICE in "${SERVICES[@]}"; do
  CURRENT=$((CURRENT + 1))
  echo -e "\n${YELLOW}[${CURRENT}/${TOTAL}] ${SERVICE} 처리 중...${NC}"

  # ECR 리포지토리 생성 (없으면)
  aws ecr describe-repositories --repository-names "${SERVICE}" --region ${AWS_REGION} 2>/dev/null || \
    aws ecr create-repository --repository-name "${SERVICE}" --region ${AWS_REGION}

  # Docker 이미지 빌드
  echo "  🔨 빌드 중..."
  docker build --no-cache -t ${SERVICE} -f ${SERVICE}/Dockerfile .

  # 태그 및 푸시
  echo "  📤 푸시 중..."
  docker tag ${SERVICE}:latest ${ECR_REGISTRY}/${SERVICE}:latest
  docker push ${ECR_REGISTRY}/${SERVICE}:latest

  echo -e "  ${GREEN}✅ ${SERVICE} 완료!${NC}"
done

echo -e "\n${GREEN}==========================================${NC}"
echo -e "${GREEN}✅ 모든 이미지 빌드 & 푸시 완료!${NC}"
echo -e "${GREEN}==========================================${NC}"

echo -e "\n${YELLOW}💡 K8s에 배포하려면:${NC}"
echo "cd k8s/scripts && ./deploy.sh"
echo ""
echo -e "${YELLOW}💡 특정 서비스만 재배포하려면:${NC}"
echo "kubectl rollout restart deployment/<서비스명> -n tori-app"