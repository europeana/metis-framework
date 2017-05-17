/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.common.Language;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * The Dataset representation
 * Created by ymamakis on 2/17/16.
 */
@Entity
public class Dataset {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  /**
   * The name of the dataset
   */
  @Indexed(unique = true)
  private String datasetName;

  /**
   * Identical to the organizationId from Organization class
   */
  @Indexed
  private String organizationId;

  /**
   * Data providers associated with this dataset
   */
  @Indexed
  private String dataProvider;

  /**
   * Providers have signed DEA for the dataset
   */
  private boolean deaSigned;

  /**
   * List of subjects for the dataset
   */
  @JacksonXmlElementWrapper(localName = "subjects")
  @JacksonXmlProperty(localName = "subject")
  private List<String> subjects;

  /**
   * The source of the dataset
   */
  @JacksonXmlElementWrapper(localName = "sources")
  @JacksonXmlProperty(localName = "source")
  private List<String> sources;

  /**
   * When was the dataset created
   */
  private Date createdDate;

  /**
   * When was the dataset updated
   */
  private Date updatedDate;

  /**
   * What dataset replaces it (ID)
   */
  private String replacedBy;

  /**
   * Description of the dataset
   */
  private String description;

  /**
   * Notes for the dataset
   */
  private String notes;

  /**
   * User id that created the dataset
   */
  @Indexed
  private String createdByLdapId;

  /**
   * User id to process the dataset
   */
  @Indexed
  private String assignedToLdapId;

  /**
   * When it ws first published
   */
  private Date firstPublished;

  /**
   * When it was last published
   */
  private Date lastPublished;

  /**
   * How many records were published
   */
  private int publishedRecords;

  /**
   * When it was harvested
   */
  private Date harvestedAt;

  /**
   * When it was submitted
   */
  private Date submissionDate;

  /**
   * How many records were submitted
   */
  private int submittedRecords;

  /**
   * Has the provider accepted it
   */
  private boolean accepted;

  /**
   * Data Quality Assurance
   */
  @JacksonXmlElementWrapper(localName = "dqas")
  @JacksonXmlProperty(localName = "dqa")
  private List<String> dqas;

  /**
   * Harvesting metadata (override the organizational metadata)
   */
  private HarvestingMetadata metadata;

  /**
   * The workflow status
   */
  private WorkflowStatus workflowStatus;

  /**
   * The country of the dataset
   */
  private Country country;

  /**
   * The language of the dataset
   */
  private Language language;

  private Boolean acceptanceStep;

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
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

  public String getDataProvider() {
    return dataProvider;
  }

  public void setDataProvider(String dataProvider) {
    this.dataProvider = dataProvider;
  }

  public boolean isDeaSigned() {
    return deaSigned;
  }

  public void setDeaSigned(boolean deaSigned) {
    this.deaSigned = deaSigned;
  }

  public List<String> getSubjects() {
    return subjects;
  }

  public void setSubjects(List<String> subjects) {
    this.subjects = subjects;
  }

  public List<String> getSources() {
    return sources;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getReplacedBy() {
    return replacedBy;
  }

  public void setReplacedBy(String replacedBy) {
    this.replacedBy = replacedBy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getCreatedByLdapId() {
    return createdByLdapId;
  }

  public void setCreatedByLdapId(String createdByLdapId) {
    this.createdByLdapId = createdByLdapId;
  }

  public String getAssignedToLdapId() {
    return assignedToLdapId;
  }

  public void setAssignedToLdapId(String assignedToLdapId) {
    this.assignedToLdapId = assignedToLdapId;
  }

  public Date getFirstPublished() {
    return firstPublished;
  }

  public void setFirstPublished(Date firstPublished) {
    this.firstPublished = firstPublished;
  }

  public Date getLastPublished() {
    return lastPublished;
  }

  public void setLastPublished(Date lastPublished) {
    this.lastPublished = lastPublished;
  }

  public int getPublishedRecords() {
    return publishedRecords;
  }

  public void setPublishedRecords(int publishedRecords) {
    this.publishedRecords = publishedRecords;
  }

  public Date getHarvestedAt() {
    return harvestedAt;
  }

  public void setHarvestedAt(Date harvestedAt) {
    this.harvestedAt = harvestedAt;
  }

  public Date getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(Date submissionDate) {
    this.submissionDate = submissionDate;
  }

  public int getSubmittedRecords() {
    return submittedRecords;
  }

  public void setSubmittedRecords(int submittedRecords) {
    this.submittedRecords = submittedRecords;
  }

  public boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }

  public List<String> getDqas() {
    return dqas;
  }

  public void setDqas(List<String> dqas) {
    this.dqas = dqas;
  }

  public HarvestingMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(HarvestingMetadata metadata) {
    this.metadata = metadata;
  }

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
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


  public Boolean getAcceptanceStep() {
    return acceptanceStep;
  }

  public void setAcceptanceStep(Boolean acceptanceStep) {
    this.acceptanceStep = acceptanceStep;
  }
}
