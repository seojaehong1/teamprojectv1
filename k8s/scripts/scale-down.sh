#!/bin/bash

# ===========================================
# TORI COFFEE - 노드 스케일 다운 스크립트
# 사용법: ./scale-down.sh
# 비용 절약을 위해 노드를 0으로 줄입니다.
# ===========================================

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

AWS_REGION="ap-northeast-2"
CLUSTER_NAME="teamproject-cluster"

echo -e "${YELLOW}==========================================${NC}"
echo -e "${YELLOW}💰 TORI COFFEE 노드 스케일 다운${NC}"
echo -e "${YELLOW}==========================================${NC}"

# 현재 노드그룹 확인
echo -e "\n${YELLOW}📊 현재 노드그룹 확인...${NC}"
NODEGROUP_NAME=$(aws eks list-nodegroups --cluster-name ${CLUSTER_NAME} --region ${AWS_REGION} --query 'nodegroups[0]' --output text)

if [ -z "$NODEGROUP_NAME" ] || [ "$NODEGROUP_NAME" == "None" ]; then
    echo -e "${RED}❌ 노드그룹을 찾을 수 없습니다.${NC}"
    exit 1
fi

echo "노드그룹: ${NODEGROUP_NAME}"

# 현재 노드 수 확인
CURRENT_NODES=$(kubectl get nodes --no-headers 2>/dev/null | wc -l)
echo "현재 노드 수: ${CURRENT_NODES}"

# 확인 프롬프트
echo ""
echo -e "${RED}⚠️ 주의: 노드를 0으로 줄이면 모든 Pod가 종료됩니다!${NC}"
echo "EKS Control Plane 비용 (~\$73/월)만 유지됩니다."
echo ""
read -p "노드를 0으로 줄이시겠습니까? (y/N): " confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "취소되었습니다."
    exit 0
fi

# 스케일 다운 실행
echo -e "\n${YELLOW}⏳ 노드 스케일 다운 중...${NC}"
aws eks update-nodegroup-config \
    --cluster-name ${CLUSTER_NAME} \
    --nodegroup-name ${NODEGROUP_NAME} \
    --scaling-config minSize=0,maxSize=5,desiredSize=0 \
    --region ${AWS_REGION}

echo -e "\n${GREEN}✅ 스케일 다운 요청 완료!${NC}"
echo ""
echo "노드가 종료되는 데 몇 분이 걸릴 수 있습니다."
echo ""
echo -e "${YELLOW}💡 다시 켜려면:${NC}"
echo "./scale-up.sh"
echo ""
echo "또는 수동으로:"
echo "aws eks update-nodegroup-config --cluster-name ${CLUSTER_NAME} --nodegroup-name ${NODEGROUP_NAME} --scaling-config minSize=1,maxSize=5,desiredSize=3 --region ${AWS_REGION}"
