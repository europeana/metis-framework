package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.rest.client.AbstractConnectionProvider;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnricherProvider extends AbstractConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherProvider.class);

  private RecordParser recordParser;
  private EntityResolverCreator entityResolverCreator;
  private String enrichmentUrl;

  /**
   * Set the record parser to use. The default is null, in which case an instance of {@link
   * MetisRecordParser} will be used.
   *
   * @param recordParser The record parser to use.
   */
  public void setRecordParser(RecordParser recordParser) {
    this.recordParser = recordParser;
  }

  /**
   * Convenience method for {@link #setEntityResolverCreator(EntityResolverCreator)} that uses the
   * passed entity resolver instead of creating one on demand. Calling this method counts as calling
   * {@link #setEntityResolverCreator(EntityResolverCreator)} with a non-null creator.
   *
   * @param entityResolver The entity resolver to use.
   */
  public void setEntityResolver(EntityResolver entityResolver) {
    setEntityResolverCreator(() -> entityResolver);
  }

  /**
   * Set the entity resolver creator to use. The default is null, in which case a {@link
   * RemoteEntityResolver} will be used with the connection settings in this class, and {@link
   * #setEnrichmentUrl(String)} will need to have been called.
   *
   * @param entityResolverCreator A creator for the entity resolver.
   */
  public void setEntityResolverCreator(EntityResolverCreator entityResolverCreator) {
    this.entityResolverCreator = entityResolverCreator;
  }

  /**
   * Set the URL of the enrichment service. The default is null, in which case {@link
   * #setEntityResolverCreator(EntityResolverCreator)} will need to have been called.
   *
   * @param enrichmentUrl The URL of the enrichment service.
   */
  public void setEnrichmentUrl(String enrichmentUrl) {
    this.enrichmentUrl = enrichmentUrl;
  }

  /**
   * Builds an {@link Enricher} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   */
  public Enricher create() throws EnrichmentException {

    // Create the entity resolver
    final EntityResolver entityResolver;
    if (entityResolverCreator != null) {
      entityResolver = entityResolverCreator.createEntityResolver();
      if (entityResolver == null) {
        throw new EnrichmentException("Entity resolver creator returned a null object.", null);
      }
    } else if (StringUtils.isNotBlank(enrichmentUrl)) {
      try {
        entityResolver = new RemoteEntityResolver(new URL(enrichmentUrl), batchSizeEnrichment,
                createRestTemplate());
      } catch (MalformedURLException e) {
        LOGGER.debug("There was a problem creating entity resolver");
        throw new EnrichmentException("There was a problem while creating new Enricher", e);
      }
    } else {
      throw new EnrichmentException("We must have either a non-null entity resolver creator,"
              + " or a non-blank enrichment URL.", null);
    }

    // Done.
    return new EnricherImpl(recordParser == null ? new MetisRecordParser() : recordParser,
            entityResolver, new EntityMergeEngine());
  }

  /**
   * Implementations of this interface create instances of {@link EntityResolver}.
   */
  @FunctionalInterface
  public interface EntityResolverCreator {

    /**
     * Creates an instance of {@link EntityResolver}.
     *
     * @return An instance of {@link EntityResolver}.
     * @throws EnrichmentException In case there was a problem creating the instance.
     */
    EntityResolver createEntityResolver() throws EnrichmentException;
  }
}
