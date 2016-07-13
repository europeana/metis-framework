package eu.europeana.identifier.rest.config;

import eu.europeana.identifier.rest.IdentifierResource;
import eu.europeana.identifier.rest.ItemizationResource;
import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.rest.exceptions.IdentifierExceptionMapper;
import eu.europeana.itemization.IdentifierError;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by ymamakis on 2/9/16.
 */
@ApplicationPath("/rest")
public class Application extends ResourceConfig {
    public Application() {
        super();
        register(IdentifierResource.class);
        register(ItemizationResource.class);
        register(Request.class);
        register(RequestResult.class);
        register(MultiPartFeature.class);
        register(IdentifierException.class);
        register(IdentifierError.class);
        register(IdentifierExceptionMapper.class);
        register(io.swagger.jaxrs.listing.ApiListingResource.class);
        register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("metis-identifier-test.de.a9sapp.eu");
        beanConfig.setBasePath("/rest");
        beanConfig.setResourcePackage("eu.europeana.identifier.rest");
        beanConfig.setScan(true);
    }
}
