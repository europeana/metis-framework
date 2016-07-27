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
package eu.europeana.enrichment.api.internal;


import org.mongojack.Id;


public class MongoSequence {

	@Id
	private String id;
	
	private Long nextConceptSequence = 1l;
	
	private Long nextAgentSequence = 1l;
	
	private Long nextPlaceSequence = 1l;
	
	private Long nextTimespanSequence = 1l;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getNextConceptSequence() {
		return nextConceptSequence;
	}

	public void setNextConceptSequence(Long nextConceptSequence) {
		this.nextConceptSequence = nextConceptSequence;
	}

	public Long getNextAgentSequence() {
		return nextAgentSequence;
	}

	public void setNextAgentSequence(Long nextAgentSequence) {
		this.nextAgentSequence = nextAgentSequence;
	}

	public Long getNextPlaceSequence() {
		return nextPlaceSequence;
	}

	public void setNextPlaceSequence(Long nextPlaceSequence) {
		this.nextPlaceSequence = nextPlaceSequence;
	}

	public Long getNextTimespanSequence() {
		return nextTimespanSequence;
	}

	public void setNextTimespanSequence(Long nextTimespanSequence) {
		this.nextTimespanSequence = nextTimespanSequence;
	}

	public Long getNextSequence(ContextualCategory contextualCategory) {
		
		switch (contextualCategory) {
		case AGENT:
			return nextAgentSequence;
		case CONCEPT:
			return nextConceptSequence;
		case TIMESPAN:
			return nextTimespanSequence;
		case PLACE:
			return nextPlaceSequence;
		default:
			throw new IllegalArgumentException("Not supported contextual entity");
		}
		
		
	}
	
	public void setNextSequence(Long nextSequence, ContextualCategory contextualCategory) {
		switch (contextualCategory) {
		case AGENT:
			nextAgentSequence = nextSequence;
			break;
		case CONCEPT:
			nextConceptSequence = nextSequence;
			break;
		case TIMESPAN:
			nextTimespanSequence = nextSequence;
			break;
		case PLACE:
			nextPlaceSequence = nextSequence;
			break;
		default:
			throw new IllegalArgumentException("Not supported contextual entity");
		}
	}
	
}
