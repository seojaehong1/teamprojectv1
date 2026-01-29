#!/bin/bash

# ===========================================
# TORI COFFEE - 빠른 재배포 스크립트
# 사용법: ./quick-redeploy.sh [서비스명]
# 예시: ./quick-redeploy.sh frontend-service
# ===========================================

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID="490866675691"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
NAMESPACE="tori-app"

SERVICE_NAME=$1

if [ -z "$SERVICE_NAME" ]; then
    echo -e "${YELLOW}사용법: ./quick-redeploy.sh [서비스명]${NC}"
    echo ""
    echo "사용 가능한 서비스:"
    echo "  - frontend-service"
    echo "  - gateway-service"
    echo "  - member-service"
    echo "  - product-service"
    echo "  - order-service"
    echo "  - board-service"
    echo "  - admin-service"
    echo "  - inventory-service"
    echo "  - eureka-server"
    echo ""
    echo "예시: ./quick-redeploy.sh frontend-service"
    exit 1
fi

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}🚀 ${SERVICE_NAME} 빠른 재배포${NC}"
echo -e "${GREEN}==========================================${NC}"

# 프로젝트 루트 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"
cd "$PROJECT_DIR"

echo -e "\n${YELLOW}📦 [1/5] ECR 로그인...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY} 2>/dev/null

echo -e "\n${YELLOW}🔨 [2/5] Gradle 빌드...${NC}"
./gradlew :${SERVICE_NAME}:clean :${SERVICE_NAME}:build -x test

echo -e "\n${YELLOW}🐳 [3/5] Docker 이미지 빌드...${NC}"
docker build --no-cache -t ${SERVICE_NAME} -f ${SERVICE_NAME}/Dockerfile .

echo -e "\n${YELLOW}📤 [4/5] ECR 푸시...${NC}"
docker tag ${SERVICE_NAME}:latest ${ECR_REGISTRY}/tori-app/${SERVICE_NAME}:latest
docker push ${ECR_REGISTRY}/tori-app/${SERVICE_NAME}:latest

echo -e "\n${YELLOW}🔄 [5/5] K8s 재배포...${NC}"
kubectl rollout restart deployment/${SERVICE_NAME} -n ${NAMESPACE}

echo -e "\n${YELLOW}⏳ 배포 상태 확인 중...${NC}"
kubectl rollout status deployment/${SERVICE_NAME} -n ${NAMESPACE} --timeout=180s

echo -e "\n${GREEN}==========================================${NC}"
echo -e "${GREEN}✅ ${SERVICE_NAME} 재배포 완료!${NC}"
echo -e "${GREEN}==========================================${NC}"

# 파드 상태 확인
echo -e "\n${YELLOW}🔹 ${SERVICE_NAME} Pods:${NC}"
kubectl get pods -n ${NAMESPACE} -l app=${SERVICE_NAME}
