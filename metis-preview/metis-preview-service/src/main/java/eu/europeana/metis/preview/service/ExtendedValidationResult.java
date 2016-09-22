package eu.europeana.metis.preview.service;

import eu.europeana.validation.model.ValidationResultList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Extended class with preview URL for preview service
 * Created by ymamakis on 9/2/16.
 */
@XmlRootElement
public class ExtendedValidationResult extends ValidationResultList {
    private String portalUrl;

    /**
     * The portal url
     * @return The preview portal URL
     */
    @XmlElement
    public String getPortalUrl() {
        return portalUrl;
    }

    /**
     * Set the preview portal URL
     * @param portalUrl Set the preview portal URL
     */
    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }
}
