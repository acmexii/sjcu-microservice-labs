## AWS에 쿠버네티스 만들고 마이크로서비스 배포하기


## 쿠버네티스 만들기

- Academy Learner Lab을 통해 아마존 AWS에 Kubernetes를 설치하고 쿠버네티스 클라이언트와 연동하는 내용이다.  
Text Editor(windows - 메모장)이 반드시 필요하며, 설명 중간중간 Text Editor에 메모 요구가 나오면 이후 사용을 위해 반드시 메모하면서 진행한다.

- 사전 환경
  > AWS Learner Lab 로그인 필수 (아래 참조)
  
  > Gitpod based 랩환경 사전설정 필수
[(Gitpod-based Lab Environments 참고링크)](https://github.com/acmexii/msaez-labs/tree/main/06%EA%B0%95_Sample-Order-Microservice#configure-web-based-rumtime-environments)


### Connect to AWS Learner Lab & Ignition
- AWS Academy Lab - https://awsacademy.instructure.com/ 에 발급받은 접속정보로 로그인한다.
- 대쉬보드 > 모듈 > Learner Lab - Associate Services 클릭한다.
![image](https://user-images.githubusercontent.com/35618409/187118228-d5a56653-ca93-440f-a855-18a72630c12e.png)
- 아래와 같이 터미널이 출력된다.
![image](https://user-images.githubusercontent.com/35618409/187118347-117ab92b-450d-4e6b-a3da-c5f4d5e90e91.png)
- 터미널 상단의 "▶Start Lab"을 클릭하여 터미널을 활성화한다.
- 'V' 형의 큰 이미지에 Spinner가 돌아가면서 Lab이 활성화된다.

- Lab이 활성화되고 나면 터미널 상단의 "<u>AWS</u>" 링크를 클릭한다.
- 새로운 탭이 열리면서 아마존 관리콘솔(AWS Console) Main 페이지가 출력된다.
![image](https://user-images.githubusercontent.com/35618409/187118466-12b742e2-7323-494a-8572-df2c22aa53b9.png)
  - 접속지역은 미국동부(버지니아 북부, us-east-1)이다. 화면 우측상단에서 확인가능
  - 이 us-east-1 가 리전 코드라고 부르며, 반드시 Text Editor에 복사해 두자.!!! 리전코드!!
- Main 페이지 상단에 보면, 서비스 검색을 위한 입력 필드가 있다.
- 이 입력란에 'Kubernetes'를 입력하고 검색된 첫번째  'Elastic Kubernetes Service'를 선택한다.
- 아래와 같이 쿠버네티스 서비스를 생성할 수 있는 화면이 출력된다.
![image](https://user-images.githubusercontent.com/35618409/187118637-0a60e652-cffa-44f3-a8ff-cb2a931e6bb0.png)
- '클러스터 추가' > '생성'를 클릭하여 쿠버네티스 생성을 이어지는 순서대로 진행한다.


### Creating Kubernetes Cluster 

#### 클러스터 구성
- 클러스터 이름을 영문으로 입력한다.
  - 예시: gdhong-eks
  - gdhong 대신에 반드시 '내이름영문명-eks' 형식의 정보로 수정한다. (Sample: lily-eks)
  - 클러스터 이름을 Text Editor에 꼭 메모하자.
- Kubernetes 버전을 1.21, 또는 1.22로 선택하자. (반드시)
- Kubernetes 버전과 Role을 디폴트설정으로 두고 '다음'을 클릭한다.
![image](https://user-images.githubusercontent.com/35618409/187119325-0578886e-d4ea-40b7-8b93-c0ae911b905c.png)
- 선택된 Kubernetes 버전을 다시 한번 확인한다. (1.23 미만으로 선택되었는지.)

#### 네트워킹 지정
- VPC를 기본값으로 둔다. 
- 서브넷 구성에서 목록을 열어 현재, 6개가 선택된 상태에서 us-east-1a, us-east-1b, us-east-1c만 선택되도록 d,e,f를 선택해제한다.
![image](https://user-images.githubusercontent.com/35618409/187119661-d464d3f7-4072-44e4-a61b-d5802ae6efa9.png)

- 2줄로 적힌 보안그룹 설명문을 자세히 살펴보면, 새보안그룹 생성에 링크가 걸려 있는데 컨트롤 키(Ctrl)를 누른 상태에서 'VPC콘솔' 링크를 클릭한다.
![image](https://user-images.githubusercontent.com/35618409/187119829-afbd28a7-11e8-4faa-a246-30cb20d328d2.png)
- 새로운 탭이 열리면서 보안그룹 생성화면이 나타난다.
- 우측 빨간색 '보안그룹 생성' 버튼을 클릭한다.
- 보안그룹 이름에 나만의 SecurityGroup명을 입력한다. (예시, 내이름영문명-securitygroup)
- 필수정보인 '보안그룹 설명' 필드에도 동일하게 보안그룹 이름을 입력한다.
- 인바운드 규칙에서 '규칙 추가'를 클릭한다.
- 유형에서 '모든 트래픽', 소스 유형은 'Anywhere-IPv4'를 선택한다.
- 우측 아래쪽에 있는 '보안그룹 생성' 버튼을 클릭해 정상적으로 생성되면 창을 닫는다.
![image](https://user-images.githubusercontent.com/35618409/187125888-f2a627a9-90b3-4306-8421-414e04612c13.png)

- 진행중이던 Cluster 생성 창에서 계속 진행한다.
- 보안그룹 선택 필드 우측의 Reload를 눌러 방금 생성한 보안그룹을 지정한다.
![image](https://user-images.githubusercontent.com/35618409/187126059-9db5a09a-fe34-44af-a6c5-f2da83dc0112.png)
- 나머지 설정을 Default로 두고 '다음'을 클릭한다.

#### 로깅 구성
- 모두 비활성화 상태에서 '다음'을 클릭한다.

#### 검토 및 생성
- 설정 확인 후, '생성'을 눌러 클러스터를 생성한다.


### Creating Cluster Worker Node

- 5~10분 정도 경과 후, Kubernetes 클러스터 생성이 완료된다. 
  - 클러스터 메뉴에 생성된 Cluster(ex. lily-eks)가 확인된다.

#### Adding WorkNode Group to my Cluster
- 내 클러스터를 클릭해 'Compute(컴퓨팅)' 탭에서 노드그룹을 추가한다.
- 'Compute(컴퓨팅)' 탭의 '노드그룹추가' 를 클릭한다.
![image](https://user-images.githubusercontent.com/35618409/187133568-9e46bc9f-4357-45e0-b8a4-d5dda8cdf3e1.png)

#### 노드 그룹 구성
- 이름에 노드 그룹명(ex. 내이름영문명-NodeGroup)을 입력한다.
- 노드 IAM 역할은 LabRole을 선택한다.

#### Kubernetes 레이블
- Kubernetes 레이블에서 '레이블 추가'를 눌러 
- 키 에는 'worker', 값 에는 노드 그룹명(ex. lily-eks-NodeGroup)을 입력한다. (필수)
- 아래에서 '다음' 클릭한다.

#### 컴퓨팅 및 조정 구성 설정
- 쿠버네티스 클러스터를 구성하는 워크노드(VM, EC2) 관련 설정이다.
- 모든 값을 default로 둔다.
- 아래에서 '다음' 클릭한다.


#### 네트워킹 지정
- VPC 서브넷 설정을 기본으로 둔다.
- 아래에서 '다음' 클릭한다.


#### 검토 및 생성
- Worker Node 추가에 필요한 모든 설정 확인 후, '생성'을 클릭한다.


### Check my Kubernetes Cluster & Woker Nodes
- 클러스터 메뉴에 생성된 내 클러스터가 조회된다.
- 내 클러스터를 클릭해 'Compute(컴퓨팅)' 탭을 눌러 생성된 워크노드을 확인한다. 


### Configure Kubernetes Client for Accessing from Gitpod  

생성된 AWS Kubernetes Cluster를 위한 Client 환경은 GitPod 상에서 연결 설정하여 사용한다.
(Learner Lab에서도 가능하지만, UI가 상대적으로 편리한 GitPod를 활용)


### Copy AWS Credentials to GitPod
- Learner Lab에서 AWS Credential을 복사하여 GitPod에 붙여넣는다.
  - Learner Lab 메뉴 중, AWS Details를 클릭하고, 출력된 내용에서 AWS CLI 'Show'를 클릭한다.
  - Region 코드가 us-east-1 로 조회된다. (다른 Value일 수도 있음)
  - AWS CLI 네모영역 안에 보여지는 모든 정보를 복사하여 Text Editor에 붙여넣기 해 둔다. 
![image](https://user-images.githubusercontent.com/35618409/187328088-7295b12a-c1de-498f-a3d0-2a2c312f8b9e.png)

- GitPod에 접속하면 터미널상에 스크립트가 자동 실행되면서 로딩이 완료된다.
- 터미널 로딩이 완료된 후, 쿠버네티스 클라이언트 설정을 위해 아래 Command를 입력한다.
- 나타나는 각 항목의 Value에 Text Editor에 있는, 또는 Learner Lab에서 조회된 정보를 입력한다.
```
aws configure
AWS Access Key ID [None]: Text Editor에 있는 aws_access_key_id의 Value 
AWS Secret Access Key [None]: Text Editor에 있는 aws_secret_access_key의 Value 
Default region name [None]: AWS Region 코드, Text Editor에 메모해 둔 코드
Default output format [None]: json
```

- 생성된 Credential 정보를 열어 aws_session_token을 추가한다.
```
cd ~/.aws
vi credentials
```
- Text Editor에 있는 aws_session_token 정보를 복사하여 추가한다.
- 붙여넣기가 완료되면 저장 종료(:wq)한다.

#### kubernetes Cluster Connect & Testing

- 설정된 aws 클라언트를 사용하여 Kubernetes Cluster와 클라이언트(kubectl)를 연결한다.
- 아래 REGION_CODE와 MY-CLUSTER-NAME을 Text Editor에 있는 내 정보로 수정하여 명령문을 실행한다.
```
aws eks --region REGION_CODE update-kubeconfig --name MY-CLUSTER-NAME
```

- 연결이 정상적으로 설정되면,
- Updated context arn:aws:eks:us-east-1:~~cluster/gdhong ~~~/.kube/config
- 메시지가 나타난다.

- 아래 커맨드 입력시, 응답이 조회되면 테스트가 성공한 것이다.
```
kubectl get all 
```
- service/kubernetes   ClusterIP   10.100.0.1   <none>        443/TCP   58m

	
## 주문서비스 쿠버네티스에 배포하기

### 이미지 생성 및 배포
- 주문 이미지 도커라이징
```
cd order
mvn package 
docker image build -t MY-DOCKER-ID/order:v0.1 .
docker login 
docker push 
docker push MY-DOCKER-ID/order:v0.1
```

- GitPod 터미널을 활용하여 주문 서비스 배포하기
```
kubectl create deploy order --name=MY-DOCKER-ID/order:v0.1
kubectl get all
```

### YAML 형식으로 배포해 보기
```
kubectl delete deploy order
cd kubernetes
# 19라인을 내 이미지 이름(MY-DOCKER-ID/order:v0.1) 으로 수정
kubectl apply -f deployment.yaml
kubectl get all
```