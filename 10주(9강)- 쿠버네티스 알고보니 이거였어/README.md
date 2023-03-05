## 쿠버네티스 알고보니 이거였어 

### 주문서비스 생성하기 


- 도커 허브에 저장된 주문 이미지으로 서비스 배포 및 확인하기

```
kubectl create deploy order --image=jinyoung/monolith-order:v202105042
kubectl get all  # 모든 객체 조회
kubectl get po # pod 객체만 조회 (pod name 확인)
```


### 주문서비스 삭제해 보기 

```
# New Terminal (관측용)
watch kubectl get pod
kubectl delete pod -l app=order 
```

- Pod를 삭제해도 새로운 Pod로 서비스가 재생성됨을 확인


### 클라우드 외부에서도 접근 가능하도록 노출하기

```
kubectl expose deploy order --type=LoadBalancer --port=8080
kubectl get service 
```

> External IP를 얻어오는데 오래걸리거나, ALB 등이 연결되는데 시간이 걸리는 경우 다음의 port-forwarding 명령으로 localhost 에 접속할 수 있다: 
```
# 새 터미널
kubectl port-forward deploy/order 8080:8080

# 다른 터미널
curl localhost:8080
```

- Service 정보의 External IP가 Pending 상태에서 IP정보로 변경시까지 대기하기
- 엔드포인트를 통해 서비스 확인 - http://(IP정보):8080/orders
- Ctrl + C를 눌러 모니터링 모드 종료하기 

접속테스트:
```
# http a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 26 May 2021 06:26:06 GMT
Transfer-Encoding: chunked

{
    "_links": {
        "deliveries": {
            "href": "http://a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080/deliveries{?page,size,sort}",
            "templated": true
        },
        "orders": {
            "href": "http://a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080/orders{?page,size,sort}",
            "templated": true
        },
        "productOptions": {
            "href": "http://a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080/productOptions"
        },
        "products": {
            "href": "http://a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080/products{?page,size,sort}",
            "templated": true
        },
        "profile": {
            "href": "http://a78bb72215adc4a7c9db56a0c9acc457-1497647582.ap-northeast-2.elb.amazonaws.com:8080/profile"
        }
    }
}
```


### 주문서비스 인스턴스 확장(Scale-Out) 하기 (수동)

```
kubectl scale deploy order --replicas=3
kubectl get pod
```

- 주문서비스의 인스턴스(Pod)가 3개로 확장됨을 확인


### YAML 기반 서비스 배포하기

- GitPod > Lab 폴더 마우스 오른쪽 클릭 > New File > order.yaml 입력
- 아래 내용 복사하여 붙여넣기

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-by-yaml
  labels:
    app: order
spec:
  replicas: 2
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: jinyoung/monolith-order:v20210504
          ports:
            - containerPort: 8080        
```

- 입력 후, 저장
```
- kubectl apply -f order.yaml 
- kubectl get all 
```
