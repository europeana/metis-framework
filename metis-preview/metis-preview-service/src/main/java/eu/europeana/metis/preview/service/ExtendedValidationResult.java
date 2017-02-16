package eu.europeana.metis.preview.service;

import eu.europeana.validation.model.ValidationResultList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Extended class with preview URL for preview service
 * Created by ymamakis on 9/2/16.
 */
@XmlRootElement
public class ExtendedValidationResult extends ValidationResultList {
    private String portalUrl;
    private Date date;
    private List<String> records;
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

    @XmlElement
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @XmlElement
    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }
}
