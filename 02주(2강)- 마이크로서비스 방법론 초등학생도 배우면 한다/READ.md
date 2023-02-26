## 초등생도 한다는 이벤트스토밍 해보기

실습은 브라우저에서 2개의 탭(실습가이드, 실습환경)으로 진행된다.

- 실습을 위해 브라우저의 새로운 탭에 아래 이벤트스토밍 교구로 접속해 둔다.  
- https://labs.msaez.io/#/



### 아래 사용자 시나리오를 충족하는 이벤트스토밍을 수행한다.

1. 고객 (Customer) 이 상품을 선택하여 주문한다 (Place an Order)
1. 주문이 되면 상품 배송을 한다.
1. 배송이 완료되면 상품의 재고량이 감소한다.

예시) OrderPlaced (pp형으로 도출, 목적어+동사pp)


### 추가 시나리오를 이벤트스토밍에 모델에 반영한다.

1. 고객이 주문을 취소할 수 있다 (Customer can cancel order)
1. 주문이 취소되면 배달이 취소된다 (Whenever customer cancel an order, cook or delivery is canceled too)
1. 배달이 수거되면 재고량이 증가한다


### 이벤트스토밍 결과 모델에 따른 마이크로서비스템플릿 코드를 확인한다.

- 교구의 우측 상단 메뉴 중, Code > Code Previw를 클릭한다.