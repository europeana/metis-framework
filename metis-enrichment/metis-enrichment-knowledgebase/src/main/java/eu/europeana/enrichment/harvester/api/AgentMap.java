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
package eu.europeana.enrichment.harvester.api;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Property;
import org.mongojack.Id;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@Entity
public class AgentMap {
	@Id
	private String id;
	private URI agentUri;
	@Property
	private String controlledSourceId;
	private Date storedDate;
	private Date harvestedDate;
	private ArrayList <String> sameAs;
	/**
	 * 
	 * @param id
	 * @param uri
	 * @param sourceId
	 * @param storedDate
	 * @param harvestedDate
	 */
	public  AgentMap(String id, URI uri, String sourceId, Date storedDate, Date harvestedDate){

		this.id = id;
		this.agentUri =uri;
		this.controlledSourceId = sourceId;
		this.storedDate = storedDate;
		this.harvestedDate = harvestedDate;
		this.sameAs=new ArrayList <String>();
	}
	
	public AgentMap(){
		
	}
	/**
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public URI getAgentUri() {
		return agentUri;
	}
	public void setAgentUri(URI agentUri) {
		this.agentUri = agentUri;
	}
	public String getControlledSourceId() {
		return controlledSourceId;
	}
	public void setControlledSourceId(String controlledSourceId) {
		this.controlledSourceId = controlledSourceId;
	}
	public Date getStoredDate() {
		return storedDate;
	}
	public void setStoredDate(Date storedDate) {
		this.storedDate = storedDate;
	}
	public Date getHarvestedDate() {
		return harvestedDate;
	}
	public void setHarvestedDate(Date harvestedDate) {
		this.harvestedDate = harvestedDate;
	}
	public List <String> getSameAs(){
		return sameAs;
	}
	
	public void addSameAs(String id){
		if (!sameAs.contains(id))
			this.sameAs.add(id);
	}
	
}
