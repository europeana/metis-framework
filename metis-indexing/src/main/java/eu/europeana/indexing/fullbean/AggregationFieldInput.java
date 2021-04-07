package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

/**
 * Converts a {@link Aggregation} from an {@link eu.europeana.metis.schema.jibx.RDF} to a {@link
 * AggregationImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
 */
final class AggregationFieldInput implements Function<Aggregation, AggregationImpl> {

  private final List<WebResourceImpl> recordWebResources;
  private final List<WebResourceImpl> referencedWebResources;

  AggregationFieldInput(final List<WebResourceImpl> recordWebResources,
      List<WebResourceImpl> referencedWebResources) {
    this.recordWebResources = recordWebResources;
    this.referencedWebResources = referencedWebResources;
  }

  private String processResource(List<WebResourceImpl> aggregationWebResources,
      ResourceType resource) {
    String resourceString = Optional.ofNullable(resource).map(ResourceType::getResource)
        .map(String::trim).orElse(null);
    final boolean isNotAlreadyReferenced = referencedWebResources.stream().map(WebResourceImpl::getAbout)
        .noneMatch(about -> about.equals(resourceString));
    if (resourceString != null && isNotAlreadyReferenced) {
      final List<WebResourceImpl> matchingWebResources = recordWebResources.stream()
          .filter(webResource -> webResource.getAbout().equals(resourceString))
          .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(matchingWebResources)) {
        aggregationWebResources.addAll(matchingWebResources);
        referencedWebResources.addAll(matchingWebResources);
        recordWebResources.removeAll(matchingWebResources);
      } else {
        WebResourceImpl webResource = new WebResourceImpl();
        webResource.setAbout(resourceString);
        aggregationWebResources.add(webResource);
        referencedWebResources.add(webResource);
      }
    }
    return resourceString;
  }

  @Override
  public AggregationImpl apply(Aggregation aggregation) {

    AggregationImpl mongoAggregation = new AggregationImpl();
    final List<WebResourceImpl> webResources = new ArrayList<>();

    mongoAggregation.setAbout(aggregation.getAbout());
    Map<String, List<String>> dp = FieldInputUtils
        .createResourceOrLiteralMapFromString(aggregation.getDataProvider());
    mongoAggregation.setEdmDataProvider(dp);
    if (aggregation.getIntermediateProviderList() != null) {
      Map<String, List<String>> providers = FieldInputUtils
          .createResourceOrLiteralMapFromList(aggregation.getIntermediateProviderList());
      mongoAggregation.setEdmIntermediateProvider(providers);
    }

    mongoAggregation.setEdmIsShownAt(processResource(webResources, aggregation.getIsShownAt()));
    mongoAggregation.setEdmIsShownBy(processResource(webResources, aggregation.getIsShownBy()));
    mongoAggregation.setEdmObject(processResource(webResources, aggregation.getObject()));

    Map<String, List<String>> prov = FieldInputUtils
        .createResourceOrLiteralMapFromString(aggregation.getProvider());
    mongoAggregation.setEdmProvider(prov);
    Map<String, List<String>> rights = FieldInputUtils
        .createResourceMapFromString(aggregation.getRights());
    mongoAggregation.setEdmRights(rights);

    if (aggregation.getUgc() == null) {
      //False value is not supported in the RDF enumeration so we have to manually add it if Ugc is not present
      mongoAggregation.setEdmUgc("false");
    } else {
      mongoAggregation
          .setEdmUgc(aggregation.getUgc().getUgc().toString().toLowerCase(Locale.ENGLISH));
    }

    String agCHO = Optional.ofNullable(aggregation.getAggregatedCHO())
        .map(ResourceType::getResource).orElse(null);
    mongoAggregation.setAggregatedCHO(agCHO);

    Map<String, List<String>> rights1 = FieldInputUtils
        .createResourceOrLiteralMapFromList(aggregation.getRightList());
    mongoAggregation.setDcRights(rights1);

    if (aggregation.getHasViewList() == null) {
      mongoAggregation.setHasView(null);
    } else {
      String[] hasViewList = aggregation.getHasViewList().stream()
          .map(hasView -> processResource(webResources, hasView)).filter(Objects::nonNull)
          .toArray(String[]::new);
      mongoAggregation.setHasView(hasViewList);
    }

    mongoAggregation.setWebResources(webResources);

    return mongoAggregation;
  }
}
