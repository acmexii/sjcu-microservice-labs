## 옆집에 불나도 살아남기


### 이벤트스토밍 모델

<img width="899" alt="image" src="https://user-images.githubusercontent.com/487999/190903135-a6bb95c0-d1f6-424e-9444-1bbf0119386a.png">


### 모델에 대한 마이크로서비스 코드 확인

- Github URL을 활용하여 GitPod로 접속한다.
- 04주(4강)-옆집에 불나도 살아남기 1탄 폴더로 이동한다.

- order/../Order.java 의 @PrePersist
```
    @PrePersist
    public void onPrePersist() {
        // Get request from Inventory
        Inventory inventory =
           Application.applicationContext.getBean(InventoryService.class)
           .getInventory(Long.valueOf(getProductId()));

        if(inventory.getStock() < getQty()) throw new RuntimeException("Out of Stock!");

    }
```
> 재고 서비스를 호출한 결과 얻은 재고량을 확인하여 재고가 주문량에 못 미치면 오류를 내도록 하는 검증 로직을 추가

- order/../external/InventoryService.java
```
@FeignClient(name = "inventory", url = "${api.url.inventory}")
public interface InventoryService {
    @RequestMapping(method = RequestMethod.GET, path = "/inventories/{id}")
    public Inventory getInventory(@PathVariable("id") Long id);

  ...
}
```
> 재고량을 얻기 위한 GET 호출의 FeignClient Interface 확인



### 서킷브레이커 설정전 주문해 보기 
- monolith(Order) 서비스와 inventory 서비스를 실행한다. 
```
cd order
mvn clean spring-boot:run

cd inventory
mvn clean spring-boot:run
```
- 충분한 재고량을 입력한다.
```
http :8082/inventories id=1 stock=10000
```
- 부하 툴을 사용하여 동시사용자 2명의 10초간의 주문을 넣어본다.
-> sudo apt install siege -y 을 통해 설치

```
siege -c2 -t10S  -v --content-type "application/json" 'http://localhost:8081/orders POST {"productId":1, "qty":1}'
```
		
> 모든 호출이  201 Code 로 성공함을 알 수 있다.


### 옆집에 불내기와 살아남기 위한 설정

- inventory 서비스의 Inventory.java 에 성능이 느려지도록 강제 딜레이를 발생시키는 코드를 추가한다.  


   ```java
    @PostLoad
    public void makeDelay(){
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

```

- monolith(Order) 서비스의 application.yaml 파일의 다음 설정을 true 로 하고, 임계치를 610ms로 바꾼다:  

```yaml
  
    feign:
      hystrix:
        enabled: true
    
    hystrix:
      command:
        # 전역설정
        default:
          execution.isolation.thread.timeoutInMilliseconds: 610
```



### 서킷브레이커 설정후 주문해 보기 

- monolith와 inventory 서비스를 모두 재시작한다. 
- 재시작 후, 인벤트로의 재고량을 충분히 설정한다:
```
http :8082/inventories id=1 stock=10000
```
- 다시 부하 툴을 사용하여 주문을 넣어본다.  
    ```
    siege -c2 -t20S  -v --content-type "application/json" 'http://localhost:8081/orders POST {"productId":1, "qty":1}'
    ```
> Delay 가 발생함에 따라 적당히 201 code 와 500 오류 코드가 반복되며 inventory 로 부하를 조절하면서 요청을 관리하는 것을 확인할 수 있다.
> 결과적으로 Availability 는 60~90% 수준이 유지되면서 서비스는 유지된다.

- monolith(Order) 서비스의 로그를 확인:
```
java.lang.RuntimeException: Hystrix circuit short-circuited and is OPEN

```
> 서킷 브레이커가 발동하여 오류가 발생한 것을 확인할 수 있다.


### fallback 처리 (장애시에 적당한 대체값)

- inventory 서비스가 중지된 상태로 주문을 넣어본다. ( 500 에러 )

```
http localhost:8081/orders productId=1 qty=1 
```

- order 서비스의 external/InventoryService.java 의 FeignClient에 fallback 옵션을 설정한다.
- 10라인 FeignClient 설정을 아래 코드로 Replace 한다.    
 ```
@FeignClient(name = "inventory", url = "${api.url.inventory}", fallback = InventoryServiceFallback.class)
 ```
 
- monolith(Order) 서비스에 Fallback 구현체를 추가한다:
- external 패키지 상에서 New File >  InventoryServiceFallback.java 파일을 생성하고 아래 샘플코드를 붙여넣는다.
```
package labshoppubsub.external;

import org.springframework.stereotype.Service;

@Service
public class InventoryServiceFallback implements InventoryService{
    public Inventory getInventory(Long id){
        Inventory fallbackValue = new Inventory();
        fallbackValue.setStock(1L);

        return fallbackValue;
    }
}
```

- monolith(Order) 서비스를 재실행 후 주문을 넣어본다. ( 주문 가능 )
    - 이때 inventory 서비스는 중지 상태 이어야 한다.  
    - InventoryServiceImpl 의 getInventory 메서드가 실행되어 적당한 가짜 값인 1이 리턴되어 재고량이 있는 것으로 리턴하게 하는 것을 확인할 수 있다. 

```
http localhost:8081/orders productId=1 qty=1   # will succeed!
```
