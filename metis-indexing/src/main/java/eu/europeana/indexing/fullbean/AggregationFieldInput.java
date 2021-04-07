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
import java.util.Set;
import java.util.function.Function;

/**
 * Converts a {@link Aggregation} from an {@link eu.europeana.metis.schema.jibx.RDF} to a {@link
 * AggregationImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
 */
final class AggregationFieldInput implements Function<Aggregation, AggregationImpl> {

  private final Map<String, WebResourceImpl> recordWebResourcesMap;
  private final Set<String> referencedWebResourceAbouts;

  AggregationFieldInput(final Map<String, WebResourceImpl> recordWebResourcesMap,
      Set<String> referencedWebResourceAbouts) {
    this.recordWebResourcesMap = recordWebResourcesMap;
    this.referencedWebResourceAbouts = referencedWebResourceAbouts;
  }

  private String processResource(List<WebResourceImpl> aggregationWebResources,
      ResourceType resource) {
    String resourceString = Optional.ofNullable(resource).map(ResourceType::getResource)
        .map(String::trim).orElse(null);
    if (resourceString != null && !referencedWebResourceAbouts.contains(resourceString)) {
      final WebResourceImpl matchingWebResource = recordWebResourcesMap.get(resourceString);

      if (matchingWebResource != null) {
        aggregationWebResources.add(matchingWebResource);
        referencedWebResourceAbouts.add(matchingWebResource.getAbout());
        recordWebResourcesMap.remove(matchingWebResource.getAbout());
      } else {
        WebResourceImpl webResource = new WebResourceImpl();
        webResource.setAbout(resourceString);
        aggregationWebResources.add(webResource);
        referencedWebResourceAbouts.add(webResource.getAbout());
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
