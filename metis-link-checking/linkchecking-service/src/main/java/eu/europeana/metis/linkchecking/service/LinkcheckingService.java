package eu.europeana.metis.linkchecking.service;

import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 11/4/16.
 */
public class LinkcheckingService {


    public List<LinkcheckStatus> generateLinkCheckingReport(List<LinkcheckRequest> requests) {
        List<LinkcheckStatus> statuses = new ArrayList<>();

        for (LinkcheckRequest request : requests) {

            LinkcheckStatus status = new LinkcheckStatus();
            status.setEdmFieldName(request.getFieldName());
            for (String url : request.getUrls()) {
                try {
                    URL urlObj = new URL(url);
                    HttpURLConnection connection= (HttpURLConnection) urlObj.openConnection();
                    if (connection.getResponseCode() < 400) {
                        status.setSucceeded(status.getSucceeded() + 1);
                    } else {
                        status.setFailed(status.getFailed() + 1);
                    }
                } catch (IOException e) {
                    status.setFailed(status.getFailed() + 1);
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            statuses.add(status);
        }
        return statuses;
    }
}
