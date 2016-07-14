import eu.europeana.validation.Application;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by ymamakis on 7/14/16.
 */
@Configuration
@Import(Application.class)
public class TestApplication{

    @PostConstruct
    public void startup(){
        MongoProvider.start();
    }

    @PreDestroy
    public void shutdown(){
        MongoProvider.stop();
    }
}
