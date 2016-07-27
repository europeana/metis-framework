package eu.europeana.enrichment.migration;

import java.util.Map;

import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;

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
