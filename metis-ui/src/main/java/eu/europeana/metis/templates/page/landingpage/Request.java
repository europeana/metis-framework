package eu.europeana.metis.templates.page.landingpage;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "email",
    "organization",
    "date",
    "type",
    "status",
    "status_type",
    "request_type",
    "url"
})
public class Request {

  @JsonProperty("id")
  private String id;
  @JsonProperty("email")
  private String email;
  @JsonProperty("organization")
  private String organization;
  @JsonProperty("date")
  private String date;
  @JsonProperty("type")
  private String type;
  @JsonProperty("status")
  private String status;
  @JsonProperty("status_type")
  private String statusType;
  @JsonProperty("request_type")
  private String requestType;
  @JsonProperty("url")
  private String url;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  @JsonProperty("organization")
  public String getOrganization() {
    return organization;
  }

  @JsonProperty("organization")
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  @JsonProperty("date")
  public String getDate() {
    return date;
  }

  @JsonProperty("date")
  public void setDate(String date) {
    this.date = date;
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  @JsonProperty("status_type")
  public String getStatusType() {
    return statusType;
  }

  @JsonProperty("status_type")
  public void setStatusType(String statusType) {
    this.statusType = statusType;
  }

  @JsonProperty("request_type")
  public String getRequestType() {
    return requestType;
  }

  @JsonProperty("request_type")
  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
