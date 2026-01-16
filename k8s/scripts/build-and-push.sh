#!/bin/bash

# .env 파일 로드 (있으면)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/.env" ]; then
  source "${SCRIPT_DIR}/.env"
fi

# AWS 설정 - 환경변수 또는 .env 파일에서 로드
if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo "Error: AWS_ACCOUNT_ID 환경변수가 설정되지 않았습니다."
  echo "export AWS_ACCOUNT_ID=your-account-id 또는 .env 파일을 생성하세요."
  exit 1
fi

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
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

echo "=========================================="
echo "ECR 로그인"
echo "=========================================="
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo "=========================================="
echo "Gradle 빌드"
echo "=========================================="
./gradlew clean build -x test

echo "=========================================="
echo "Docker 이미지 빌드 및 푸시"
echo "=========================================="

for SERVICE in "${SERVICES[@]}"; do
  echo "Processing ${SERVICE}..."

  # ECR 리포지토리 생성 (없으면)
  aws ecr describe-repositories --repository-names "tori-app/${SERVICE}" --region ${AWS_REGION} 2>/dev/null || \
    aws ecr create-repository --repository-name "tori-app/${SERVICE}" --region ${AWS_REGION}

  # Docker 이미지 빌드
  docker build -t ${SERVICE} -f ${SERVICE}/Dockerfile .

  # 태그 및 푸시
  docker tag ${SERVICE}:latest ${ECR_REGISTRY}/tori-app/${SERVICE}:latest
  docker push ${ECR_REGISTRY}/tori-app/${SERVICE}:latest

  echo "${SERVICE} 완료!"
done

echo "=========================================="
echo "모든 이미지 푸시 완료!"
echo "=========================================="