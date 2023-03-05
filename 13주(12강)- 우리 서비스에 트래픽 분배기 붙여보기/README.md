## 우리서비스에 트래픽 분배기 붙여보기

쿠버네티스는 클러스터에 배포된 마이크로서비스별 컨테이너에 접속하기 위해 **"Service"** 객체를 제공한다. 이 Service로 접속하게 되면 연결된 컨테이너로 요청이 프락시된다. 

이번 실습에서는 이 Service에 대해 알아본다.

- 마이크로서비스들의 라우터인 Serivce Scope에 대해 이해한다.
- 마이크로서비스까지 요청이 전달되는 과정에 Kube Proxy의 역할에 대해 학습한다.

### Service basic template
```
  apiVersion: v1
  kind: Service
  metadata:
    name: my-service
  spec:
    selector:
      app: order
    ports:
      - protocol: TCP
        port: 8080
        targetPort: 8080
    type: ClusterIP/NodePort/LoadBalancer		
```

### 대상 컨테이너 생성
```
kubectl create deploy order --image=ghcr.io/acmexii/order-liveness:latest            
```

### ClusterIP Type Service 생성 

- 클러스터 내에서만 접근 가능한 라우터를 생성한다.
```
kubectl expose deploy order --type=ClusterIP --port=8080 --target-port=8080
# 생성된 ClusterIP 정보확인
kubectl get service 
# Selector 확인
kubectl get service order -o yaml
```
- 새로운 Terminal에서 클라이언트용 컨테이너를 생성하고 접속한다.
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
```
kubectl exec -it siege -- /bin/bash
```
- 위에서 확인된 Order 서비스의 ClusterIP로  접근한다.
```
http http://10.x.x.x:8080
```

### NodePort Type Service 생성

- 워크 노드의 포트를 통해서도 접근 가능한 라우터를 다시 생성한다.  
```
kubectl delete service order
kubectl expose deploy order --type=NodePort --port=8080 --target-port=8080
# 생성된 NodePort 정보확인
kubectl get service 
```

- NodePort로 서비스 접근
- Order 컨테이너가 바인딩 된 Worker-Node를 확인한다.
```
kubectl get pod -o wide
```

- 해당 워크노드의 Cluster IP를 통해 접근한다.
```
# Worker-Node IP 확인
kubectl get nodes -o wide
# siege 터미널에서 NodePort 접속
http http://192.168.x.x:3xxxx
```

- Order Pod가 바인딩 되지않은 다른 Worker-Node를 통해서도 접근해 본다.
- 접근이 된다, 이유는
- 라우터 생성시 모든 Node상에 있는 Kube-proxy Daemon이 Networking Rule 작업(Iptables 갱신)을 수행하므로 어느 Node로 접속해도 접근 가능하다. 
- Rule 확인하기
```
# AWS Console을 통해 EC2 > Auto Scaling Group > Work-Node에 SSH로 접근
$ iptables -t nat -S | grep 8080
```

### LoadBalancer Type Service 생성 

- 워크 노드의 포트 뿐만 아니라 클라우드 외부에서도 접근 가능한 라우터를 다시 생성한다.  
```
kubectl delete service order
kubectl expose deploy order --type=LoadBalancer --port=8080 --target-port=8080
# 생성된 LoadBalancer 정보확인
kubectl get service
```

- LoadBalancer 엔드포인트로 서비스 접근
```
# Order Service의 External-IP 복사   
# Web Browser를 통한 접속
```

### Kube-DNS A레코드를 통한 접근

```
# siege 터미널에서
http http://order:8080
```
- Kubernetes Cluster에서 서비스가 생성되면 Kube-DNS에 A레코드가 등록되고, 서비스가 삭제되면 A레코드 또한 자동 삭제한다.
- Kube-DNS 정보확인
- kubectl get service -n kube-system
- kubernetes는 컨테이너 생성시에 NameServer(Kube-DNS) 정보를 자동 Injection 한다.
```
$ cat /etc/resolv.conf
```
- 마이크로서비스간 참조시, Service ClusterIP 또한 유동적이므로 Cluster 내에서는 '서비스 이름'으로 접근한다.
