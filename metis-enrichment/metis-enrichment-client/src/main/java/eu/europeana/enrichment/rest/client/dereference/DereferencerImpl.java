package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

/**
 * The default implementation of the dereferencing function that accesses a server through HTTP/REST.
 */
public class DereferencerImpl implements Dereferencer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerImpl.class);

    private final EntityMergeEngine entityMergeEngine;
    private final EntityResolver entityResolver;
    private final DereferenceClient dereferenceClient;

    /**
     * Constructor.
     *
     * @param entityMergeEngine The entity merge engine. Cannot be null.
     * @param entityResolver    Remove entity resolver: Can be null if we only dereference own entities.
     * @param dereferenceClient Dereference client. Can be null if we don't dereference own entities.
     */
    public DereferencerImpl(EntityMergeEngine entityMergeEngine, EntityResolver entityResolver,
                            DereferenceClient dereferenceClient) {
        this.entityMergeEngine = entityMergeEngine;
        this.entityResolver = entityResolver;
        this.dereferenceClient = dereferenceClient;
    }

    private static URL checkIfUrlIsValid(HashSet<Report> reports, String id) {
        try {
            URI uri = new URI(id);
            return new URL(uri.toString());
        } catch (URISyntaxException | MalformedURLException e) {
            reports.add(Report
                    .buildDereferenceIgnore()
                    .withStatus(HttpStatus.OK)
                    .withValue(id)
                    .withException(e)
                    .build());
            LOGGER.debug("Invalid enrichment reference found: {}", id);
            return null;
        }
    }

    private static void setDereferenceStatusInReport(String resourceId, HashSet<Report> reports,
                                                     DereferenceResultStatus resultStatus) {
        if (!resultStatus.equals(DereferenceResultStatus.SUCCESS)) {
            String resultMessage;
            switch (resultStatus) {
                case ENTITY_FOUND_XML_XSLT_ERROR:
                    resultMessage = "Entity was found, applying the XSLT results in an XML error"
                            .concat("either because the entity is malformed or the XSLT is malformed).");
                    break;
                case ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS:
                    resultMessage = "Entity was found, but the XSLT mapping did not produce a contextual class.";
                    break;
                case INVALID_URL:
                    resultMessage = "A URL to be dereferenced is invalid.";
                    break;
                case NO_VOCABULARY_MATCHING:
                    resultMessage = "Could not find a vocabulary matching the URL.";
                    break;
                case UNKNOWN_EUROPEANA_ENTITY:
                    resultMessage = "Dereferencing or Coreferencing: the europeana entity does not exist.";
                    break;
                case NO_ENTITY_FOR_VOCABULARY:
                    resultMessage = "Could not find an entity for a known vocabulary.";
                    break;
                case FAILURE:
                    resultMessage = "Dereference or Coreferencing failed.";
                    break;
                default:
                    resultMessage = "";
            }
            if (resultStatus.equals(DereferenceResultStatus.FAILURE)) {
                reports.add(Report.buildDereferenceError()
                        .withValue(resourceId)
                        .withMessage(resultMessage)
                        .build());
            } else if (resultStatus.equals(DereferenceResultStatus.INVALID_URL) ||
                    resultStatus.equals(DereferenceResultStatus.NO_VOCABULARY_MATCHING) ||
                    resultStatus.equals(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS)) {
                reports.add(Report
                        .buildDereferenceIgnore()
                        .withStatus(HttpStatus.OK)
                        .withValue(resourceId)
                        .withMessage(resultMessage)
                        .build());
            } else {
                reports.add(Report
                        .buildDereferenceWarn()
                        .withStatus(HttpStatus.OK)
                        .withValue(resourceId)
                        .withMessage(resultMessage)
                        .build());
            }
        }
    }

    @Override
    public Set<Report> dereference(RDF rdf) {
        // Extract fields from the RDF for dereferencing
        LOGGER.debug(" Extracting fields from RDF for dereferencing...");
        Map<Class<? extends AboutType>, Set<String>> resourceIds = extractReferencesForDereferencing(rdf);

        // Get the dereferenced information to add to the RDF using the extracted fields
        LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
        List<DereferencedEntities> dereferenceInformation = dereferenceEntities(resourceIds);
        Set<Report> reports = dereferenceInformation.stream()
                .map(DereferencedEntities::getReportMessages)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // Merge the acquired information into the RDF
        LOGGER.debug("Merging Dereference Information...");
        entityMergeEngine.mergeReferenceEntitiesFromDereferencedEntities(rdf, dereferenceInformation);

        // Done.
        LOGGER.debug("Dereference completed.");
        return reports;
    }

    @Override
    public List<DereferencedEntities> dereferenceEntities(Map<Class<? extends AboutType>, Set<String>> resourceIds) {

        // Sanity check.
        if (resourceIds.isEmpty()) {
            return List.of(new DereferencedEntities(Collections.emptyMap(), new HashSet<>()));
        }

        //TODO Using TreeMap to sort out the elements because unit tests were failing due to "incorrect" order
        //TODO The order should not matter, the unit tests need fixing
        //TODO There's already ticket MET-5065 to handle it

        // First try to get them from our own entity collection database.
        TreeMap<Class<? extends AboutType>, Set<ReferenceTerm>> mappedReferenceTerms = new TreeMap<>(
                Comparator.comparing(Class::getName));
        TreeMap<Class<? extends AboutType>, DereferencedEntities> dereferencedResultEntities = new TreeMap<>(
                Comparator.comparing(Class::getName));
        resourceIds.forEach((key, value) -> {
            HashSet<Report> reports = new HashSet<>();
            Set<ReferenceTerm> referenceTermSet = setUpReferenceTermSet(value, reports);
            mappedReferenceTerms.put(key, referenceTermSet);
            final DereferencedEntities dereferencedOwnEntities = dereferenceOwnEntities(referenceTermSet, reports, key);
            dereferencedResultEntities.put(key, dereferencedOwnEntities);
        });

        final Set<String> foundOwnEntityIds = dereferencedResultEntities
                .values().stream()
                .map(DereferencedEntities::getReferenceTermListMap)
                .map(Map::values).flatMap(Collection::stream).flatMap(Collection::stream)
                .map(EnrichmentBase::getAbout)
                .collect(Collectors.toSet());

        // For the remaining ones, get them from the dereference service.
        for (Map.Entry<Class<? extends AboutType>, Set<ReferenceTerm>> entry : mappedReferenceTerms.entrySet()) {
            DereferencedEntities dereferencedEntities;
            Set<ReferenceTerm> notFoundOwnReferenceTerms = entry.getValue().stream().filter(
                    referenceTerm -> !foundOwnEntityIds.contains(referenceTerm.getReference().toString())).collect(
                    Collectors.toSet());
            if (notFoundOwnReferenceTerms.isEmpty()) {
                continue;
            }

            if (entry.getKey().equals(Aggregation.class)) {
                dereferencedEntities = dereferenceAggregation(notFoundOwnReferenceTerms, entry.getKey());
            } else {
                dereferencedEntities = dereferenceExternalEntity(notFoundOwnReferenceTerms, entry.getKey());
            }

            updateDereferencedEntitiesMap(dereferencedResultEntities, entry.getKey(), dereferencedEntities);
        }
        // Done.
        return new ArrayList<>(dereferencedResultEntities.values());
    }

    private DereferencedEntities dereferenceAggregation(Set<ReferenceTerm> referenceTerms, Class<? extends AboutType> classType) {

        DereferencedEntities result = dereferenceEntitiesWithUri(referenceTerms,
                new HashSet<>(), classType);

        //Collect references that returned empty lists values for references that we checked with uri
        Set<ReferenceTerm> remainingReferences = referenceTerms.stream().filter(
                        referenceTerm -> result.getReferenceTermListMap().get(referenceTerm).isEmpty())
                .collect(Collectors.toSet());

        //If there are any remaining references then do external dereferencing
        if (CollectionUtils.isNotEmpty(remainingReferences)) {
            DereferencedEntities aggregationRemainingDereferencingResult = dereferenceExternalEntity(remainingReferences, classType);
            result.getReferenceTermListMap().putAll(aggregationRemainingDereferencingResult.getReferenceTermListMap());
            result.getReportMessages().addAll(aggregationRemainingDereferencingResult.getReportMessages());
        }

        return result;
    }

    @Override
    public Map<Class<? extends AboutType>, Set<String>> extractReferencesForDereferencing(RDF rdf) {
        return DereferenceUtils.extractReferencesForDereferencing(rdf);
    }

    public DereferencedEntities dereferenceOwnEntities(Set<ReferenceTerm> resourceIds,
        HashSet<Report> reports,
        Class<? extends AboutType> classType) {
        if (entityResolver == null) {
            return new DereferencedEntities(Collections.emptyMap(), new HashSet<>());
        }
        try {
            Map<ReferenceTerm, List<EnrichmentBase>> result = new HashMap<>();
            Set<ReferenceTerm> ownEntities = resourceIds.stream()
                    .filter(id -> EntityResolver.europeanaLinkPattern.matcher(id.getReference().toString()).matches())
                    .collect(Collectors.toSet());
            entityResolver.resolveById(ownEntities)
                    .forEach((key, value) -> result.put(key, List.of(value)));
            ownEntities.stream().filter(id -> result.get(id) == null || result.get(id).isEmpty())
                    .forEach(notFoundOwnId -> {
                        setDereferenceStatusInReport(notFoundOwnId.getReference().toString(),
                                reports, DereferenceResultStatus.UNKNOWN_EUROPEANA_ENTITY);
                        result.putIfAbsent(notFoundOwnId, Collections.emptyList());
                    });
            return new DereferencedEntities(result, reports, classType);
        } catch (Exception e) {
            return handleDereferencingException(resourceIds, reports, e, classType);
        }
    }

    private DereferencedEntities dereferenceEntitiesWithUri(Set<ReferenceTerm> resourceIds,
                                                            HashSet<Report> reports,
                                                            Class<? extends AboutType> classType) {
        if (entityResolver == null) {
            return new DereferencedEntities(Collections.emptyMap(), new HashSet<>());
        }
        try {
            return new DereferencedEntities(new HashMap<>(entityResolver.resolveByUri(resourceIds)), reports, classType);
        } catch (Exception e) {
            return handleDereferencingException(resourceIds, reports, e, classType);
        }
    }

    private DereferencedEntities dereferenceExternalEntity(Set<ReferenceTerm> referenceTerms,
                                                           Class<? extends AboutType> classType) {
        HashSet<Report> reports = new HashSet<>();
        // Check that there is something to do.
        if (dereferenceClient == null) {
            return new DereferencedEntities(Collections.emptyMap(), reports, classType);
        }

        // Perform the dereferencing.
        EnrichmentResultList result;
        Map<ReferenceTerm, List<EnrichmentBase>> resultMap = new HashMap<>();
        for (ReferenceTerm referenceTerm : referenceTerms) {
            String resourceId = referenceTerm.getReference().toString();
            try {
                LOGGER.debug("Dereference external entity processing {}", resourceId);
                result = retryableExternalRequestForNetworkExceptions(
                        () -> dereferenceClient.dereference(resourceId));
                DereferenceResultStatus resultStatus = Optional.ofNullable(result)
                        .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                        .orElseGet(Collections::emptyList).stream()
                        .map(EnrichmentResultBaseWrapper::getDereferenceStatus)
                        .filter(Objects::nonNull).findFirst()
                        .orElse(DereferenceResultStatus.FAILURE);

                setDereferenceStatusInReport(resourceId, reports, resultStatus);
            } catch (BadRequest e) {
                // We are forgiving for these errors
                LOGGER.warn("ResourceId {}, failed", resourceId, e);
                reports.add(Report
                        .buildDereferenceWarn()
                        .withStatus(HttpStatus.BAD_REQUEST)
                        .withValue(resourceId)
                        .withException(e)
                        .build());
                result = null;
            } catch (Exception e) {
                DereferenceException dereferenceException = new DereferenceException(
                        "Exception occurred while trying to perform dereferencing.", e);
                    reports.add(Report
                            .buildDereferenceError()
                            .withValue(resourceId)
                            .withException(dereferenceException)
                            .build());
                result = null;
            }
            resultMap.put(referenceTerm, Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                    .orElseGet(Collections::emptyList).stream()
                    .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
                    .flatMap(List::stream).collect(Collectors.toList()));
        }

        // Return the result.
        return new DereferencedEntities(resultMap, reports, classType);
    }

    private Set<ReferenceTerm> setUpReferenceTermSet(Set<String> resourcesIds, HashSet<Report> reports) {
        return resourcesIds.stream()
                .map(id -> checkIfUrlIsValid(reports, id))
                .filter(Objects::nonNull)
                .map(validateUrl -> new ReferenceTermImpl(validateUrl, new HashSet<>()))
                .collect(Collectors.toSet());
    }

    private void updateDereferencedEntitiesMap(TreeMap<Class<? extends AboutType>, DereferencedEntities> mapToUpdate,
                                               Class<? extends AboutType> classType,
                                               DereferencedEntities elementToUpdateWith) {

        DereferencedEntities foundEntities = mapToUpdate.get(classType);
        foundEntities.getReferenceTermListMap().putAll(elementToUpdateWith.getReferenceTermListMap());
        foundEntities.getReportMessages().addAll(elementToUpdateWith.getReportMessages());

    }

    private DereferencedEntities handleDereferencingException(Set<ReferenceTerm> resourceIds, HashSet<Report> reports,
                                                              Exception exception, Class<? extends AboutType> classType) {
        DereferenceException dereferenceException = new DereferenceException(
                "Exception occurred while trying to perform dereferencing.", exception);
        reports.add(Report
                .buildDereferenceWarn()
                .withStatus(HttpStatus.OK)
                .withValue(resourceIds.stream()
                        .map(resourceId -> resourceId.getReference().toString())
                        .collect(Collectors.joining(",")))
                .withException(dereferenceException)
                .build());
        return new DereferencedEntities(new HashMap<>(), reports, classType);
    }
}
