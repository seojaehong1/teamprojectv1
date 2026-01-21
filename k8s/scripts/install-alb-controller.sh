#!/bin/bash

# ===========================================
# AWS Load Balancer Controller 설치 스크립트
# ===========================================

# .env 파일 로드
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/.env" ]; then
  source "${SCRIPT_DIR}/.env"
fi

# 설정
CLUSTER_NAME="${EKS_CLUSTER_NAME:-tori-app-cluster}"
AWS_REGION="${AWS_REGION:-ap-northeast-2}"

if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo "Error: AWS_ACCOUNT_ID가 설정되지 않았습니다."
  exit 1
fi

echo "==========================================="
echo "AWS Load Balancer Controller 설치"
echo "==========================================="

# 1. IAM Policy 생성
echo "1. IAM Policy 생성..."
curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.1/docs/install/iam_policy.json

aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json 2>/dev/null || echo "Policy already exists"

rm iam_policy.json

# 2. IAM Service Account 생성
echo "2. IAM Service Account 생성..."
eksctl create iamserviceaccount \
  --cluster=${CLUSTER_NAME} \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --attach-policy-arn=arn:aws:iam::${AWS_ACCOUNT_ID}:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve \
  --region=${AWS_REGION}

# 3. Helm 설치 확인
if ! command -v helm &> /dev/null; then
  echo "Error: Helm이 설치되지 않았습니다."
  echo "설치: https://helm.sh/docs/intro/install/"
  exit 1
fi

# 4. Helm repo 추가
echo "3. Helm repo 추가..."
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# 5. AWS Load Balancer Controller 설치
echo "4. AWS Load Balancer Controller 설치..."
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=${CLUSTER_NAME} \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set region=${AWS_REGION} \
  --set vpcId=$(aws eks describe-cluster --name ${CLUSTER_NAME} --query "cluster.resourcesVpcConfig.vpcId" --output text)

# 6. 설치 확인
echo ""
echo "설치 확인..."
kubectl get deployment -n kube-system aws-load-balancer-controller

echo "==========================================="
echo "AWS Load Balancer Controller 설치 완료!"
echo "==========================================="
echo ""
echo "다음 단계: ./deploy.sh 로 애플리케이션 배포"