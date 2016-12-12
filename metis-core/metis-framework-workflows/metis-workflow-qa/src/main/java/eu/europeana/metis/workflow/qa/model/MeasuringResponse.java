package eu.europeana.metis.workflow.qa.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ymamakis on 11/22/16.
 */
@XmlRootElement
public class MeasuringResponse {
    private String sessionId;
    private String status;
    private String result;

    @XmlElement
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @XmlElement
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlElement
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
