package eu.europeana.enrichment.migration;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * @author hgeorgiadis
 *
 */
public class AgentRepresentationMigration extends AbstractRepresentationMigration<AgentImpl> {

	public AgentRepresentationMigration(Map<String, String> lookupCodeUri, Map<String, String> lookupOriginalCodeUri) {
		super(lookupCodeUri, lookupOriginalCodeUri);
	}

	@Override
	public void migrateRepresentation(String codeURI, String originalCodeURI, AgentImpl representation) {

		representation.setAbout(codeURI);
		String[] sameAsURIs = representation.getOwlSameAs();
		sameAsURIs = ArrayUtils.addAll(sameAsURIs, originalCodeURI);
		representation.setOwlSameAs(sameAsURIs);
	}

}
