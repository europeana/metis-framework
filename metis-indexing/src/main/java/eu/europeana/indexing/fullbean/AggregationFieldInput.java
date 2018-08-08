package eu.europeana.indexing.fullbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;

/**
 * Converts a {@link Aggregation} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link AggregationImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class AggregationFieldInput implements Function<Aggregation, AggregationImpl> {

  private final Supplier<List<WebResourceImpl>> webResourcesSupplier;

  AggregationFieldInput(Supplier<List<WebResourceImpl>> webResourcesSupplier) {
    this.webResourcesSupplier = webResourcesSupplier;
  }

  private static String processResource(List<WebResourceImpl> webResources, ResourceType resource) {
    String resourceString =
        Optional.ofNullable(resource).map(ResourceType::getResource).map(String::trim).orElse(null);
    boolean addWebResource = resourceString != null && webResources.stream()
        .map(WebResourceImpl::getAbout).noneMatch(about -> about.equals(resourceString));
    if (addWebResource) {
      WebResourceImpl webResource = new WebResourceImpl();
      webResource.setAbout(resourceString);
      webResources.add(webResource);
    }
    return resourceString;
  }

  @Override
  public AggregationImpl apply(Aggregation aggregation) {

    AggregationImpl mongoAggregation = new AggregationImpl();
    final List<WebResourceImpl> webResources = new ArrayList<>(webResourcesSupplier.get());

    mongoAggregation.setAbout(aggregation.getAbout());
    Map<String, List<String>> dp =
        FieldInputUtils.createResourceOrLiteralMapFromString(aggregation.getDataProvider());
    mongoAggregation.setEdmDataProvider(dp);
    if (aggregation.getIntermediateProviderList() != null) {
      Map<String, List<String>> providers = FieldInputUtils
          .createResourceOrLiteralMapFromList(aggregation.getIntermediateProviderList());
      mongoAggregation.setEdmIntermediateProvider(providers);
    }

    mongoAggregation.setEdmIsShownAt(processResource(webResources, aggregation.getIsShownAt()));
    mongoAggregation.setEdmIsShownBy(processResource(webResources, aggregation.getIsShownBy()));
    mongoAggregation.setEdmObject(processResource(webResources, aggregation.getObject()));

    Map<String, List<String>> prov =
        FieldInputUtils.createResourceOrLiteralMapFromString(aggregation.getProvider());
    mongoAggregation.setEdmProvider(prov);
    Map<String, List<String>> rights =
        FieldInputUtils.createResourceMapFromString(aggregation.getRights());
    mongoAggregation.setEdmRights(rights);

    if (aggregation.getUgc() != null) {
      mongoAggregation
          .setEdmUgc(aggregation.getUgc().getUgc().toString().toLowerCase(Locale.ENGLISH));
    } else {
      //False value is not supported in the RDF enumeration so we have to manually add it if Ugc is not present
      mongoAggregation.setEdmUgc("false");
    }

    String agCHO = Optional.ofNullable(aggregation.getAggregatedCHO())
        .map(ResourceType::getResource).orElse(null);
    mongoAggregation.setAggregatedCHO(agCHO);

    Map<String, List<String>> rights1 =
        FieldInputUtils.createResourceOrLiteralMapFromList(aggregation.getRightList());
    mongoAggregation.setDcRights(rights1);

    if (aggregation.getHasViewList() != null) {
      String[] hasViewList = aggregation.getHasViewList().stream()
          .map(hasView -> processResource(webResources, hasView)).filter(Objects::nonNull)
          .toArray(String[]::new);
      mongoAggregation.setHasView(hasViewList);
    } else {
      mongoAggregation.setHasView(null);

    }

    mongoAggregation.setWebResources(webResources);

    return mongoAggregation;
  }
}
