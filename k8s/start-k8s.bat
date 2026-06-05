@echo off
rem
cd /d "%~dp0.."

echo Compilando as imagens Docker...
docker build -t config-server:latest ./services/config-server
docker build -t eureka-server:latest ./services/eureka-server
docker build -t api-gateway:latest ./services/api-gateway
docker build -t sensor-service:latest ./services/sensor-service
docker build -t alert-service:latest ./services/alert-service

echo Iniciando
kubectl get hpa
kubectl apply -f ./k8s/1-postgres-db.yaml
kubectl apply -f ./k8s/4-config-server.yaml
echo Aguardando estabilizar
timeout /t 20 /nobreak >nul

kubectl apply -f ./k8s/5-eureka-server.yaml
echo Aguardando estabilizar
timeout /t 20 /nobreak >nul

kubectl apply -f ./k8s/6-api-gateway.yaml
kubectl apply -f ./k8s/8-hpa-gateway.yaml
kubectl apply -f ./k8s/2-sensor-service.yaml
kubectl apply -f ./k8s/3-hpa-sensor.yaml
kubectl apply -f ./k8s/7-alert-service.yaml
kubectl apply -f ./k8s/9-hpa-alert.yaml
echo Sucesso