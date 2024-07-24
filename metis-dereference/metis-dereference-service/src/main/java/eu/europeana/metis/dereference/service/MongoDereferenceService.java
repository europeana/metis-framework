package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.utils.EnrichmentBaseConverter;
import eu.europeana.metis.dereference.DereferenceResult;
import eu.europeana.metis.dereference.IncomingRecordToEdmTransformer;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.GraphUtils;
import eu.europeana.metis.dereference.service.utils.VocabularyCandidates;
import eu.europeana.metis.exception.BadContentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoDereferenceService implements DereferenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

    private final RdfRetriever retriever;
    private final ProcessedEntityDao processedEntityDao;
    private final VocabularyDao vocabularyDao;

    /**
     * Constructor.
     *
     * @param processedEntityDao Object managing the processed entity cache.
     * @param vocabularyDao      Object that accesses vocabularies.
     */
    @Autowired
    public MongoDereferenceService(ProcessedEntityDao processedEntityDao,
                                   VocabularyDao vocabularyDao) {
        this(new RdfRetriever(), processedEntityDao, vocabularyDao);
    }

    /**
     * Constructor.
     *
     * @param retriever          Object that retrieves entities from their source services.
     * @param processedEntityDao Object managing the processed entity cache.
     * @param vocabularyDao      Object that accesses vocabularies.
     */
    MongoDereferenceService(RdfRetriever retriever, ProcessedEntityDao processedEntityDao,
                            VocabularyDao vocabularyDao) {
        this.retriever = retriever;
        this.processedEntityDao = processedEntityDao;
        this.vocabularyDao = vocabularyDao;
    }

    @Override
    public DereferenceResult dereference(String resourceId) {

        // Sanity checks
        if (resourceId == null) {
            throw new IllegalArgumentException("Parameter resourceId cannot be null.");
        }

        // Get the main object to dereference. In case of errors we are done.
        final TransformedEntity resource = dereferenceSingleResource(resourceId);
        if (resource.getResultStatus() != DereferenceResultStatus.SUCCESS) {
            return new DereferenceResult(resource.getResultStatus());
        }

        // Deserialize the entity
        final EnrichmentBase deserializedEntity;
        try {
            deserializedEntity = resource.getTransformedEntity() == null ? null
                : EnrichmentBaseConverter.convertToEnrichmentBase(resource.getTransformedEntity());
        } catch (JAXBException e) {
            LOGGER.info("Problem occurred while parsing transformed entity {}.", resourceId, e);
            return new DereferenceResult(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_ERROR);
        }

        // Perform the breadth-first search to search for broader terms (if needed).
        final int iterations = resource.getVocabulary().getIterations();
        final Map<String, Pair<EnrichmentBase, DereferenceResultStatus>> result;
        if (iterations > 0) {
            result = GraphUtils.breadthFirstSearch(resourceId,
                new ImmutablePair<>(deserializedEntity, resource.getResultStatus()),
                resource.getVocabulary().getIterations(),
                this::resolveBroaderValue, this::extractBroaderResources);
        } else {
            result = new HashMap<>();
            result.put(resourceId, new ImmutablePair<>(deserializedEntity,
                resource.getResultStatus()));
        }

        // Done. Collect results.
        return new DereferenceResult(
                result.values().stream().map(Pair::getLeft).collect(Collectors.toList()),
                result.values().stream().map(Pair::getRight).filter(Objects::nonNull).findFirst()
                        .orElse(DereferenceResultStatus.SUCCESS));
    }

    private Pair<EnrichmentBase, DereferenceResultStatus> resolveBroaderValue(String resourceId) {
        final TransformedEntity resource = dereferenceSingleResource(resourceId);
        final EnrichmentBase deserializedEntity;
        try {
            deserializedEntity = resource.getTransformedEntity() == null ? null
                : EnrichmentBaseConverter.convertToEnrichmentBase(resource.getTransformedEntity());
        } catch (JAXBException e) {
            LOGGER.info("Problem occurred while parsing transformed entity {}.", resourceId, e);
            return new ImmutablePair<>(null, DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_ERROR);
        }
        return new ImmutablePair<>(deserializedEntity, resource.getResultStatus());
    }

    private void extractBroaderResources(Pair<EnrichmentBase, DereferenceResultStatus> resource,
                                         Set<String> destination) {
        final Stream<String> resourceIdStream;
        if (resource.getLeft() instanceof Concept concept) {
            resourceIdStream = Optional.ofNullable(concept.getBroader()).stream()
                .flatMap(Collection::stream).map(Resource::getResource);
        } else if (resource.getLeft() instanceof TimeSpan timeSpan) {
            resourceIdStream = Optional.ofNullable(timeSpan.getIsPartOf()).stream()
                .flatMap(List::stream).map(LabelResource::getResource);
        } else if (resource.getLeft() instanceof Place place) {
            resourceIdStream = Optional.ofNullable(place.getIsPartOf()).stream()
                .flatMap(Collection::stream).map(LabelResource::getResource);
        } else {
            resourceIdStream = Stream.empty();
        }
        resourceIdStream.filter(Objects::nonNull).forEach(destination::add);
    }

    // TODO this code is used to determine whether the vocabulary has changed. Don't forget this.
