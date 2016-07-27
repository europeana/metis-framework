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

import eu.europeana.corelib.solr.entity.PlaceImpl;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hgeorgiadis
 *
 */
public class PlaceRepresentationMigration extends AbstractRepresentationMigration<PlaceImpl> {

	public PlaceRepresentationMigration(Map<String, String> lookupCodeUri, Map<String, String> lookupOriginalCodeUri) {
		super(lookupCodeUri, lookupOriginalCodeUri);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void migrateRepresentation(String codeURI, String originalCodeURI, PlaceImpl representation) {

		representation.setAbout(codeURI);
		Map<String, List<String>> isPartOfNew = new HashMap<String, List<String>>();
		Map<String, List<String>> isPartOf = representation.getIsPartOf();
		if (isPartOf != null && !isPartOf.isEmpty()) {
			for (String lang : isPartOf.keySet()) {
				List<String> isPartOrUrisNew = new ArrayList<String>();
				for (String isPartOfURI : isPartOf.get(lang)) {
					String lookupCodeUri = lookupOriginalCodeUri(isPartOfURI);

					if (lookupCodeUri != null) {

						isPartOrUrisNew.add(lookupCodeUri);
					}
				}
				if (!isPartOrUrisNew.isEmpty()) {
					isPartOfNew.put(lang, isPartOrUrisNew);
				}
			}
			representation.setIsPartOf(isPartOfNew.isEmpty() ? null : isPartOfNew);
		}

		Map<String, List<String>> hasPartNew = new HashMap<String, List<String>>();
		Map<String, List<String>> hasPart = representation.getDcTermsHasPart();
		if (hasPart != null && !hasPart.isEmpty()) {
			for (String lang : hasPart.keySet()) {
				List<String> isPartOrUrisNew = new ArrayList<String>();
				for (String hasPartURI : hasPart.get(lang)) {

					String lookupCodeUri = lookupOriginalCodeUri(hasPartURI);
					if (lookupCodeUri != null) {

						isPartOrUrisNew.add(lookupCodeUri);
					}
				}
				if (!isPartOrUrisNew.isEmpty()) {
					hasPartNew.put(lang, isPartOrUrisNew);
				}
			}
			representation.setDcTermsHasPart(hasPartNew.isEmpty() ? null : hasPartNew);
		}

		String[] sameAsURIs = representation.getOwlSameAs();
		sameAsURIs = ArrayUtils.addAll(sameAsURIs, originalCodeURI);
		representation.setOwlSameAs(sameAsURIs);
	}

}
