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
package eu.europeana.enrichment.gui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Enrichment wrapper data transformation object
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class EntityWrapperDTO implements IsSerializable {
	private String originalField;
	private String className;
	private String contextualEntity;
	/**
	 * The original field that generated the enrichment
	 * @return
	 */
	public String getOriginalField() {
		return originalField;
	}
	public void setOriginalField(String originalField) {
		this.originalField = originalField;
	}
	
	/**
	 * The class type of the enrichment
	 * @return
	 */
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * The actual value of the enrichment
	 * @return
	 */
	public String getContextualEntity() {
		return contextualEntity;
	}
	public void setContextualEntity(String contextualEntity) {
		this.contextualEntity = contextualEntity;
	}
}
