#!/bin/bash

# AWS 설정 - 실제 값으로 변경하세요
AWS_ACCOUNT_ID="YOUR_AWS_ACCOUNT_ID"
AWS_REGION="ap-northeast-2"

echo "=========================================="
echo "Kubernetes 배포 시작"
echo "=========================================="

# 이미지 태그 치환
echo "이미지 태그 설정..."
find k8s/services -name "*.yaml" -exec sed -i "s/\${AWS_ACCOUNT_ID}/${AWS_ACCOUNT_ID}/g" {} \;
find k8s/services -name "*.yaml" -exec sed -i "s/\${AWS_REGION}/${AWS_REGION}/g" {} \;

# 1. Namespace 생성
echo "1. Namespace 생성..."
kubectl apply -f k8s/base/namespace.yaml

# 2. ConfigMap, Secret 생성
echo "2. ConfigMap, Secret 생성..."
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets.yaml

# 3. MySQL 배포
echo "3. MySQL 배포..."
kubectl apply -f k8s/mysql/

# MySQL이 준비될 때까지 대기
echo "MySQL 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=mysql-member -n tori-app --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-product -n tori-app --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-order -n tori-app --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-inventory -n tori-app --timeout=300s

# 4. RabbitMQ 배포
echo "4. RabbitMQ 배포..."
kubectl apply -f k8s/rabbitmq/

# RabbitMQ 준비 대기
echo "RabbitMQ 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=rabbitmq -n tori-app --timeout=180s

# 5. Eureka Server 배포
echo "5. Eureka Server 배포..."
kubectl apply -f k8s/services/eureka-server.yaml

# Eureka 준비 대기
echo "Eureka Server 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=eureka-server -n tori-app --timeout=180s

# 6. 나머지 서비스 배포
echo "6. 애플리케이션 서비스 배포..."
kubectl apply -f k8s/services/gateway-service.yaml
kubectl apply -f k8s/services/frontend-service.yaml
kubectl apply -f k8s/services/member-service.yaml
kubectl apply -f k8s/services/product-service.yaml
kubectl apply -f k8s/services/order-service.yaml
kubectl apply -f k8s/services/inventory-service.yaml
kubectl apply -f k8s/services/board-service.yaml
kubectl apply -f k8s/services/admin-service.yaml

# 7. Ingress 배포
echo "7. Ingress 배포..."
kubectl apply -f k8s/ingress.yaml

echo "=========================================="
echo "배포 완료!"
echo "=========================================="
echo ""
echo "Pod 상태 확인:"
kubectl get pods -n tori-app
echo ""
echo "Service 상태 확인:"
kubectl get svc -n tori-app
echo ""
echo "Ingress 확인:"
kubectl get ingress -n tori-app