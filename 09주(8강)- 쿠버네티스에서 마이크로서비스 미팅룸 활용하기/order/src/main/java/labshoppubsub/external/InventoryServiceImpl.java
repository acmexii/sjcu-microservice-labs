package labshoppubsub.external;

import org.springframework.stereotype.Service;

//<<< Resilency / Fallback
@Service
public class InventoryServiceImpl implements InventoryService {


    /**
     * Fallback
     */
    public Inventory getInventory(Long id) {
        Inventory inventory = new Inventory();
        return inventory;
    }
}
//>>> Resilency / Fallback
