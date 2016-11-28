package eu.europeana.metis.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * 
 * @author alena
 *
 */
public class MetisServletInitializer extends AbstractDispatcherServletInitializer {	
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		return context;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(MetisSecurityConfig.class, MetisWebMvcConfig.class, MetisLdapManagerConfig.class);
		return context;
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		registerProxyFilter(servletContext, "springSecurityFilterChain");
	}
	
	private void registerProxyFilter(ServletContext servletContext, String name) {
		DelegatingFilterProxy filter = new DelegatingFilterProxy(name);
		filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
		servletContext.addFilter(name, filter).addMappingForUrlPatterns(null, false, "/*");
	}
}