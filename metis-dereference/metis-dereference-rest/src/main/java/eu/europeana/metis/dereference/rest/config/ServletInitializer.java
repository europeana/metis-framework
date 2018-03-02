package eu.europeana.metis.dereference.rest.config;

import eu.europeana.metis.dereference.service.MongoDereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferencingManagementService;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet initialization
 * Created by gmamakis on 12-2-16.
 */
public class ServletInitializer extends AbstractDispatcherServletInitializer {
    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.scan(ClassUtils.getPackageName(getClass()));
        context.register(MongoDereferenceService.class);
        context.register(MongoDereferencingManagementService.class);
        context.register(RdfRetriever.class);

        return context;
    }

    @Override
    protected String[] getServletMappings() {
        return  new String[]{"/"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return null;
    }
}
