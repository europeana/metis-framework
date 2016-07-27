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
package eu.europeana.metis.ui.wrapper;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import eu.europeana.metis.framework.common.FolderMetadata;
import eu.europeana.metis.framework.common.FtpMetadata;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.common.HttpMetadata;
import eu.europeana.metis.framework.common.OAIMetadata;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.FtpDatasetMetadata;
import eu.europeana.metis.framework.dataset.HttpDatasetMetadata;
import eu.europeana.metis.framework.common.Language;
import eu.europeana.metis.framework.dataset.OAIDatasetMetadata;
import eu.europeana.metis.framework.dataset.WorkflowStatus;

public class DatasetWrapper {
	
	private Dataset dataset;

	public DatasetWrapper(Dataset dataset) {
		this.dataset = dataset;
	}

	public DatasetWrapper() {
		this.dataset = new Dataset();
	}

	public Dataset getDataset() {
		return dataset;
	}

	public ObjectId getId() {
		return dataset.getId();
	}

	public void setId(ObjectId id) {
		dataset.setId(id);
	}

	public String getName() {
		return dataset.getName();
	}

	public void setName(String name) {
		dataset.setName(name);
	}

	public String getDataProvider() {
		return dataset.getDataProvider();
	}

	public void setDataProvider(String dataProvider) {
		dataset.setDataProvider(dataProvider);
	}

	public boolean isDeaSigned() {
		return dataset.isDeaSigned();
	}

	public void setDeaSigned(boolean deaSigned) {
		dataset.setDeaSigned(deaSigned);
	}

	public List<String> getSubject() {
		return dataset.getSubject();
	}

	public void setSubject(List<String> subject) {
		dataset.setSubject(subject);
	}

	public List<String> getSource() {
		return dataset.getSource();
	}

	public void setSource(List<String> source) {
		dataset.setSource(source);
	}

	public Date getCreated() {
//		if (dataset.getCreated()==null)
//			return null;
//		
//		return df.format(dataset.getCreated());
		return dataset.getCreated();
	}

	public void setCreated(Date created) {
//		try {
//			if (created != null)
//				dataset.setCreated(df.parse(created));
//		} catch (ParseException e) {
//		}
		dataset.setCreated(created);
	}

	public Date getUpdated() {
		return dataset.getUpdated();
	}

	public void setUpdated(Date updated) {
		dataset.setUpdated(updated);
	}

	public String getReplacedBy() {
		return dataset.getReplacedBy();
	}

	public void setReplacedBy(String replacedBy) {
		dataset.setReplacedBy(replacedBy);
	}

	public String getDescription() {
		return dataset.getDescription();
	}

	public void setDescription(String description) {
		dataset.setDescription(description);
	}

	public String getNotes() {
		return dataset.getNotes();
	}

	public void setNotes(String notes) {
		dataset.setNotes(notes);
	}

	public String getCreatedByLdapId() {
		return dataset.getCreatedByLdapId();
	}

	public void setCreatedByLdapId(String createdByLdapId) {
		dataset.setCreatedByLdapId(createdByLdapId);
	}

	public String getAssignedToLdapId() {
		return dataset.getAssignedToLdapId();
	}

	public void setAssignedToLdapId(String assignedToLdapId) {
		dataset.setAssignedToLdapId(assignedToLdapId);
	}

	public Date getFirstPublished() {
		return dataset.getFirstPublished();
	}

	public void setFirstPublished(Date firstPublished) {
		dataset.setFirstPublished(firstPublished);
	}

	public Date getLastPublished() {
		return dataset.getLastPublished();
	}

	public void setLastPublished(Date lastPublished) {
		dataset.setLastPublished(lastPublished);
	}

	public int getRecordsPublished() {
		return dataset.getRecordsPublished();
	}

	public void setRecordsPublished(int recordsPublished) {
		dataset.setRecordsPublished(recordsPublished);
	}

	public Date getHarvestedAt() {
		return dataset.getHarvestedAt();
	}

	public void setHarvestedAt(Date harvestedAt) {
		dataset.setHarvestedAt(harvestedAt);
	}

	public Date getSubmittedAt() {
		return dataset.getSubmittedAt();
	}

	public void setSubmittedAt(Date submittedAt) {
		dataset.setSubmittedAt(submittedAt);
	}

	public int getRecordsSubmitted() {
		return dataset.getRecordsSubmitted();
	}

	public void setRecordsSubmitted(int recordsSubmitted) {
		dataset.setRecordsSubmitted(recordsSubmitted);
	}

	public boolean isAccepted() {
		return dataset.isAccepted();
	}

	public void setAccepted(boolean accepted) {
		dataset.setAccepted(accepted);
	}

	public List<String> getDQA() {
		return dataset.getDQA();
	}

	public void setDQA(List<String> DQA) {
		dataset.setDQA(DQA);
	}

	public HarvestingMetadata getMetadata() {
		return dataset.getMetadata();
	}

