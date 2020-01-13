package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.CountryDeserializer;
import eu.europeana.metis.core.common.CountrySerializer;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.common.LanguageDeserializer;
import eu.europeana.metis.core.common.LanguageSerializer;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Dataset model that contains all the required fields for Dataset functionality.
 */
@Entity
@Indexes(@Index(fields = {@Field("organizationId"),
    @Field("datasetName")}, options = @IndexOptions(unique = true)))
public class Dataset implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  @Indexed(options = @IndexOptions(unique = true))
  private String ecloudDatasetId;

  @Indexed
  private String datasetId;

  @Indexed
  private String datasetName;

  @Indexed
  private String organizationId;

  @Indexed
  private String organizationName;

  @Indexed
  private String provider;

  @Indexed
  private String intermediateProvider;

  @Indexed
  private String dataProvider;

  @Indexed
  private String createdByUserId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date createdDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;

  private List<String> oldDatasetIds;

  private String replacedBy;

  private String replaces;

  @JsonSerialize(using = CountrySerializer.class)
  @JsonDeserialize(using = CountryDeserializer.class)
  private Country country;

  @JsonSerialize(using = LanguageSerializer.class)
  @JsonDeserialize(using = LanguageDeserializer.class)
  private Language language;

  private String description;

  private Boolean unfitForPublication;

  private String notes;

  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId xsltId;

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public void setEcloudDatasetId(String ecloudDatasetId) {
    this.ecloudDatasetId = ecloudDatasetId;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getIntermediateProvider() {
    return intermediateProvider;
  }

  public void setIntermediateProvider(String intermediateProvider) {
    this.intermediateProvider = intermediateProvider;
  }

  public String getDataProvider() {
    return dataProvider;
  }

  public void setDataProvider(String dataProvider) {
    this.dataProvider = dataProvider;
  }

  public String getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(String createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = new Date(createdDate.getTime());
  }

  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public List<String> getOldDatasetIds() {
    return this.oldDatasetIds == null ? null : new ArrayList<>(oldDatasetIds);
  }

  public void setOldDatasetIds(List<String> oldDatasetIds) {
    this.oldDatasetIds = oldDatasetIds == null ? null : new ArrayList<>(oldDatasetIds);
  }

  public String getReplacedBy() {
    return replacedBy;
  }

  public void setReplacedBy(String replacedBy) {
    this.replacedBy = replacedBy;
  }

  public String getReplaces() {
    return replaces;
  }

  public void setReplaces(String replaces) {
    this.replaces = replaces;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getUnfitForPublication() {
    return unfitForPublication;
  }

  public void setUnfitForPublication(Boolean unfitForPublication) {
    this.unfitForPublication = unfitForPublication;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public ObjectId getXsltId() {
    return xsltId;
  }

  public void setXsltId(ObjectId xsltId) {
    this.xsltId = xsltId;
  }
}
