package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EntityMergeEngine;

import java.util.Properties;

import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.web.EntityClientApiImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Instances of this object can set up {@link Enricher} instances. It has connection settings
 * that will apply only when needed (i.e. when no alternative {@link EntityResolver} is provided and
 * the default is used).</p>
 * <p> Users of this code can use the defaults, but also supply their own
 * implementation for the functionality represented by {@link RecordParser} and/or {@link
 * EntityResolver}. </p>
 */
public class EnricherProvider extends ConnectionProvider {

    private RecordParser recordParser;
    private EntityResolverCreator entityResolverCreator;
    private String entityManagementUrl;
    private String entityApiUrl;
    private String entityApiKey;

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
     * ClientEntityResolver} will be used with the connection settings in this class, and {@link
     * #setEnrichmentPropertiesValues(String, String, String)} will need to have been called.
     *
     * @param entityResolverCreator A creator for the entity resolver.
     */
    public void setEntityResolverCreator(EntityResolverCreator entityResolverCreator) {
        this.entityResolverCreator = entityResolverCreator;
    }

    /**
     * Set the properties values of the enrichment service. The default is null, in which case {@link
     * #setEntityResolverCreator(EntityResolverCreator)} will need to have been called.
     *
     * @param entityManagementUrl The url of the entity management service
     * @param entityApiUrl The url of the entity API service
     * @param entityApiKey The key for the entity service
     */
    public void setEnrichmentPropertiesValues(String entityManagementUrl, String entityApiUrl, String entityApiKey) {
        this.entityManagementUrl = entityManagementUrl;
        this.entityApiUrl = entityApiUrl;
        this.entityApiKey = entityApiKey;
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
        } else if (StringUtils.isNotBlank(entityManagementUrl) && StringUtils.isNotBlank(entityApiUrl)
                && StringUtils.isNotBlank(entityApiKey)) {

            final Properties properties = new Properties();
            properties.put("entity.management.url", entityManagementUrl);
            properties.put("entity.api.url", entityApiUrl);
            properties.put("entity.api.key", entityApiKey);
            entityResolver = new ClientEntityResolver(new EntityClientApiImpl(new EntityClientConfiguration(properties)),
                    batchSizeEnrichment);

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
