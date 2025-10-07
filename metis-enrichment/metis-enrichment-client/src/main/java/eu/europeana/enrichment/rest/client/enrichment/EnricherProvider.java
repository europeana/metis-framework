package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolverFactory;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import java.util.Properties;
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
    private String entityManagementUrl;
    private String entityApiUrl;
    private String entityApiTokenEndpoint;
    private String entityApiGrantParams;

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
     * Set the properties values of the enrichment service.
     *
     * @param entityManagementUrl The url of the entity management service
     * @param entityApiUrl The url of the entity API service
     * @param entityApiTokenEndpoint the entity api token endpoint
     * @param entityApiGrantParams the entity api grant params
     */
    public void setEnrichmentPropertiesValues(String entityManagementUrl,
        String entityApiUrl,
        String entityApiTokenEndpoint,
        String entityApiGrantParams) {
        this.entityManagementUrl = entityManagementUrl;
        this.entityApiUrl = entityApiUrl;
        this.entityApiTokenEndpoint = entityApiTokenEndpoint;
        this.entityApiGrantParams = entityApiGrantParams;
    }

    /**
     * Builds an {@link Enricher} according to the parameters that are set.
     *
     * @return An instance.
     * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
     * @throws EnrichmentException if there was an error during initialization
     */
    public Enricher create() throws EnrichmentException {

        // Create the entity resolver
        final EntityClientConfiguration entityClientConfiguration;
        if (StringUtils.isNotBlank(entityManagementUrl) && StringUtils.isNotBlank(entityApiUrl)
                && StringUtils.isNotBlank(entityApiTokenEndpoint) && StringUtils.isNotBlank(entityApiGrantParams)) {
            final Properties properties = buildEntityApiClientProperties(entityManagementUrl, entityApiUrl,
                entityApiTokenEndpoint, entityApiGrantParams);
            entityClientConfiguration = new EntityClientConfiguration(properties);
        } else {
            throw new EnrichmentException("We must have either a non-null entity resolver creator,"
                    + " or a non-blank enrichment URL.", null);
        }

        // Done.
        return new EnricherImpl(recordParser == null ? new MetisRecordParser() : recordParser,
            new ClientEntityResolverFactory(entityClientConfiguration), new EntityMergeEngine());
    }

}
