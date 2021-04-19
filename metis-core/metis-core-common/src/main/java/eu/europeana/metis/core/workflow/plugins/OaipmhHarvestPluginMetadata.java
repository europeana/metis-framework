package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.metis.utils.CommonStringValues;
import java.util.Date;

/**
 * OAIPMH Harvest Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class OaipmhHarvestPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType PLUGIN_TYPE = ExecutablePluginType.OAIPMH_HARVEST;
  private String url;
  private String metadataFormat;
  private String setSpec;
  private boolean incrementalHarvest; // Default: false (i.e. full harvest)
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date fromDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date untilDate;
  //Default false. If false, it indicates that the ProvidedCHO rdf:about should be used to set the identifier for ECloud
  private boolean useDefaultIdentifiers;
  //If useDefaultIdentifiers == true then this is the prefix to be trimmed from the OAI Header Identifier
  private String identifierPrefixRemoval;

  public OaipmhHarvestPluginMetadata() {
    //Required for json serialization
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

  public void setIncrementalHarvest(boolean incrementalHarvest) {
    this.incrementalHarvest = incrementalHarvest;
  }

  public boolean isIncrementalHarvest() {
    return incrementalHarvest;
  }

  public Date getFromDate() {
    return fromDate == null ? null : new Date(fromDate.getTime());
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate == null ? null : new Date(fromDate.getTime());
  }

  public boolean isUseDefaultIdentifiers() {
    return useDefaultIdentifiers;
  }

  public void setUseDefaultIdentifiers(boolean useDefaultIdentifiers) {
    this.useDefaultIdentifiers = useDefaultIdentifiers;
  }

  public String getIdentifierPrefixRemoval() {
    return identifierPrefixRemoval;
  }

  public void setIdentifierPrefixRemoval(String identifierPrefixRemoval) {
    this.identifierPrefixRemoval = identifierPrefixRemoval;
  }

  public Date getUntilDate() {
    return untilDate == null ? null : new Date(untilDate.getTime());
  }

  public void setUntilDate(Date untilDate) {
    this.untilDate = untilDate == null ? null : new Date(untilDate.getTime());
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return PLUGIN_TYPE;
  }

}
