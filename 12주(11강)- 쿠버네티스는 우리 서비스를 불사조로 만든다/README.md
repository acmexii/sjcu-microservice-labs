## 쿠버네티스는 우리 서비스를 불사조로 만든다

클라우드의 리소스를 잘 활용하기 위해서는 요청이 적을때는 최소한의 Pod 를 유지한 후에 요청이 많아질 경우 Pod를 확장하여 요청을 처리할 수 있다.  
Pod 를 Kubernetes에서 수평적으로 확장하는 방법을 HorizontalPodAutoscaler(HPA) 라고 부른다. replicas 를 관리하는 Deployment, StatefulSet 에 적용이 가능하고, 확장이 불가능한 DaemonSets 에는 설정이 불가능하다.  

HPA는 워크로드의 CPU 또는 메모리를 측정하여 작동하기 때문에 Kubernetes 에 metric server가 필수적으로 설치가 되어있어야 한다.

이번시간에는 HPA 설정을 적용 한 후에, siege 라는 부하 테스트 툴을 사용하여 서비스에 부하를 주어 Pod 가 Auto Scale-Out 되는 과정을 실습한다.


### GitPod에 클라우드 환경 설정하기

- AWS 클라우드에서 Kubernetes가 생성되어 있어야 한다. 
- Kubernetes가 생성되어 있지 않으면 해당 Lab(08주 7강. AWS 클라우드에 쿠버네티스생성 실습)을 참조하여 생성한다.

#### GitPod에 AWS Client Single Sign-On 설정
- 먼저, https://awsacademy.instructure.com/ 에 들어가 Lab을 Start 한다.
- AWS Details 메뉴에서 AWS CLI 옆 'Show'를 클릭한다.
- 나타나는 모든 정보를 텍스트 에디터에 복사해 둔다.
- 터미널에서 아래 내용대로 설정을 진행(매칭되는 정보입력) 한다. 
```
aws configure
AWS Access Key ID [None]: # Value 입력
AWS Secret Access Key [None]: # Value 입력
Default region name [None]: us-east-1
Default output format [None]: json
```

- 생성된 aws credentials 화일에 aws token을 추가한다.
```
vi ~/.aws/credentials
# 맨 아래에 텍스트 에디터에 있는 aws_session_token 전체 행을 추가하고 저장한다.
```

#### GitPod에 Kubernetes Client Single Sign-On 설정

- 내가 생성한 클러스터 이름으로 설정한다. (Cluster 이름이 abc-eks 일 경우)
```
aws eks update-kubeconfig --name abc-eks
kubectl get all  # 확인
```


### 주문서비스 배포 및 라우터 생성

- GitPod > Lab 폴더 마우스 오른쪽 클릭 > New File > order.yaml 입력
- 아래 내용 복사하여 붙여넣기

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
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
          image: ghcr.io/acmexii/order-liveness:latest
          ports:
            - containerPort: 8080 
          resources:
            requests:
              cpu: "200m"    
