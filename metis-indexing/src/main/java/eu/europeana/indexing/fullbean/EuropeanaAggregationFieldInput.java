package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import eu.europeana.corelib.definitions.jibx.AggregatedCHO;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;

/**
 * Converts a {@link EuropeanaAggregationType} from an
 * {@link eu.europeana.corelib.definitions.jibx.RDF} to a {@link EuropeanaAggregationImpl} for a
 * {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class EuropeanaAggregationFieldInput {

  EuropeanaAggregationImpl createAggregationMongoFields(EuropeanaAggregationType aggregation) {
    EuropeanaAggregationImpl mongoAggregation = new EuropeanaAggregationImpl();

    mongoAggregation.setAbout(aggregation.getAbout());

    Map<String, List<String>> creator =
        FieldInputUtils.createResourceOrLiteralMapFromString(aggregation.getCreator());

    mongoAggregation.setDcCreator(creator);


    Map<String, List<String>> country = FieldInputUtils.createLiteralMapFromString(
        aggregation.getCountry().getCountry().xmlValue().toLowerCase(Locale.ENGLISH));
    mongoAggregation.setEdmCountry(country);
    String isShownBy =
        FieldInputUtils.exists(IsShownBy::new, aggregation.getIsShownBy()).getResource();
    mongoAggregation.setEdmIsShownBy(isShownBy);

    Map<String, List<String>> language = FieldInputUtils.createLiteralMapFromString(
        aggregation.getLanguage().getLanguage().xmlValue().toLowerCase(Locale.ENGLISH));

    mongoAggregation.setEdmLanguage(language);

    String agCHO =
        FieldInputUtils.exists(AggregatedCHO::new, aggregation.getAggregatedCHO()).getResource();
    mongoAggregation.setAggregatedCHO(agCHO);
    mongoAggregation.setEdmLandingPageFromAggregatedCHO();

    Map<String, List<String>> edmRights =
        FieldInputUtils.createResourceMapFromString(aggregation.getRights());
    mongoAggregation.setEdmRights(edmRights);
    String[] aggregates = FieldInputUtils.resourceListToArray(aggregation.getAggregateList());
    mongoAggregation.setAggregates(aggregates);
    String[] hasViewList = FieldInputUtils.resourceListToArray(aggregation.getHasViewList());
    mongoAggregation.setEdmHasView(hasViewList);
    return mongoAggregation;
  }
}
