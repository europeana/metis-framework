package eu.europeana.metis.framework.dataset;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import eu.europeana.metis.framework.common.HarvestingMetadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Created by ymamakis on 2/17/16.
 */
@XmlRootElement
public class Dataset {

    @Id
    @XmlElement
    private String id;

    @Indexed
    @XmlElement
    private String name;

    @XmlElement
    @Indexed
    private List<String> dataProviders;

    @XmlElement
    private boolean deaSigned;

    @XmlElement
    private List<String> subject;

    @XmlElement
    private List<String> source;

    @XmlElement
    private Date created;

    @XmlElement
    private Date updated;

    @XmlElement
    private String replacedBy;

    @XmlElement
    private String description;

    @XmlElement
    private String notes;

    @XmlElement
    @Indexed
    private String createdByLdapId;

    @XmlElement
    @Indexed
    private String assignedToLdapId;

    @XmlElement
    private Date firstPublished;

    @XmlElement
    private Date lastPublished;

    @XmlElement
    private int recordsPublished;

    @XmlElement
    private Date harvestedAt;

    @XmlElement
    private Date submittedAt;

    @XmlElement
    private int recordsSubmitted;

    @XmlElement
    private boolean accepted;

    @XmlElement
    private List<String> DQA;

    @XmlElement
    private HarvestingMetadata metadata;

    @XmlElement
    private WorkflowStatus workflowStatus;

    @XmlElement
    private Country country;

    @XmlElement
    private Language language;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDataProviders() {
        return dataProviders;
    }

    public void setDataProviders(List<String> dataProviders) {
        this.dataProviders = dataProviders;
    }

    public boolean isDeaSigned() {
        return deaSigned;
    }

    public void setDeaSigned(boolean deaSigned) {
        this.deaSigned = deaSigned;
    }

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
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

    public int getRecordsPublished() {
        return recordsPublished;
    }

    public void setRecordsPublished(int recordsPublished) {
        this.recordsPublished = recordsPublished;
    }

    public Date getHarvestedAt() {
        return harvestedAt;
    }

    public void setHarvestedAt(Date harvestedAt) {
        this.harvestedAt = harvestedAt;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getRecordsSubmitted() {
        return recordsSubmitted;
    }

    public void setRecordsSubmitted(int recordsSubmitted) {
        this.recordsSubmitted = recordsSubmitted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public List<String> getDQA() {
        return DQA;
    }

    public void setDQA(List<String> DQA) {
        this.DQA = DQA;
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
}
