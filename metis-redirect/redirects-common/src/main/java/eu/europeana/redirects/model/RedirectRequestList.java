package eu.europeana.redirects.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Wrapper for batch redirect request
 * Created by ymamakis on 1/13/16.
 */
@XmlRootElement
public class RedirectRequestList {
    @XmlElement
    private List<RedirectRequest> requestList;

    public List<RedirectRequest> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<RedirectRequest> requestList) {
        this.requestList = requestList;
    }
}
