package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;

/**
 * Converts a {@link EuropeanaAggregationType} from an
 * {@link eu.europeana.corelib.definitions.jibx.RDF} to a {@link EuropeanaAggregationImpl} for a
 * {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class EuropeanaAggregationFieldInput
    implements Function<EuropeanaAggregationType, EuropeanaAggregationImpl> {

  @Override
  public EuropeanaAggregationImpl apply(EuropeanaAggregationType aggregation) {
    EuropeanaAggregationImpl mongoAggregation = new EuropeanaAggregationImpl();

    mongoAggregation.setAbout(aggregation.getAbout());

    Map<String, List<String>> creator =
        FieldInputUtils.createResourceOrLiteralMapFromString(aggregation.getCreator());

    mongoAggregation.setDcCreator(creator);

    Map<String, List<String>> country = FieldInputUtils.createLiteralMapFromString(
        aggregation.getCountry().getCountry().xmlValue());
    mongoAggregation.setEdmCountry(country);
    String isShownBy =
        Optional.ofNullable(aggregation.getIsShownBy()).map(ResourceType::getResource).orElse(null);
    mongoAggregation.setEdmIsShownBy(isShownBy);

    Map<String, List<String>> language = FieldInputUtils.createLiteralMapFromString(
        aggregation.getLanguage().getLanguage().xmlValue().toLowerCase(Locale.ENGLISH));

    mongoAggregation.setEdmLanguage(language);

    String agCHO = Optional.ofNullable(aggregation.getAggregatedCHO())
        .map(ResourceType::getResource).orElse(null);
    mongoAggregation.setAggregatedCHO(agCHO);

    Map<String, List<String>> edmRights =
        FieldInputUtils.createResourceMapFromString(aggregation.getRights());
    mongoAggregation.setEdmRights(edmRights);
    String[] aggregates = FieldInputUtils.resourceListToArray(aggregation.getAggregateList());
    mongoAggregation.setAggregates(aggregates);
    String[] hasViewList = FieldInputUtils.resourceListToArray(aggregation.getHasViewList());
    mongoAggregation.setEdmHasView(hasViewList);
    String edmPreview = Optional.ofNullable(aggregation.getPreview())
        .map(ResourceType::getResource).orElse(null);
    mongoAggregation.setEdmPreview(edmPreview);
    return mongoAggregation;
  }
}
