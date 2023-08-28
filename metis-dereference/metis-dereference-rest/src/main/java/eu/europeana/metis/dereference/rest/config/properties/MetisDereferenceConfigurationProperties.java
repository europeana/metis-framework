package eu.europeana.metis.dereference.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "metis-dereference")
public class MetisDereferenceConfigurationProperties {

    private String allowedUrlDomains;
    private String purgeAllFrequency;
    private String purgeEmptyXmlFrequency;

    public String getAllowedUrlDomains() {
        return allowedUrlDomains;
    }

    public void setAllowedUrlDomains(String allowedUrlDomains) {
        this.allowedUrlDomains = allowedUrlDomains;
    }

    public String getPurgeAllFrequency() {
        return purgeAllFrequency;
    }

    public void setPurgeAllFrequency(String purgeAllFrequency) {
        this.purgeAllFrequency = purgeAllFrequency;
    }

    public String getPurgeEmptyXmlFrequency() {
        return purgeEmptyXmlFrequency;
    }

    public void setPurgeEmptyXmlFrequency(String purgeEmptyXmlFrequency) {
        this.purgeEmptyXmlFrequency = purgeEmptyXmlFrequency;
    }
}
