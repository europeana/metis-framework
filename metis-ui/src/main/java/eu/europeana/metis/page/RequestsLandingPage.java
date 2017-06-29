package eu.europeana.metis.page;

import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.Request;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RequestsLandingPage extends MetisLandingPage {

  private List<RoleRequest> roleRequests;

  public RequestsLandingPage(List<RoleRequest> roleRequests, MetisuiConfig metisuiConfig) {
    super(metisuiConfig);
    this.roleRequests = roleRequests;
  }

  @Override
  public void addPageContent() {
    buildRequestsPageContent();
  }

  private void buildRequestsPageContent() {
    if (roleRequests == null) {
      return;
    }
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsRequests(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);
    metisLandingPageModel.setRequest(processRequests());
  }

  private List<Request> processRequests() {
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
    List<Request> requests = new ArrayList<>();
    for (RoleRequest roleRequest : roleRequests) {
      requests.add(processRequest(format, roleRequest));
    }
    return requests;
  }

  private Request processRequest(SimpleDateFormat format, RoleRequest roleRequest) {
    Request request = new Request();
    request.setId(roleRequest.getId().toString());
    request.setEmail(roleRequest.getUserId());
    request.setOrganization(roleRequest.getOrganizationId());
    request.setUrl("/profile?userId=" + request.getId());
    request.setDate(format.format(roleRequest.getRequestDate()));

    if (roleRequest.isDeleteRequest()) {
      request.setType("Delete");
      request.setRequestType("user-request-type-delete");
    } else {
      request.setType("Add");
      request.setRequestType("user-request-type-add");
    }
    if (roleRequest.getRequestStatus() == null || roleRequest.getRequestStatus()
        .equalsIgnoreCase("Pending")) {
      request.setStatus("Pending");
      request.setStatusType("user-request-pending");
    } else if (roleRequest.getRequestStatus().equalsIgnoreCase("Accepted")) {
      request.setStatus("Accepted");
      request.setStatusType("user-request-accepted");
    } else if (roleRequest.getRequestStatus().equalsIgnoreCase("Rejected")) {
      request.setStatus("Rejected");
      request.setStatusType("user-request-rejected");
    }
   return request;
  }
}

