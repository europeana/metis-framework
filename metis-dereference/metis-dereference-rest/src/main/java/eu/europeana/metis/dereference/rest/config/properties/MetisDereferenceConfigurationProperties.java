package eu.europeana.metis.dereference.rest.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "metis-dereference")
public record MetisDereferenceConfigurationProperties(
    String allowedUrlDomains,
    String purgeAllFrequency,
    String purgeEmptyXmlFrequency,
    List<String> allowedCorsHosts
) {}
