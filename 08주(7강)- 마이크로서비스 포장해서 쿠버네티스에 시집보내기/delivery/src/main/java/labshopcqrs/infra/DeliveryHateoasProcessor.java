package labshopcqrs.infra;
import labshopcqrs.domain.*;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.EntityModel;

@Component
public class DeliveryHateoasProcessor implements RepresentationModelProcessor<EntityModel<Delivery>>  {

    @Override
    public EntityModel<Delivery> process(EntityModel<Delivery> model) {

        
        return model;
    }
    
}
