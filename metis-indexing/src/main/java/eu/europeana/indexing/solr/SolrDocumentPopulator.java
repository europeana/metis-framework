package eu.europeana.indexing.solr;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.crf.EncodedMediaType;
import eu.europeana.indexing.solr.crf.TagExtractor;
import eu.europeana.indexing.solr.crf.WebResourceWrapper;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides functionality to populate Solr documents. Both methods in this class should
 * be called to fill the Solr document. The method {@link #populateWithProperties(SolrInputDocument,
 * FullBeanImpl)} copies properties from the source to the Solr document. The method {@link
 * #populateWithCrfFields(SolrInputDocument, RDF)} on the other hand performs some analysis and sets
 * technical metadata.
 *
 * @author jochen
 */
public class SolrDocumentPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrDocumentPopulator.class);

  /**
   * Populates a Solr document with the properties of the full bean. Please note: this method should
   * only be called once on a given document, otherwise the behavior is not defined.
   *
   * @param document The Solr document to populate.
   * @param fullBean The FullBean to populate from.
   * @return The SolrInputDocument representation of the FullBean
   */
  public SolrInputDocument populateWithProperties(SolrInputDocument document,
      FullBeanImpl fullBean) {

    final List<LicenseImpl> lincenses;
    if (fullBean.getLicenses() != null) {
      lincenses = fullBean.getLicenses().stream().filter(Objects::nonNull)
          .collect(Collectors.toList());
    } else {
      lincenses = Collections.emptyList();
    }

    new ProvidedChoSolrCreator().addToDocument(document, fullBean.getProvidedCHOs().get(0));
    new AggregationSolrCreator(lincenses).addToDocument(document,
        fullBean.getAggregations().get(0));
    new EuropeanaAggregationSolrCreator(lincenses).addToDocument(document,
        fullBean.getEuropeanaAggregation());
    new ProxySolrCreator().addAllToDocument(document, fullBean.getProxies());
    new ConceptSolrCreator().addAllToDocument(document, fullBean.getConcepts());
    new TimespanSolrCreator().addAllToDocument(document, fullBean.getTimespans());
    new AgentSolrCreator().addAllToDocument(document, fullBean.getAgents());
    new PlaceSolrCreator().addAllToDocument(document, fullBean.getPlaces());
    new ServiceSolrCreator().addAllToDocument(document, fullBean.getServices());

    final Set<String> defRights = fullBean.getAggregations().stream()
        .map(AggregationImpl::getEdmRights).filter(Objects::nonNull)
        .flatMap(SolrPropertyUtils::getRightsFromMap).collect(Collectors.toSet());
    new LicenseSolrCreator(license -> defRights.contains(license.getAbout()))
        .addAllToDocument(document, fullBean.getLicenses());

    document.addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(),
        fullBean.getEuropeanaCompleteness());
    document.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
        fullBean.getEuropeanaCollectionName()[0]);
    document.addField(EdmLabel.TIMESTAMP_CREATED.toString(), fullBean.getTimestampCreated());
    document.addField(EdmLabel.TIMESTAMP_UPDATED.toString(), fullBean.getTimestampUpdated());

    return document;
  }

  /**
   * Populates a Solr document with the CRF fields of the RDF. Please note: this method should only
   * be called once on a given document, otherwise the behavior is not defined.
   *
   * @param document The document to populate.
   * @param rdf The RDF to populate from.
   */
  public void populateWithCrfFields(SolrInputDocument document, RDF rdf) {

    // Check Europeana aggregation list.
    final List<EuropeanaAggregationType> aggregationList =
        rdf.getEuropeanaAggregationList() != null ? rdf.getEuropeanaAggregationList()
            : Collections.emptyList();
    if (aggregationList.size() > 1) {
      LOGGER
          .info("Multiple Europeana aggregations found in RDF: ignoring all except the last one.");
    }
    final EuropeanaAggregationType aggregation =
        aggregationList.isEmpty() ? null : aggregationList.get(aggregationList.size() - 1);

    // Get the web resources.
    final List<WebResourceWrapper> webResources = WebResourceWrapper.getListFromRdf(rdf);

    // has_thumbnails is true if and only if edm:EuropeanaAggregation/edm:preview is filled and the
    // associated edm:webResource exists with technical metadata (i.e. ebucore:hasMimetype is set).
    final String previewUri =
        Optional.ofNullable(aggregation).map(EuropeanaAggregationType::getPreview)
            .map(ResourceType::getResource).filter(StringUtils::isNotBlank).orElse(null);
    final boolean hasThumbnails = previewUri != null
        && webResources.stream().filter(resource -> previewUri.equals(resource.getAbout()))
        .map(WebResourceWrapper::getMimeType).anyMatch(Objects::nonNull);
    document.addField(EdmLabel.CRF_HAS_THUMBNAILS.toString(), hasThumbnails);

    // Compose the set of isShownAt urls.
    final Set<String> isShownAtUrls;
    if (rdf.getAggregationList() == null) {
      isShownAtUrls = Collections.emptySet();
    } else {
      isShownAtUrls = rdf.getAggregationList().stream().map(Aggregation::getIsShownAt)
          .filter(Objects::nonNull).map(IsShownAt::getResource).collect(Collectors.toSet());
    }

    // has_media is true if and only if there is at least one web resource, not of type
    // 'is_shown_at', representing technical metadata of a known type.
    final boolean hasMedia =
        webResources.stream().filter(resource -> !isShownAtUrls.contains(resource.getAbout()))
            .map(WebResourceWrapper::getMediaType).anyMatch(type -> type != EncodedMediaType.OTHER);
    document.addField(EdmLabel.CRF_HAS_MEDIA.toString(), hasMedia);

    // has_landingPage is true if and only if there is at least one web resource of type
    // 'is_shown_at', representing technical metadata of some (non-null) mime type.
    final boolean hasLandingPage = webResources.stream()
        .filter(resource -> isShownAtUrls.contains(resource.getAbout()))
        .map(WebResourceWrapper::getMimeType).anyMatch(Objects::nonNull);
    document.addField(EdmLabel.CRF_HAS_LANDING_PAGE.toString(), hasLandingPage);

    // is_fulltext is true if and only if there is at least one web resource with 'rdf:type' equal
    // to 'edm:FullTextResource'.
    final boolean isFullText = webResources.stream().map(WebResourceWrapper::getType)
        .anyMatch("http://www.europeana.eu/schemas/edm/FullTextResource"::equals);
    document.addField(EdmLabel.CRF_IS_FULL_TEXT.toString(), isFullText);

    // Compose the filter and facet tags.
    final Set<Integer> filterTags = new HashSet<>();
    final Set<Integer> facetTags = new HashSet<>();
    final TagExtractor tagExtractor = new TagExtractor();
    for (WebResourceWrapper webResource : webResources) {
      filterTags.addAll(tagExtractor.getFilterTags(webResource));
      facetTags.addAll(tagExtractor.getFacetTags(webResource));
    }

    // Add the filter and facet tags to the Solr document.
    for (Integer tag : filterTags) {
      document.addField(EdmLabel.CRF_FILTER_TAGS.toString(), tag);
    }
    for (Integer tag : facetTags) {
      document.addField(EdmLabel.CRF_FACET_TAGS.toString(), tag);
    }
  }
}
