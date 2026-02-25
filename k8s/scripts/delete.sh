#!/bin/bash

echo "=========================================="
echo "Kubernetes 리소스 삭제"
echo "=========================================="

# 역순으로 삭제
echo "Ingress 삭제..."
kubectl delete -f k8s/ingress.yaml --ignore-not-found

echo "서비스 삭제..."
kubectl delete -f k8s/services/ --ignore-not-found

echo "RabbitMQ 삭제..."
kubectl delete -f k8s/rabbitmq/ --ignore-not-found

echo "MySQL 삭제..."
kubectl delete -f k8s/mysql/ --ignore-not-found

echo "ConfigMap, Secret 삭제..."
kubectl delete -f k8s/base/configmap.yaml --ignore-not-found
kubectl delete -f k8s/base/secrets.yaml --ignore-not-found

echo "Namespace 삭제..."
kubectl delete -f k8s/base/namespace.yaml --ignore-not-found

echo "=========================================="
echo "삭제 완료!"
echo "=========================================="