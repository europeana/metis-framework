package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class OaipmhHarvestPluginMetadata implements AbstractMetisPluginMetadata {
  private static final PluginType pluginType = PluginType.OAIPMH_HARVEST;
  private boolean mocked = true;
  private String url;
  private String metadataFormat;
  private String setSpec;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date fromDate;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date untilDate;
  private Map<String, List<String>> parameters;

  public OaipmhHarvestPluginMetadata() {
  }

  public OaipmhHarvestPluginMetadata(boolean mocked, String url, String metadataFormat,
      String setSpec, Map<String, List<String>> parameters) {
    this.mocked = mocked;
    this.url = url;
    this.metadataFormat = metadataFormat;
    this.setSpec = setSpec;
    this.parameters = parameters;
  }

  @Override
  public boolean isMocked() {
    return mocked;
  }

  public void setMocked(boolean mocked) {
    this.mocked = mocked;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMetadataFormat() {
    return metadataFormat;
  }

  public void setMetadataFormat(String metadataFormat) {
    this.metadataFormat = metadataFormat;
  }

  public String getSetSpec() {
    return setSpec;
  }

  public void setSetSpec(String setSpec) {
    this.setSpec = setSpec;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }

  public Date getUntilDate() {
    return untilDate;
  }

  public void setUntilDate(Date untilDate) {
    this.untilDate = untilDate;
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

}
