package eu.europeana.metis.config;

import eu.europeana.metis.framework.rest.client.DsOrgRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * The configuration represents the communication of Metis with Europeana CMS (currently with Zoho).
 * It gives access to common Europeana data (like list of organizations - Europeana data partners).
 * @author alena
 *
 */
@Configuration
@PropertySource("classpath:/orchestrator.properties")
public class MetisOrchestratorConfig {
	@Value("${metis.orchestrator.url}")
	private String hostURL;
	@Value("${metis.orchestrator.apikey}")
	private String apikey;
	
	@Bean
	public DsOrgRestClient dsOrgRestClient() {
		return new DsOrgRestClient(hostURL, apikey);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
