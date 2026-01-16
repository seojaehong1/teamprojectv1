#!/bin/bash

# ===========================================
# EKS 클러스터 생성 스크립트
# ===========================================

# .env 파일 로드
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/.env" ]; then
  source "${SCRIPT_DIR}/.env"
fi

# 설정
CLUSTER_NAME="${EKS_CLUSTER_NAME:-tori-app-cluster}"
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
NODE_TYPE="${EKS_NODE_TYPE:-t3.medium}"
NODE_COUNT="${EKS_NODE_COUNT:-3}"
NODE_MIN="${EKS_NODE_MIN:-2}"
NODE_MAX="${EKS_NODE_MAX:-5}"

echo "==========================================="
echo "EKS 클러스터 생성"
echo "==========================================="
echo "클러스터 이름: ${CLUSTER_NAME}"
echo "리전: ${AWS_REGION}"
echo "노드 타입: ${NODE_TYPE}"
echo "노드 수: ${NODE_COUNT} (min: ${NODE_MIN}, max: ${NODE_MAX})"
echo "==========================================="

# 사전 확인
echo "AWS 자격 증명 확인..."
aws sts get-caller-identity || {
  echo "Error: AWS 자격 증명이 설정되지 않았습니다."
  echo "aws configure 를 먼저 실행하세요."
  exit 1
}

# eksctl 설치 확인
if ! command -v eksctl &> /dev/null; then
  echo "Error: eksctl이 설치되지 않았습니다."
  echo "설치: https://eksctl.io/installation/"
  exit 1
fi

echo ""
echo "클러스터 생성을 시작합니다... (15-20분 소요)"
echo ""

# EKS 클러스터 생성
eksctl create cluster \
  --name ${CLUSTER_NAME} \
  --region ${AWS_REGION} \
  --nodegroup-name tori-nodes \
  --node-type ${NODE_TYPE} \
  --nodes ${NODE_COUNT} \
  --nodes-min ${NODE_MIN} \
  --nodes-max ${NODE_MAX} \
  --managed \
  --with-oidc \
  --ssh-access=false

if [ $? -eq 0 ]; then
  echo "==========================================="
  echo "EKS 클러스터 생성 완료!"
  echo "==========================================="

  # kubeconfig 업데이트
  echo "kubeconfig 업데이트..."
  aws eks update-kubeconfig --name ${CLUSTER_NAME} --region ${AWS_REGION}

  # 클러스터 확인
  echo ""
  echo "클러스터 노드 확인:"
  kubectl get nodes

  echo ""
  echo "다음 단계:"
  echo "1. AWS Load Balancer Controller 설치: ./install-alb-controller.sh"
  echo "2. 애플리케이션 배포: ./deploy.sh"
else
  echo "Error: 클러스터 생성 실패"
  exit 1
fi