```

```
kubectl expose deploy order --port=8080
```

- 부하 테스트 Pod 설치
  - 워크로드 생성기를 설치하여 자동 확장 랩에 활용한다.
  - 아래 스크립트를 terminal 에 복사하여 siege 라는 Pod 를 생성한다.
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
- 생성된 siege Pod 안쪽에서 정상작동 확인
```
kubectl exec -it siege -- /bin/bash
siege -c1 -t2S -v http://order:8080/orders
exit
```

- Metric server 설치 확인 방법
  - kubectl top pods 를 실행했을때, 아래와 같이 정보가 나오면 설치가 되어있다.
```
NAME                     CPU(cores)   MEMORY(bytes)   
order-684647ccf9-ltlqg   3m           288Mi           
siege                    0m           8Mi   
```
- "error: Metrics API not available" 메시지가 나오면 metric server가 설치되지 않은은 것으로 아래와 같은 명령어로 설치한다.
> kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
> kubectl get deployment metrics-server -n kube-system

### Auto Scale-Out 설정

1.0 Auto Scaler를 설정한다
- 오토 스케일링 설정명령어 호출
```
kubectl autoscale deployment order --cpu-percent=50 --min=1 --max=3
```

- "cpu-percent=50 : Pod 들의 요청 대비 평균 CPU 사용율(YAML Spec.에서 요청량이 200 milli-cores일때, 모든 Pod의 평균 CPU 사용율이 100 milli-cores(50%)를 넘게되면 HPA 발생)"

- kubectl get hpa 명령어로 설정값을 확인 한다.
```
NAME    REFERENCE          TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
order   Deployment/order   <unknown>/20%   1         3         0          7s
```


### Auto Scale-Out 증명


2.1 새로운 터미널을 열어서 seige 명령으로 부하를 주어서 Pod 가 늘어나도록 한다.
```
kubectl exec -it siege -- /bin/bash
siege -c20 -t40S -v http://order:8080/orders
exit
```

2.2 터미널 1개는 kubectl get po -w 명령을 사용하여 pod 가 생성되는 것을 확인한다.
```
order-7b76557b8f-bgptv   1/1     Running   0          34m
siege                    1/1     Running   0          33m
order-7b76557b8f-7g9d6   0/1     Pending   0          0s
order-7b76557b8f-hmssb   0/1     Pending   0          0s
order-7b76557b8f-7g9d6   0/1     ContainerCreating   0          0s
order-7b76557b8f-hmssb   0/1     ContainerCreating   0          0s
order-7b76557b8f-7g9d6   0/1     Running             0          6s
order-7b76557b8f-hmssb   0/1     Running             0          6s
order-7b76557b8f-7g9d6   1/1     Running             0          23s
order-7b76557b8f-hmssb   1/1     Running             0          27s
``` 

2.3 kubectl get hpa 명령어로 CPU 값이 늘어난 것을 확인 한다.
```
NAME    REFERENCE          TARGETS     MINPODS   MAXPODS   REPLICAS   AGE
order   Deployment/order   1152%/20%   1         3         3          37m
```



## 무정지 배포 실습 (readinessProbe, 제로 다운타임)

클러스터에 배포를 할때 readinessProbe 설정이 없으면 다운타임이 존재 하게 된다. 이는 쿠버네티스에서 Ramped 배포 방식으로 무정지 배포를 시도 하지만, 서비스가 기동하는 시간이 있기 때문에 기동 시간동안에 트래픽이 유입되면 장애가 발생 할 수 있다.  

배포시 다운타임의 존재 여부를 확인하기 위하여, siege 라는 부하 테스트 툴을 사용한다.  
배포시작전에 부하테스트 툴을 실행하고, 배포 완료시 종료한 후, 결과값인 Availability 를 체크 하여 어느정도의 실패가 있었는지를 확인한다.

- 먼저 이전 실습에서 생성된 주문과 HPA 객체를 삭제한다.
```
kubectl delete deploy order,hpa --all
```

- Lab 폴더 마우스 오른쪽 클릭 > New File > deployment.yaml 입력
- 아래 내용 복사하여 붙여넣기
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 1
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
          image: jinyoung/order:stable
          ports:
            - containerPort: 8080
```

- 주문서비스를 배포한다:
```
kubectl apply -f deployment.yaml
```

서비스 객체를 위한 yaml도 만든다:

```
apiVersion: "v1"
kind: "Service"
metadata: 
  name: "order"
  labels: 
    app: "order"
spec: 
  ports: 
    - 
      port: 8080
      targetPort: 8080
  selector: 
    app: "order"
  type: "ClusterIP"
```
를 붙여넣기 하여 생성한 다음,

```
kubectl apply -f service.yaml
```
로 서비스 객체를 생성한다.


- 새로운 터미널을 오픈하고, Siege로 접속해 주문서비스 정상작동 확인
```
kubectl exec -it siege -- /bin/bash
siege -c1 -t2S -v http://order:8080/orders
```
 
### 1. readinessProbe 가 없는 상태에서 배포 진행

1.1 새 버전을 배포할 준비를 한다:
deployment.yaml 의 이미지 정보를 아래와 같이 변경한 후 (19라인):
```
image: jinyoung/order:canary
```

1.2 새로운 터미널을 열어서 충분한 시간만큼 부하를 준다.

```
kubectl exec -it siege -- /bin/bash
siege -c1 -t60S -v http://order:8080/orders --delay=1S
```

1.3. 배포를 반영한다:

```
kubectl apply -f deployment.yaml
```

1.5 siege 로그를 보면서 배포시 정지시간이 발생한것을 확인한다.
```
Transactions:                     82 hits
Availability:                  70.09 %
Elapsed time:                  59.11 secs
```

### 2. readinessProbe 를 설정하고 배포 진행

2.1 아래와 같이 readiness 설정을 주입한다:

```
    spec:
      containers:
        - name: order
				  ...
          readinessProbe:    # 이부분!
            httpGet:
              path: '/orders'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```
2.2. image 명도 변경한다 (19라인):
```
				image: jinyoung/order:stable
```

2.2 siege 터미널을 열어서 충분한 시간만큼 부하를 준다.

```
kubectl exec -it siege -- /bin/bash
siege -c1 -t60S -v http://order:8080/orders --delay=1S
```

2.3 수정된 주문 서비스를 적용하여 배포한다
- kubectl apply -f deployment.yaml


2.5 siege 로그를 보면서 배포시 무정지로 배포된 것을 확인한다.
```
Transactions:                    112 hits
Availability:                 100.00 %
Elapsed time:                  59.58 secs
```
