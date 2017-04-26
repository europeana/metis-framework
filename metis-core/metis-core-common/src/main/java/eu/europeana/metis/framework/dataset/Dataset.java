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

package eu.europeana.metis.framework.dataset;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.common.Language;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * The Dataset representation
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
@Entity
public class Dataset {


    @Id
    private ObjectId id;
    /**
     * The name of the dataset
     */
    @Indexed (unique = true)
    private String name;

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
    private List<String> subject;

    /**
     * The source of the dataset
     */
    private List<String> source;

    /**
     * When was the dataset created
     */
    private Date created;

    /**
     * When was the dataset updated
     */
    private Date updated;

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
    private int recordsPublished;

    /**
     * When it was harvested
     */
    private Date harvestedAt;

    /**
     * When it was submitted
     */
    private Date submittedAt;

    /**
     * How many records were submitted
     */
    private int recordsSubmitted;

    /**
     * Has the provider accepted it
     */
    private boolean accepted;

    /**
     * Data Quality Assurance
     */
    private List<String> DQA;

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

    @XmlElement
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }
    @XmlElement
    public boolean isDeaSigned() {
        return deaSigned;
    }

    public void setDeaSigned(boolean deaSigned) {
        this.deaSigned = deaSigned;
    }
    @XmlElement
    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }
    @XmlElement
    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }
    @XmlElement
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    @XmlElement
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
    @XmlElement
    public String getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(String replacedBy) {
        this.replacedBy = replacedBy;
    }
    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @XmlElement
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    @XmlElement
    public String getCreatedByLdapId() {
        return createdByLdapId;
    }

    public void setCreatedByLdapId(String createdByLdapId) {
        this.createdByLdapId = createdByLdapId;
    }
    @XmlElement
    public String getAssignedToLdapId() {
        return assignedToLdapId;
    }

    public void setAssignedToLdapId(String assignedToLdapId) {
        this.assignedToLdapId = assignedToLdapId;
    }
    @XmlElement
    public Date getFirstPublished() {
        return firstPublished;
    }

    public void setFirstPublished(Date firstPublished) {
        this.firstPublished = firstPublished;
    }
    @XmlElement
    public Date getLastPublished() {
        return lastPublished;
    }

    public void setLastPublished(Date lastPublished) {
        this.lastPublished = lastPublished;
    }
    @XmlElement
    public int getRecordsPublished() {
        return recordsPublished;
    }

    public void setRecordsPublished(int recordsPublished) {
        this.recordsPublished = recordsPublished;
    }
    @XmlElement
    public Date getHarvestedAt() {
        return harvestedAt;
    }

    public void setHarvestedAt(Date harvestedAt) {
        this.harvestedAt = harvestedAt;
    }
    @XmlElement
    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }
    @XmlElement
    public int getRecordsSubmitted() {
        return recordsSubmitted;
    }

    public void setRecordsSubmitted(int recordsSubmitted) {
        this.recordsSubmitted = recordsSubmitted;
    }
    @XmlElement
    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    @XmlElement
    public List<String> getDQA() {
        return DQA;
    }

    public void setDQA(List<String> DQA) {
        this.DQA = DQA;
    }
    @XmlElement
    public HarvestingMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(HarvestingMetadata metadata) {
        this.metadata = metadata;
    }
    @XmlElement
    public WorkflowStatus getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkflowStatus workflowStatus) {
        this.workflowStatus = workflowStatus;
    }
    @XmlElement
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
    @XmlElement
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @XmlElement
    public Boolean getAcceptanceStep() {
        return acceptanceStep;
    }

    public void setAcceptanceStep(Boolean acceptanceStep) {
        this.acceptanceStep = acceptanceStep;
    }
}
