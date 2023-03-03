package labshopcqrs.domain;

import labshopcqrs.domain.DeliveryStarted;
import labshopcqrs.domain.DeliveryCancelled;
import labshopcqrs.DeliveryApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;


@Entity
@Table(name="Delivery_table")
@Data

public class Delivery  {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String address;
    private String customerId;
    private Integer quantity;
    private Long orderId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        DeliveryStarted deliveryStarted = new DeliveryStarted(this);
        deliveryStarted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        DeliveryCancelled deliveryCancelled = new DeliveryCancelled(this);
        deliveryCancelled.publishAfterCommit();
    }

    public static DeliveryRepository repository(){
        DeliveryRepository deliveryRepository = DeliveryApplication.applicationContext.getBean(DeliveryRepository.class);
        return deliveryRepository;
    }

    public static void addToDeliveryList(OrderPlaced orderPlaced){
        Delivery delivery = new Delivery();
        delivery.setAddress(orderPlaced.getAddress());
        delivery.setQuantity(orderPlaced.getQty());
        delivery.setCustomerId(orderPlaced.getCustomerId());
        delivery.setStatus("DeliveryStarted");
        repository().save(delivery);
    }

    public static void cancelDelivery(OrderCancelled orderCancelled){
       
        repository().findByOrderId(orderCancelled.getId()).ifPresent(delivery ->{
            delivery.setStatus("DeliveryCancelled");
            repository().save(delivery);
         });
        
    }


}
