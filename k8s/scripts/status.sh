#!/bin/bash

# ===========================================
# TORI COFFEE - 상태 확인 스크립트
# 사용법: ./status.sh
# ===========================================

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

NAMESPACE="tori-app"

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}📊 TORI COFFEE 클러스터 상태${NC}"
echo -e "${GREEN}==========================================${NC}"

# 노드 상태
echo -e "\n${YELLOW}🔹 노드 상태:${NC}"
kubectl get nodes 2>/dev/null || echo "클러스터에 연결할 수 없습니다."

# 파드 상태
echo -e "\n${YELLOW}🔹 파드 상태:${NC}"
kubectl get pods -n ${NAMESPACE} 2>/dev/null || echo "파드가 없거나 네임스페이스가 없습니다."

# 서비스 상태
echo -e "\n${YELLOW}🔹 서비스 상태:${NC}"
kubectl get svc -n ${NAMESPACE} 2>/dev/null || echo "서비스가 없습니다."

# Ingress (ALB)
echo -e "\n${YELLOW}🔹 Ingress (ALB):${NC}"
kubectl get ingress -n ${NAMESPACE} 2>/dev/null || echo "Ingress가 없습니다."

# HPA
echo -e "\n${YELLOW}🔹 HPA (Auto Scaling):${NC}"
kubectl get hpa -n ${NAMESPACE} 2>/dev/null || echo "HPA가 설정되지 않았습니다."

# 리소스 사용량
echo -e "\n${YELLOW}🔹 리소스 사용량:${NC}"
kubectl top pods -n ${NAMESPACE} 2>/dev/null || echo "Metrics Server가 설치되지 않았거나 데이터가 없습니다."

# ALB URL 출력
ALB_URL=$(kubectl get ingress -n ${NAMESPACE} -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "")
if [ -n "$ALB_URL" ]; then
    echo -e "\n${GREEN}==========================================${NC}"
    echo -e "🌐 접속 URL: ${YELLOW}http://${ALB_URL}${NC}"
    echo -e "${GREEN}==========================================${NC}"
fi

# 문제가 있는 파드 확인
echo -e "\n${YELLOW}🔹 문제가 있는 파드:${NC}"
PROBLEM_PODS=$(kubectl get pods -n ${NAMESPACE} --field-selector=status.phase!=Running,status.phase!=Succeeded 2>/dev/null)
if [ -z "$PROBLEM_PODS" ]; then
    echo -e "${GREEN}모든 파드가 정상 작동 중입니다! ✅${NC}"
else
    echo -e "${RED}$PROBLEM_PODS${NC}"
fi
