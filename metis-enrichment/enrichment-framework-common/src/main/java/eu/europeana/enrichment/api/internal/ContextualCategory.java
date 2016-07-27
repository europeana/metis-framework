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

public enum ContextualCategory {
	
	AGENT("agent", "AgentImpl"),
	TIMESPAN("timespan", "TimespanImpl"),
	PLACE("place", "PlaceImpl"),
	CONCEPT("concept", "ConceptImpl");
	
	private String label;
	
	private String entityClass;

	private ContextualCategory(String label, String entityClass) {
		this.label = label;
		this.entityClass = entityClass;
	}

	public String getLabel() {
		return label;
	}

	public String getEntityClass() {
		return entityClass;
	}
	
	
}
