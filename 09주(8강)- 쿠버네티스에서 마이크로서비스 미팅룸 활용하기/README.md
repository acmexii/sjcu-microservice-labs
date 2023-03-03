## 쿠버네티스에 마이크로서비스 미팅룸 활용

- 쿠버네티스 상에 배포된 마이크로서비스들 간의 Kafka 통신을 실습한다.
- Order 서비스에서 OrderPlaced 이벤트가 발생했을때 Inventory 서비스가 OrderPlaced 이벤트를 수신하여 재고량을 변경(감소)한다. 

### 마이크로서비스 배포하기

- 주문서비스와 상품서비스를 쿠버네티스에 배포한다.

#### order 서비스 배포

```
cd order
mvn package 
docker image build -t MY-DOCKER-ID/order:v0.1 .
docker login 
docker push 
docker push MY-DOCKER-ID/order:v0.1
```

```
cd kubernetes
# 19라인을 내 이미지 이름(MY-DOCKER-ID/order:v0.1) 으로 수정
kubectl apply -f deployment.yaml
kubectl apply -f servce.yaml
```

#### inventory 서비스 배포

```
cd inventory
mvn package 
docker image build -t MY-DOCKER-ID/product:v0.1 .
docker login 
docker push 
docker push MY-DOCKER-ID/product:v0.1
```

```
cd kubernetes
# 19라인을 내 이미지 이름(MY-DOCKER-ID/product:v0.1) 으로 수정
kubectl apply -f deployment.yaml
kubectl apply -f servce.yaml
```

### 쿠버네티스에 미팅룸(kafka) 설치

#### Kubernetes 패키지 인스톨러인 Helm 을 먼저 로컬에 설치한다.

- Helm(패키지 인스톨러) 설치
```bash
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
```

#### Kafka (namspace없이) 설치
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install my-kafka bitnami/kafka
```

#### Kafka 메시지 확인하기 
```bash
kubectl run my-kafka-client --restart='Never' --image docker.io/bitnami/kafka:2.8.0-debian-10-r0 --command -- sleep infinity
kubectl exec --tty -i my-kafka-client -- bash

# CONSUMER:
kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic labshoppubsub --from-beginning
```


### 마이크로서비스 테스트 

- 먼저, 테스트에 필요한 클라이언트를 쿠버네티스에 설치하고 접속한다.
```
kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
EOF
```

- 생성한 클라이언트로 접속한다.
```
kubectl exec -it siege -c siege -- /bin/bash
```

#### 쿠버네티스 상에서의 Pub/Sub을 테스트한다.

- 상품을 등록하고, 등록한 상품을 주문해 본다.
- 카프카 클라이언트로 토픽상에 생성되는 도메인 이벤트를 확인한다.
```
http :8082/inventories id=1 stock=10
http :8081/orders productId=1 qty=5
http :8082/inventories/1
```
