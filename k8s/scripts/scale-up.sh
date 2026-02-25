#!/bin/bash

# ===========================================
# TORI COFFEE - 노드 스케일 업 스크립트
# 사용법: ./scale-up.sh [노드수]
# 기본값: 3개 노드
# ===========================================

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

AWS_REGION="ap-northeast-2"
CLUSTER_NAME="teamproject-cluster"
DESIRED_NODES=${1:-3}  # 인자가 없으면 기본값 3

echo -e "${GREEN}==========================================${NC}"
echo -e "${GREEN}🚀 TORI COFFEE 노드 스케일 업${NC}"
echo -e "${GREEN}==========================================${NC}"

# 현재 노드그룹 확인
echo -e "\n${YELLOW}📊 현재 노드그룹 확인...${NC}"
NODEGROUP_NAME=$(aws eks list-nodegroups --cluster-name ${CLUSTER_NAME} --region ${AWS_REGION} --query 'nodegroups[0]' --output text)

if [ -z "$NODEGROUP_NAME" ] || [ "$NODEGROUP_NAME" == "None" ]; then
    echo -e "${RED}❌ 노드그룹을 찾을 수 없습니다.${NC}"
    exit 1
fi

echo "노드그룹: ${NODEGROUP_NAME}"
echo "목표 노드 수: ${DESIRED_NODES}"

# 스케일 업 실행
echo -e "\n${YELLOW}⏳ 노드 스케일 업 중...${NC}"
aws eks update-nodegroup-config \
    --cluster-name ${CLUSTER_NAME} \
    --nodegroup-name ${NODEGROUP_NAME} \
    --scaling-config minSize=1,maxSize=5,desiredSize=${DESIRED_NODES} \
    --region ${AWS_REGION}

echo -e "\n${GREEN}✅ 스케일 업 요청 완료!${NC}"
echo ""
echo "노드가 시작되는 데 3-5분이 걸릴 수 있습니다."
echo ""
echo -e "${YELLOW}💡 노드 상태 확인:${NC}"
echo "kubectl get nodes -w"
echo ""
echo -e "${YELLOW}💡 노드 준비 후 서비스 배포:${NC}"
echo "./deploy.sh"
