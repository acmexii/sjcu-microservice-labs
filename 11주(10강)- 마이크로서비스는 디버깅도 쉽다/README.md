## 마이크로서비스는 디버깅도 쉽다

#### 객체 생성
```
kubectl create deployment nginx --image=nginx
```

#### 객체 정보 열람하기
```
kubectl get deploy -o wide
kubectl get pod -o wide
```

#### 객체 상세 정보 확인하기
```
kubectl describe [pod/객체 이름]
kubectl describe [deploy/nginx]
```

### Pod Status에 따른 Trouble Shooting

### 1. ImagePullBackOff/ ErrImagePull Status 일때,

- 원인 : 이미지 저장소에 이미지 부재, 오타(Typo) , 혹은 권한 문제로 이미지 호출이 불가한 경우
- 해결책 : 이미지 풀 네임 및 저장소 존재 유무 확인, 권한 문제 해결 
```
# Deploy with a non-existent image name
kubectl create deployment nginx --image=nginx:1q2w3e4r01010101
kubectl get pod
# Trouble shooting
kubectl describe [pod/객체 이름]
```

### 2. CrashLoopBackOff/ Errors Status 일때,

- 원인 : 이미지는 존재하나, 런타임에서 컨테이너 생성 시 오류
- 해결책 : 컨테이너 출력 스트림을 조회하여 (DevOps 개발자가) 오류 해결 
```
# Deploying images with errors
kubectl create deployment order --image=apexacme/order:latest
kubectl get pod
# Trouble shooting
kubectl logs [pod/객체 이름]
kubectl logs -f [pod/객체 이름]
```

### 3. Running Status이지만 서비스 확인이 안될때,

- 원인 : 컨테이너는 정상적으로 생성되었으나, 잘못된 Port 매핑 오류 등
- 해결책 : 컨테이너 내부에 직접 접속하여 의심가는 체크포인트에 필요한 툴을 설치하여 해결 
```
# Assume that deployed Nginx is not queried
kubectl get pod
kubectl exec -it [pod/객체 이름] -- /bin/bash
ls
df -k
curl localhost:80
exit
```

### 4. Pending Status가 지속될 경우,

- 원인 : worker node 갯수가 부족한 경우, Pod 의 status 가 pending 에서 변하지 않음
- 해결책 : 워크 노드 풀을 확장하거나, 혹은 쓰지 않는 객체를 삭제하여 리소스를 확보

### 5. Evicted Status가 지속될 경우,

- 원인 : 이미 스케쥴링 된 pod가  실행 중 노드 메모리나 CPU 가 부족해  쫓겨난 경우
- 또는 Pod 자체의 사이즈가 커서 어느 node 도 받아들일 수 없는 경우
- 해결책 : 배포 시 리소스 Spec.을 명시하거나, 노드 스케일 업(Scale Up) 필요

