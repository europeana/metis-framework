package eu.europeana.indexing.solr;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.property.AgentSolrCreator;
import eu.europeana.indexing.solr.property.AggregationSolrCreator;
import eu.europeana.indexing.solr.property.ConceptSolrCreator;
import eu.europeana.indexing.solr.property.EuropeanaAggregationSolrCreator;
import eu.europeana.indexing.solr.property.LicenseSolrCreator;
import eu.europeana.indexing.solr.property.PlaceSolrCreator;
import eu.europeana.indexing.solr.property.ProvidedChoSolrCreator;
import eu.europeana.indexing.solr.property.ProxySolrCreator;
import eu.europeana.indexing.solr.property.ServiceSolrCreator;
import eu.europeana.indexing.solr.property.SolrPropertyUtils;
import eu.europeana.indexing.solr.property.TimespanSolrCreator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.utils.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

/**
 * This class provides functionality to populate Solr documents. Both methods in this class should
 * be called to fill the Solr document. The method {@link #populateWithProperties(SolrInputDocument,
 * FullBeanImpl)} copies properties from the source to the Solr document. The method {@link
 * #populateWithFacets(SolrInputDocument, RdfWrapper)} on the other hand performs some analysis
 * and sets technical metadata.
 *
 * @author jochen
 */
public class SolrDocumentPopulator {

  /**
   * Populates a Solr document with the properties of the full bean. Please note: this method should
   * only be called once on a given document, otherwise the behavior is not defined.
   *
   * @param document The Solr document to populate.
   * @param fullBean The FullBean to populate from.
   */
  public void populateWithProperties(SolrInputDocument document, FullBeanImpl fullBean) {

    // Gather the licenses.
    final List<LicenseImpl> licenses;
    if (fullBean.getLicenses() == null) {
      licenses = Collections.emptyList();
    } else {
      licenses = fullBean.getLicenses().stream().filter(Objects::nonNull)
          .collect(Collectors.toList());
    }

    // Gather the quality annotations.
    final Set<String> acceptableTargets = Optional.ofNullable(fullBean.getAggregations())
        .map(List::stream).orElseGet(Stream::empty).filter(Objects::nonNull)
        .map(AggregationImpl::getAbout).filter(Objects::nonNull).collect(Collectors.toSet());
    final Predicate<QualityAnnotation> hasAcceptableTarget = annotation -> Optional
        .ofNullable(annotation.getTarget()).map(Arrays::stream).orElseGet(Stream::empty)
        .anyMatch(acceptableTargets::contains);
    final Map<String, QualityAnnotation> qualityAnnotations = Optional
        .ofNullable(fullBean.getQualityAnnotations()).map(List::stream).orElseGet(Stream::empty)
        .filter(Objects::nonNull)
        .filter(annotation -> StringUtils.isNotBlank(annotation.getAbout()))
        .filter(hasAcceptableTarget)
        .collect(Collectors.toMap(QualityAnnotation::getAbout, Function.identity(), (v1, v2) -> v1));

    // Add the containing objects.
    new ProvidedChoSolrCreator().addToDocument(document, fullBean.getProvidedCHOs().get(0));
    new AggregationSolrCreator(licenses).addToDocument(document,
        fullBean.getAggregations().get(0));
    new EuropeanaAggregationSolrCreator(licenses, qualityAnnotations::get)
        .addToDocument(document, fullBean.getEuropeanaAggregation());
    new ProxySolrCreator().addAllToDocument(document, fullBean.getProxies());
    new ConceptSolrCreator().addAllToDocument(document, fullBean.getConcepts());
    new TimespanSolrCreator().addAllToDocument(document, fullBean.getTimespans());
    new AgentSolrCreator().addAllToDocument(document, fullBean.getAgents());
    new PlaceSolrCreator().addAllToDocument(document, fullBean.getPlaces());
    new ServiceSolrCreator().addAllToDocument(document, fullBean.getServices());

    // Add the licenses.
    final Set<String> defRights = fullBean.getAggregations().stream()
        .map(AggregationImpl::getEdmRights).filter(Objects::nonNull)
        .flatMap(SolrPropertyUtils::getRightsFromMap).collect(Collectors.toSet());
    new LicenseSolrCreator(license -> defRights.contains(license.getAbout()))
        .addAllToDocument(document, fullBean.getLicenses());

    // Add the top-level properties.
    document.addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(),
        fullBean.getEuropeanaCompleteness());
    document.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
        fullBean.getEuropeanaCollectionName()[0]);
    document.addField(EdmLabel.TIMESTAMP_CREATED.toString(), fullBean.getTimestampCreated());
    document.addField(EdmLabel.TIMESTAMP_UPDATED.toString(), fullBean.getTimestampUpdated());
  }

  /**
   * Populates a Solr document with the CRF fields of the RDF. Please note: this method should only
   * be called once on a given document, otherwise the behavior is not defined.
   *
   * @param document The document to populate.
   * @param rdf The RDF to populate from.
   */
  public void populateWithFacets(SolrInputDocument document, RdfWrapper rdf) {

    // has_thumbnails is true if and only if edm:EuropeanaAggregation/edm:preview is filled and the
    // associated edm:webResource exists with technical metadata (i.e. ebucore:hasMimetype is set).
    document.addField(EdmLabel.FACET_HAS_THUMBNAILS.toString(), rdf.hasThumbnails());

    // has_media is true if and only if there is at least one web resource of type 'isShownBy'
    // or 'hasView' representing technical metadata of a known type.
    final List<WebResourceWrapper> webResourcesWithMedia = rdf.getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.IS_SHOWN_BY, WebResourceLinkType.HAS_VIEW));
    final boolean hasMedia = webResourcesWithMedia.stream().map(WebResourceWrapper::getMediaType)
        .anyMatch(type -> type != MediaType.OTHER);
    document.addField(EdmLabel.FACET_HAS_MEDIA.toString(), hasMedia);

    // has_landingPage is true if and only if there is at least one web resource of type
    // 'isShownAt', representing technical metadata of some (non-empty) mime type.
    document.addField(EdmLabel.FACET_HAS_LANDING_PAGE.toString(), rdf.hasLandingPage());

    // is_fulltext is true if and only if there is at least one web resource of type 'isShownBy'
    // or 'hasView' with 'rdf:type' equal to 'edm:FullTextResource'.
    final boolean isFullText = webResourcesWithMedia.stream().map(WebResourceWrapper::getType)
        .anyMatch("http://www.europeana.eu/schemas/edm/FullTextResource"::equals);
    document.addField(EdmLabel.FACET_IS_FULL_TEXT.toString(), isFullText);

    // Compose the filter and facet tags. Only use the web resources of type 'isShownBy' or 'hasView'.
    final Set<Integer> filterCodes = new HashSet<>();
    final Set<Integer> valueCodes = new HashSet<>();
    final FacetEncoder encoder = new FacetEncoder();
    for (WebResourceWrapper webResource : webResourcesWithMedia) {
      filterCodes.addAll(encoder.getFacetFilterCodes(webResource));
      valueCodes.addAll(encoder.getFacetValueCodes(webResource));
    }

    // Add the filter and facet tags to the Solr document.
    for (Integer code : filterCodes) {
      document.addField(EdmLabel.FACET_FILTER_CODES.toString(), code);
    }
    for (Integer code : valueCodes) {
      document.addField(EdmLabel.FACET_VALUE_CODES.toString(), code);
    }
  }
}
