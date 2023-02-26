package labshoppubsub.domain;

import labshoppubsub.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel="inventories", path="inventories")
public interface InventoryRepository extends PagingAndSortingRepository<Inventory, Long>{

}
//>>> PoEAA / Repository