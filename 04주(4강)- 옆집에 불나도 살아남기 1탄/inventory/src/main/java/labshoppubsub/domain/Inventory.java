package labshoppubsub.domain;

import labshoppubsub.InventoryApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name="Inventory_table")
@Data

public class Inventory  {

    
    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private Long stock;

    @PostPersist
    public void onPostPersist(){
    }

    // @PostLoad
    // public void makeDelay(){
    //     try {
    //         Thread.currentThread().sleep((long) (400 + Math.random() * 220));
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }

    // }

    public static InventoryRepository repository(){
        InventoryRepository inventoryRepository = InventoryApplication.applicationContext.getBean(InventoryRepository.class);
        return inventoryRepository;
    }

    public static void decreaseStock(OrderPlaced orderPlaced) {

        /** fill out following code  */
        
         repository().findById(Long.valueOf(orderPlaced.getProductId())).ifPresent(inventory->{
             inventory.setStock(inventory.getStock() - orderPlaced.getQty());
             repository().save(inventory);

         });

        
    }



}
