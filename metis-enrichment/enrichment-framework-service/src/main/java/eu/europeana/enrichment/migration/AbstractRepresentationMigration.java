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
package eu.europeana.enrichment.migration;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;

import java.util.Map;

/**
 * @author hgeorgiadis
 *
 */
public abstract class AbstractRepresentationMigration<T extends AbstractEdmEntityImpl> {

	private Map<String, String> lookupCodeUri;

	private Map<String, String> lookupOriginalCodeUri;

	public AbstractRepresentationMigration(Map<String, String> lookupCodeUri,
			Map<String, String> lookupOriginalCodeUri) {
		super();
		this.lookupCodeUri = lookupCodeUri;
		this.lookupOriginalCodeUri = lookupOriginalCodeUri;
	}

	protected String lookupCodeUri(String codeUri) {
		return lookupCodeUri.get(codeUri);
	}

	protected String lookupOriginalCodeUri(String originalCodeUri) {
		return lookupOriginalCodeUri.get(originalCodeUri);
	}

	public abstract void migrateRepresentation(String codeURI, String originalCodeURI, T representation);

}