/*  Vocabulary cachedVocabulary = null;
    boolean cachedVocabularyChanged = false;
    if (cachedEntity != null && StringUtils.isNotBlank(cachedEntity.getVocabularyId())) {
        cachedVocabulary = vocabularyDao.get(cachedEntity.getVocabularyId());
        cachedVocabularyChanged = cachedVocabulary == null;
    }
*/

    private TransformedEntity dereferenceSingleResource(String resourceId) {

        // Check for URI validity.
        try {
            new URI(resourceId);
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid URI: {} with message: {}", resourceId, e.getMessage());
            return new TransformedEntity(null, null, DereferenceResultStatus.INVALID_URL);
        }

        // Find matching vocabularies, report if there are none.
        final VocabularyCandidates vocabularyCandidates;
        try {
            vocabularyCandidates = VocabularyCandidates.findVocabulariesForUrl(resourceId,
                vocabularyDao::getByUriSearch);
        } catch (URISyntaxException e) {
            // Shouldn't happen as we checked this before.
            LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.",
                resourceId), e);
            return new TransformedEntity(null, null, DereferenceResultStatus.FAILURE);
        }
        if (vocabularyCandidates.isEmpty()) {
            return new TransformedEntity(null, null,
                DereferenceResultStatus.NO_VOCABULARY_MATCHING);
        }

        // If there are vocabularies, we attempt to obtain the original entity from source.
        final OriginalEntity originalEntity = retrieveOriginalEntity(resourceId,
            vocabularyCandidates.getVocabulariesSuffixes());
        if (originalEntity.getResultStatus() != DereferenceResultStatus.SUCCESS) {
            return new TransformedEntity(null, null, originalEntity.getResultStatus());
        }

        // If we managed to obtain the original entity, we will try to transform it.
        final Set<DereferenceResultStatus> statuses = new HashSet<>();
        for (Vocabulary vocabulary : vocabularyCandidates.getVocabularies()) {
            final TransformedEntity transformedEntity = transformEntity(vocabulary,
                originalEntity.getEntity(), resourceId);
            if (transformedEntity.getResultStatus() == DereferenceResultStatus.SUCCESS) {
                return transformedEntity;
            }
            statuses.add(transformedEntity.getResultStatus());
        }

        // If we here, we did not find a successful transformation.
        final DereferenceResultStatus status = statuses.contains(
            DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS)
            ? DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS
            : statuses.stream().findAny().orElseThrow(IllegalStateException::new);
        return new TransformedEntity(null, null, status);
    }

 /*   private void saveEntity(String resourceId, DereferenceResultWrapper transformedEntityAndVocabularyPair) {

        final String entityXml = transformedEntityAndVocabularyPair.getEntity();
        final Vocabulary vocabulary = transformedEntityAndVocabularyPair.getVocabulary();
        final String vocabularyIdString = Optional.ofNullable(vocabulary).map(Vocabulary::getId)
                .map(ObjectId::toString).orElse(null);
        //Save entity
        ProcessedEntity entityToCache =  new ProcessedEntity();
        entityToCache.setResourceId(resourceId);
        entityToCache.setXml(entityXml);
        entityToCache.setVocabularyId(vocabularyIdString);
        processedEntityDao.save(entityToCache);
    }*/

    private TransformedEntity transformEntity(Vocabulary vocabulary, final String originalEntity,
                final String resourceId) {
        try {
            final IncomingRecordToEdmTransformer incomingRecordToEdmTransformer =
                new IncomingRecordToEdmTransformer(vocabulary.getXslt());
            final String result = incomingRecordToEdmTransformer
                .transform(originalEntity, resourceId).orElse(null);
            final DereferenceResultStatus resultStatus;
            if (result == null) {
                resultStatus = DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS;
            } else {
                resultStatus = DereferenceResultStatus.SUCCESS;
            }
            return new TransformedEntity(vocabulary, result, resultStatus);
        } catch (TransformerException | BadContentException | ParserConfigurationException e) {
            LOGGER.warn("Error transforming entity: {} with message: {}", resourceId,
                e.getMessage());
            LOGGER.debug("Transformation issue: ", e);
            return new TransformedEntity(vocabulary, null,
                DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_ERROR);
        }
    }

    private OriginalEntity retrieveOriginalEntity(String resourceId, Set<String> potentialSuffixes) {

        // Sanity check: this should not happen.
        if (potentialSuffixes.isEmpty()) {
            throw new IllegalArgumentException();
        }

        // Compute the result (a URI syntax issue is considered a problem with the suffix).
        final String originalEntity = potentialSuffixes.stream().map(suffix -> {
            try {
                return retriever.retrieve(resourceId, suffix);
            } catch (IOException | URISyntaxException e) {
                LOGGER.warn("Failed to retrieve: {} with message: {}", resourceId, e.getMessage());
                LOGGER.debug("Problem retrieving resource.", e);
                return null;
            }
        }).filter(Objects::nonNull).findAny().orElse(null);

        // Evaluate and return the result.
        if (originalEntity == null && LOGGER.isInfoEnabled()) {
            LOGGER.info("No entity XML for uri {}", resourceId);
        }
        final DereferenceResultStatus dereferenceResultStatus = originalEntity == null ?
            DereferenceResultStatus.NO_ENTITY_FOR_VOCABULARY : DereferenceResultStatus.SUCCESS;
        return new OriginalEntity(originalEntity, dereferenceResultStatus);
    }
}
