package eu.europeana.metis.mapping.config;

import com.github.mjeanroy.springmvc.view.mustache.MustacheViewResolver;
import com.github.mjeanroy.springmvc.view.mustache.core.DefaultTemplateLoader;
import com.github.mjeanroy.springmvc.view.mustache.mustachejava.MustacheJavaCompiler;
import eu.europeana.metis.mapping.controller.MappingToolPageController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * 
 * @author alena
 *
 */
@Configuration
@EnableWebMvc
public class MappingToolApplicationConfiguration extends WebMvcConfigurerAdapter {


	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();	
	}

	@Bean
	public MappingToolPageController pandoraPageController(){
		MappingToolPageController controller = new MappingToolPageController();
		return controller;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new BufferedImageHttpMessageConverter());
	}
	
    @Bean
    public ViewResolver getViewResolver(ResourceLoader resourceLoader) {
    	DefaultTemplateLoader loader = new DefaultTemplateLoader(resourceLoader);
    	MustacheJavaCompiler compiler = new MustacheJavaCompiler(loader);
		MustacheViewResolver resolver = new MustacheViewResolver(compiler);
		resolver.setOrder(1);
		resolver.setPrefix("WEB-INF/app/views/");
		resolver.setSuffix(".mustache");
		return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }
}
