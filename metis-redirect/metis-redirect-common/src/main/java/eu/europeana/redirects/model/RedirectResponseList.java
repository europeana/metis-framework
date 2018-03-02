package eu.europeana.redirects.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Betch Redirect Responses
 * Created by ymamakis on 1/13/16.
 */
@XmlRootElement
public class RedirectResponseList {
    private List<RedirectResponse> responseList;
    @XmlElement
    public List<RedirectResponse> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<RedirectResponse> responseList) {
        this.responseList = responseList;
    }
}
