package labshoppubsub.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import labshoppubsub.OrderApplication;
import labshoppubsub.domain.OrderPlaced;
import labshoppubsub.external.Inventory;
import labshoppubsub.external.InventoryService;
import lombok.Data;

@Entity
@Table(name = "Order_table")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String productId;

    private Integer qty;

    private String customerId;

    private Double amount;

    @PostPersist
    public void onPostPersist() {
        OrderPlaced orderPlaced = new OrderPlaced(this);
        orderPlaced.publishAfterCommit();
    }

    @PrePersist
    public void onPrePersist() {
        // Get request from Inventory
        Inventory inventory =
           OrderApplication.applicationContext.getBean(InventoryService.class)
           .getInventory(Long.valueOf(getProductId()));

        if(inventory.getStock() < getQty()) throw new RuntimeException("Out of Stock!");

    }

    public static OrderRepository repository() {
        OrderRepository orderRepository = OrderApplication.applicationContext.getBean(
            OrderRepository.class
        );
        return orderRepository;
    }
}
