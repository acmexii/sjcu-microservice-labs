package labshoppubsub.infra;
import labshoppubsub.domain.*;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.EntityModel;

@Component
public class OrderHateoasProcessor implements RepresentationModelProcessor<EntityModel<Order>>  {

    @Override
    public EntityModel<Order> process(EntityModel<Order> model) {

        
        return model;
    }
    
}
