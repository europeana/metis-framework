package eu.europeana.redirects.rest.config;

import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.rest.RedirectResource;
import eu.europeana.redirects.service.RedirectService;
import eu.europeana.redirects.service.mongo.MongoRedirectService;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey Configuration class
 * Created by ymamakis on 1/15/16.
 */
@ApplicationPath("/rest")
public class Application extends ResourceConfig {

    public Application(){
        this(new MongoRedirectService());
    }

    public Application(final RedirectService service){
        super();
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(service).to(RedirectService.class);
            }
        });
        register(RedirectResource.class);
        register(MultiPartFeature.class);
        register(RedirectRequest.class);
        register(RedirectRequestList.class);
        register(RedirectResponse.class);
        register(RedirectResponseList.class);
        register(io.swagger.jaxrs.listing.ApiListingResource.class);
        register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("metis-redirects-test");
        beanConfig.setBasePath("/rest");
        beanConfig.setResourcePackage("eu.europeana.redirects.rest");
        beanConfig.setScan(true);
    }
}
