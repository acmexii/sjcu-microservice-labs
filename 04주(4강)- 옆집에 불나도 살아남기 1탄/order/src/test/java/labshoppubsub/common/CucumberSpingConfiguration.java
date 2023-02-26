package labshoppubsub.common;

import io.cucumber.spring.CucumberContextConfiguration;
import labshoppubsub.OrderApplication;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = { OrderApplication.class })
public class CucumberSpingConfiguration {}
