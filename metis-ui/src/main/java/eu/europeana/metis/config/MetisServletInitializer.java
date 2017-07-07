package eu.europeana.metis.config;

import eu.europeana.metis.controller.MetisPageController;
import eu.europeana.metis.controller.MetisProfilePageController;
import eu.europeana.metis.controller.MetisUserPageController;
import eu.europeana.metis.page.HeaderSubMenuBuilder;
import eu.europeana.metis.page.MetisPageFactory;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
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
		return new AnnotationConfigWebApplicationContext();
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		//FIXME Maybe there is no need to have so many separate configurations and so many .properties files to store the links to the resources
		context.register(
				HeaderSubMenuBuilder.class,
				NavigationPaths.class,
				MetisPageFactory.class,
				MetisSecurityConfig.class,
				MetisWebMvcConfig.class,
				MetisLdapManagerConfig.class,
				MetisOrchestratorConfig.class,
				MetisConfig.class,
				MetisuiConfig.class,
				MetisMailConfig.class,
				MetisPageController.class,
				MetisUserPageController.class,
				MetisProfilePageController.class);
		return context;
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		registerProxyFilter(servletContext, "springSecurityFilterChain");
		registerCharacterEncodingFilter(servletContext);
	}
	
	private void registerCharacterEncodingFilter(ServletContext servletContext) {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		FilterRegistration.Dynamic characterEncoding = servletContext.addFilter("characterEncoding", characterEncodingFilter);
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);
		characterEncoding.addMappingForUrlPatterns(dispatcherTypes, true, "/*");		
	}
	
	private void registerProxyFilter(ServletContext servletContext, String name) {
		DelegatingFilterProxy filter = new DelegatingFilterProxy(name);
		filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
		servletContext.addFilter(name, filter).addMappingForUrlPatterns(null, false, "/*");
	}
}