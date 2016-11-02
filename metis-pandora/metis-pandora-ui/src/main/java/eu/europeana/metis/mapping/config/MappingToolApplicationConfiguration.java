package eu.europeana.metis.mapping.config;

import java.util.List;

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

import com.github.jknack.handlebars.Handlebars;
import com.github.mjeanroy.springmvc.view.mustache.MustacheCompiler;
import com.github.mjeanroy.springmvc.view.mustache.MustacheTemplateLoader;
import com.github.mjeanroy.springmvc.view.mustache.MustacheViewResolver;
import com.github.mjeanroy.springmvc.view.mustache.core.DefaultTemplateLoader;
import com.github.mjeanroy.springmvc.view.mustache.handlebars.HandlebarsCompiler;

import eu.europeana.metis.mapping.controller.MappingToolPageController;

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
	public Handlebars handlebars() {
		return new Handlebars();
	}
	
	@Bean 
	public MustacheTemplateLoader mustacheTemplateLoader(ResourceLoader resourceLoader){
		MustacheTemplateLoader loader =  new DefaultTemplateLoader(resourceLoader);
		loader.setPrefix("WEB-INF/app/views/");
		loader.setSuffix(".mustache");
		return loader;
	}	
	
	@Bean
	public MustacheCompiler mustacheCompiler(Handlebars handlebars,MustacheTemplateLoader loader){
		return new HandlebarsCompiler(handlebars,loader);
	}
	
	@Bean  
    public ViewResolver getViewResolver(ResourceLoader resourceLoader, MustacheCompiler mustacheCompiler) {
//		DefaultTemplateLoader loader = new DefaultTemplateLoader(resourceLoader);
//    	MustacheJavaCompiler compiler = new MustacheJavaCompiler(loader);
//		resolver.setOrder(1);
//		resolver.setPrefix("WEB-INF/app/views/");
//		resolver.setSuffix(".mustache");
		return new MustacheViewResolver(mustacheCompiler);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }
}
