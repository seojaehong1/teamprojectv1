#!/bin/bash

# ===========================================
# TORI COFFEE - EKS 리소스 삭제 스크립트
# 사용법: ./cleanup.sh
# ===========================================

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

NAMESPACE="tori-app"

echo -e "${RED}==========================================${NC}"
echo -e "${RED}🗑️ TORI COFFEE EKS 리소스 삭제${NC}"
echo -e "${RED}==========================================${NC}"

# 확인 프롬프트
read -p "정말로 모든 리소스를 삭제하시겠습니까? (y/N): " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "취소되었습니다."
    exit 0
fi

# 스크립트 디렉토리 기준으로 k8s 폴더 경로 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"

cd "$K8S_DIR"

# 1. Ingress 삭제 (ALB 삭제 - 시간이 걸림)
echo -e "\n${YELLOW}🌐 [1/6] Ingress (ALB) 삭제...${NC}"
kubectl delete -f ingress.yaml 2>/dev/null || true
echo "⏳ ALB 삭제 대기 (30초)..."
sleep 30

# 2. HPA 삭제
echo -e "\n${YELLOW}📈 [2/6] HPA 삭제...${NC}"
kubectl delete -f hpa/ 2>/dev/null || true

# 3. 서비스 삭제
echo -e "\n${YELLOW}🚀 [3/6] 애플리케이션 서비스 삭제...${NC}"
kubectl delete -f services/ 2>/dev/null || true

# 4. RabbitMQ 삭제
echo -e "\n${YELLOW}🐰 [4/6] RabbitMQ 삭제...${NC}"
kubectl delete -f rabbitmq/ 2>/dev/null || true

# 5. MySQL 삭제 (PVC 포함)
echo -e "\n${YELLOW}🗄️ [5/6] MySQL 삭제...${NC}"
kubectl delete -f mysql/ 2>/dev/null || true

# PVC 삭제
echo "💾 PVC 삭제..."
kubectl delete pvc --all -n ${NAMESPACE} 2>/dev/null || true

# 6. ConfigMap, Secrets 삭제
echo -e "\n${YELLOW}🔧 [6/6] ConfigMap & Secrets 삭제...${NC}"
kubectl delete -f configmap.yaml 2>/dev/null || true
kubectl delete -f secrets.yaml 2>/dev/null || true

# 네임스페이스 삭제 여부 확인
echo ""
read -p "네임스페이스(${NAMESPACE})도 삭제하시겠습니까? (y/N): " delete_ns
if [[ "$delete_ns" == "y" || "$delete_ns" == "Y" ]]; then
    echo -e "\n${YELLOW}📁 Namespace 삭제...${NC}"
    kubectl delete namespace ${NAMESPACE} 2>/dev/null || true
fi

# 상태 확인
echo -e "\n${GREEN}==========================================${NC}"
echo -e "${GREEN}📊 삭제 후 상태 확인${NC}"
echo -e "${GREEN}==========================================${NC}"

echo -e "\n${YELLOW}🔹 Pods:${NC}"
kubectl get pods -n ${NAMESPACE} 2>/dev/null || echo "네임스페이스가 삭제되었거나 리소스가 없습니다."

echo -e "\n${GREEN}==========================================${NC}"
echo -e "${GREEN}✅ 삭제 완료!${NC}"
echo -e "${GREEN}==========================================${NC}"

echo -e "\n${YELLOW}💡 EKS 클러스터 자체를 삭제하려면:${NC}"
echo "eksctl delete cluster --name teamproject-cluster --region ap-northeast-2"