	public void setMetadata(HarvestingMetadata metadata) {
		dataset.setMetadata(metadata);
	}

	public WorkflowStatus getWorkflowStatus() {
		return dataset.getWorkflowStatus();
	}

	public void setWorkflowStatus(WorkflowStatus workflowStatus) {
		dataset.setWorkflowStatus(workflowStatus);
	}

	public Country getCountry() {
		return dataset.getCountry();
	}

	public void setCountry(Country country) {
		dataset.setCountry(country);
	}

	public Language getLanguage() {
		return dataset.getLanguage();
	}

	public void setLanguage(Language language) {
		dataset.setLanguage(language);
	}

	/**
	 * Folder metadata
	 */
	public String getRecordXPath() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FolderMetadata) {
			return ((FolderMetadata) metadata).getRecordXPath();
		}
		return null;
	}

	/**
	 * Folder metadata
	 * 
	 * @param recordXPath
	 */
	public void setRecordXPath(String recordXPath) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FolderMetadata) {
			((FolderMetadata) metadata).setRecordXPath(recordXPath);
		}
	}

	/**
	 * OAI metadata
	 */
	public String getHarvestUrl() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIMetadata) {
			return ((OAIMetadata) metadata).getHarvestUrl();
		}
		return null;
	}

	/**
	 * OAI metadata
	 * 
	 * @param harvestUrl
	 */
	public void setHarvestUrl(String harvestUrl) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIMetadata) {
			((OAIMetadata) metadata).setHarvestUrl(harvestUrl);
		}
	}

	/**
	 * OAI metadata
	 */
	public String getMetadataFormat() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIMetadata) {
			return ((OAIMetadata) metadata).getMetadataFormat();
		}
		return null;
	}

	/**
	 * OAI metadata
	 * 
	 * @param metadataFormat
	 */
	public void setMetadataFormat(String metadataFormat) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIMetadata) {
			((OAIMetadata) metadata).setMetadataFormat(metadataFormat);
		}
	}

	/**
	 * OAI metadata
	 */
	public String getSetSpec() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIDatasetMetadata) {
			return ((OAIDatasetMetadata) metadata).getSetSpec();
		}
		return null;
	}

	/**
	 * OAI metadata
	 * 
	 * @param setSpec
	 */
	public void setSetSpec(String setSpec) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof OAIDatasetMetadata) {
			((OAIDatasetMetadata) metadata).setSetSpec(setSpec);
		}
	}

	/**
	 * HTTP + FTP metadata
	 */
	public String getHarvestUser() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpMetadata) {
			return ((HttpMetadata) metadata).getHarvestUser();
		}
		return null;
	}

	/**
	 * HTTP + FTP metadata
	 * 
	 * @param harvestUser
	 */
	public void setHarvestUser(String harvestUser) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpMetadata) {
			((HttpMetadata) metadata).setHarvestUser(harvestUser);
		}
	}

	/**
	 * HTTP + FTP base metadata
	 */
	public String getHarvestPassword() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpMetadata) {
			return ((HttpMetadata) metadata).getHarvestPassword();
		}
		return null;
	}

	/**
	 * HTTP + FTP base metadata
	 * 
	 * @param harvestPassword
	 */
	public void setHarvestPassword(String harvestPassword) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpMetadata) {
			((HttpMetadata) metadata).setHarvestPassword(harvestPassword);
		}
	}

	/**
	 * HTTP metadata
	 */
	public String getHttpUrl() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpDatasetMetadata) {
			return ((HttpDatasetMetadata) metadata).getHttpUrl();
		}
		return null;
	}

	/**
	 * HTTP metadata
	 * 
	 * @param httpUrl
	 */
	public void setHttpUrl(String httpUrl) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof HttpDatasetMetadata) {
			((HttpDatasetMetadata) metadata).setHttpUrl(httpUrl);
		}
	}

	/**
	 * FTP metadata
	 */
	public String getFtpServerAddress() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FtpMetadata) {
			return ((FtpMetadata) metadata).getFtpServerAddress();
		}
		return null;
	}

	/**
	 * FTP metadata
	 * 
	 * @param httpUrl
	 */
	public void setFtpServerAddress(String ftpServerAddress) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FtpMetadata) {
			((FtpMetadata) metadata).setFtpServerAddress(ftpServerAddress);
		}
	}

	/**
	 * FTP metadata
	 */
	public String getFtpUrl() {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FtpDatasetMetadata) {
			return ((FtpDatasetMetadata) metadata).getFtpUrl();
		}
		return null;
	}

	/**
	 * FTP metadata
	 * 
	 * @param httpUrl
	 */
	public void setFtpUrl(String ftpUrl) {
		HarvestingMetadata metadata = dataset.getMetadata();
		if (metadata instanceof FtpDatasetMetadata) {
			((FtpDatasetMetadata) metadata).setFtpUrl(ftpUrl);
		}
	}
}
