## 아래 사용자 시나리오에 포함된 도메인 이벤트들을 도출하시오

1. 고객 (Customer) 이 상품을 선택하여 주문한다 (Place an Order)
1. 주문이 되면 상품 배송을 한다.
1. 배송이 완료되면 상품의 재고량이 감소한다.

예시) OrderPlaced (pp형으로 도출, 목적어+동사pp)

## 확장 시나리오 - Saga Compensation
1. 고객이 주문을 취소할 수 있다 (Customer can cancel order)
1. 주문이 취소되면 배달이 취소된다 (Whenever customer cancel an order, cook or delivery is canceled too)
2. 배달이 수거되면 재고량이 증가한다