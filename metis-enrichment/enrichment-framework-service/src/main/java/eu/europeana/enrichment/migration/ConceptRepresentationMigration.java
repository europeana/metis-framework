package eu.europeana.enrichment.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import eu.europeana.corelib.solr.entity.ConceptImpl;

/**
 * @author hgeorgiadis
 *
 */
public class ConceptRepresentationMigration extends AbstractRepresentationMigration<ConceptImpl> {

	public ConceptRepresentationMigration(Map<String, String> lookupCodeUri,
			Map<String, String> lookupOriginalCodeUri) {
		super(lookupCodeUri, lookupOriginalCodeUri);
	}

	@Override
	public void migrateRepresentation(String codeURI, String originalCodeURI, ConceptImpl representation) {

		representation.setAbout(codeURI);
		List<String> broaderURIList = new ArrayList<String>();
		String[] broaderURIs = representation.getBroader();
		String[] broaderMatchURIs = representation.getBroadMatch();
		List<String> broaderMatchURIList = new ArrayList<String>();
		if (broaderMatchURIs != null) {
			CollectionUtils.addAll(broaderMatchURIList, broaderMatchURIs);
		}
		if (ArrayUtils.isNotEmpty(broaderURIs)) {
			for (String broaderURI : broaderURIs) {

				String lookupCodeUri = lookupOriginalCodeUri(broaderURI);
				if (lookupCodeUri != null) {
					broaderURIList.add(lookupCodeUri);
				} else {
					broaderMatchURIList.add(broaderURI);
				}
			}

			broaderURIs = broaderURIList.isEmpty() ? null : broaderURIList.toArray(new String[broaderURIList.size()]);
			representation.setBroader(broaderURIs);
			broaderMatchURIs = broaderMatchURIList.isEmpty() ? null
					: broaderMatchURIList.toArray(new String[broaderMatchURIList.size()]);
			representation.setBroadMatch(broaderMatchURIs);
		}

		List<String> narrowerURIList = new ArrayList<String>();
		String[] narrowerURIs = representation.getNarrower();
		String[] narrowerMatchURIs = representation.getNarrowMatch();
		List<String> narrowerMatchURIList = new ArrayList<String>();
		if (narrowerMatchURIs != null) {
			CollectionUtils.addAll(narrowerMatchURIList, narrowerMatchURIs);
		}
		if (ArrayUtils.isNotEmpty(narrowerURIs)) {
			for (String narrowerURI : narrowerURIs) {
				String lookupCodeUri = lookupOriginalCodeUri(narrowerURI);
				if (lookupCodeUri != null) {
					narrowerURIList.add(lookupCodeUri);
				} else {
					narrowerMatchURIList.add(narrowerURI);
				}
			}
			narrowerURIs = narrowerURIList.isEmpty() ? null
					: narrowerURIList.toArray(new String[narrowerURIList.size()]);
			representation.setNarrower(narrowerURIs);
			narrowerMatchURIs = narrowerMatchURIList.isEmpty() ? null
					: narrowerMatchURIList.toArray(new String[narrowerMatchURIList.size()]);
			representation.setNarrowMatch(narrowerMatchURIs);
		}

		String[] exactMatchUris = representation.getExactMatch();
		exactMatchUris = ArrayUtils.addAll(exactMatchUris, originalCodeURI);
		representation.setExactMatch(exactMatchUris);
	}

}
