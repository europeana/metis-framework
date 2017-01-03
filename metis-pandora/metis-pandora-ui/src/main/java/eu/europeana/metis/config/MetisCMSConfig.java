package eu.europeana.metis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import eu.europeana.metis.framework.rest.client.DsOrgRestClient;

/**
 * The configuration represents the communication of Metis with Europeana CMS (currently with Zoho).
 * It gives access to common Europeana data (like list of organizations - Europeana data partners).
 * @author alena
 *
 */
@Configuration
public class MetisCMSConfig {

	private String hostURL = "http://metis-framework-test.cfapps.io";
	
	@Bean
	public DsOrgRestClient dsOrgRestClient() {
		return new DsOrgRestClient(hostURL);
	}
}
