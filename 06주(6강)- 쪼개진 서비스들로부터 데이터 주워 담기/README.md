## 쪼개진 서비스들로부터 데이터 주워 담기


### 이벤트스토밍 모델

![image](https://user-images.githubusercontent.com/35618409/222625299-13416d48-ef82-4fb8-93e9-f56cc1a204d1.png)

- 모델상의 고객센터(customercenter) 바운디드 컨텍스트의 MyPage CQRS모델을 확인한다.

- 설계모델 확인
```
https://labs.msaez.io/#/storming/382a1a59e6ca5d3f0ad100b556df797a
```

### 모델에 대한 마이크로서비스 코드 확인

- Github URL을 활용하여 GitPod로 접속한다.
- 06주(6강)- 쪼개진 서비스들로부터 데이터 주워 담기 폴더로 이동한다.


### 배송서비스 Domain Code 확인
- 주문 및 주문취소에 따른 배송 마이크로서비스의 비즈니스 코드를 완성한다.
- Delivery.java > addToDeliveryList Port method
```
    public static void addToDeliveryList(OrderPlaced orderPlaced){
        Delivery delivery = new Delivery();
        delivery.setAddress(orderPlaced.getAddress());
        delivery.setQuantity(orderPlaced.getQty());
        delivery.setCustomerId(orderPlaced.getCustomerId());
        delivery.setStatus("DeliveryStarted");
        repository().save(delivery);
    }
```

- Delivery.java > cancelDelivery Port method
```
    public static void cancelDelivery(OrderCancelled orderCancelled){
       
        repository().findByOrderId(orderCancelled.getId()).ifPresent(delivery ->{
            delivery.setStatus("DeliveryCancelled");
            repository().save(delivery);
         });
        
    }
```


### 마이크로서비스 실행
- 주문, 배송, 고객센터 마이크로서비스를 각각 실행한다.
```
mvn spring-boot:run
```


#### customerCenter CQRS Code 확인
```

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPlaced_then_CREATE_1 (@Payload OrderPlaced orderPlaced) {
        try {

            if (!orderPlaced.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setOrderId(orderPlaced.getId());
            myPage.setProductId(orderPlaced.getProductId());
            myPage.setOrderStatus(orderPlaced.getStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_1(@Payload DeliveryStarted deliveryStarted) {
        try {
            if (!deliveryStarted.validate()) return;
                // view 객체 조회

                List<MyPage> myPageList = myPageRepository.findByOrderId(deliveryStarted.getOrderId());
                for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setDeliveryStatus(deliveryStarted.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCancelled_then_UPDATE_2(@Payload DeliveryCancelled deliveryCancelled) {
        try {
            if (!deliveryCancelled.validate()) return;
                // view 객체 조회

                List<MyPage> myPageList = myPageRepository.findByOrderId(deliveryCancelled.getOrderId());
                for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setDeliveryStatus(deliveryCancelled.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

```

### 주문 생성 및 CQRS 확인

- 주문 1건을 등록한 후, MyPage 의 내용을 확인한다.
```
http POST :8081/orders productId=1 qty=1
http :8084/myPages
```

- 주문을 취소한 후, MyPage 의 내용을 확인한다.
```
http DELETE :8081/orders/1
http :8084/myPages
```

- 배송서비스(8083)를 다운시킨 다음, MyPage 의 내용을 확인하여도 서비스가 안정적임을 확인한다. 