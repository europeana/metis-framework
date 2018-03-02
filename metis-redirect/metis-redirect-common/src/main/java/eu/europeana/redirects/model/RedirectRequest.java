package eu.europeana.redirects.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * A redirect request
 * Created by ymamakis on 1/13/16.
 */
@XmlRootElement
public class RedirectRequest {

    private String europeanaId;
    private String fieldName;
    private String fieldValue;
    private Map<String,String> parameters;


    private String collection;

    /**
     * The execution parameters for the redirects
     * @return
     */
    @XmlElement
    public Map<String, String> getParameters() {
        return parameters;
    }


    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * The collection to limit search for (in case of modified dataset identifier)
     * @return
     */
    @XmlElement
    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * The field name to search for
     * @return
     */
    @XmlElement
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * The field value to search for
     * @return
     */
    @XmlElement
    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    /**
     * The Europeana Identifier to generate the redirect
     * @return
     */
    @XmlElement
    public String getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(String europeanaId) {
        this.europeanaId = europeanaId;
    }
}
