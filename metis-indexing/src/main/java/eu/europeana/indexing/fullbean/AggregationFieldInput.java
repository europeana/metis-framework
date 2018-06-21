package eu.europeana.indexing.fullbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import eu.europeana.corelib.definitions.jibx.AggregatedCHO;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.definitions.jibx._Object;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;

/**
 * Converts a {@link Aggregation} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link AggregationImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class AggregationFieldInput {

  AggregationImpl createAggregationMongoFields(Aggregation aggregation,
      List<WebResourceImpl> webResources) {
    AggregationImpl mongoAggregation = new AggregationImpl();

    mongoAggregation.setAbout(aggregation.getAbout());
    Map<String, List<String>> dp =
        FieldInputUtils.createResourceOrLiteralMapFromString(aggregation.getDataProvider());
    mongoAggregation.setEdmDataProvider(dp);
    if (aggregation.getIntermediateProviderList() != null) {
      Map<String, List<String>> providers = FieldInputUtils
          .createResourceOrLiteralMapFromList(aggregation.getIntermediateProviderList());
      mongoAggregation.setEdmIntermediateProvider(providers);
    }
    String isShownAt =
        FieldInputUtils.exists(IsShownAt::new, (aggregation.getIsShownAt())).getResource();
    mongoAggregation.setEdmIsShownAt(isShownAt != null ? isShownAt.trim() : null);
    boolean containsIsShownAt = false;
    for (WebResourceImpl wr : webResources) {
      if (StringUtils.equals(wr.getAbout(), isShownAt)) {
        containsIsShownAt = true;
      }
    }
    if (!containsIsShownAt && isShownAt != null) {
      WebResourceImpl wr = new WebResourceImpl();
      wr.setAbout(isShownAt);
      webResources.add(wr);
    }
    String isShownBy =
        FieldInputUtils.exists(IsShownBy::new, (aggregation.getIsShownBy())).getResource();
    mongoAggregation.setEdmIsShownBy(isShownBy != null ? isShownBy.trim() : null);
    boolean containsIsShownBy = false;
    for (WebResourceImpl wr : webResources) {
      if (StringUtils.equals(wr.getAbout(), isShownBy)) {
        containsIsShownBy = true;
      }
    }
    if (!containsIsShownBy && isShownBy != null) {
      WebResourceImpl wr = new WebResourceImpl();
      wr.setAbout(isShownBy);
      webResources.add(wr);
    }
    String object = FieldInputUtils.exists(_Object::new, (aggregation.getObject())).getResource();
    mongoAggregation.setEdmObject(object != null ? object.trim() : null);
    boolean containsObject = false;
    for (WebResourceImpl wr : webResources) {
      if (StringUtils.equals(wr.getAbout(), object)) {
        containsObject = true;
      }
    }
    if (!containsObject && object != null) {
      WebResourceImpl wr = new WebResourceImpl();
      wr.setAbout(object);
      webResources.add(wr);
    }
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
      mongoAggregation.setEdmUgc(null);
    }

    String agCHO =
        FieldInputUtils.exists(AggregatedCHO::new, (aggregation.getAggregatedCHO())).getResource();
    mongoAggregation.setAggregatedCHO(agCHO);

    Map<String, List<String>> rights1 =
        FieldInputUtils.createResourceOrLiteralMapFromList(aggregation.getRightList());
    mongoAggregation.setDcRights(rights1);

    if (aggregation.getHasViewList() != null) {
      List<String> hasViewList = new ArrayList<>();
      for (HasView hasView : aggregation.getHasViewList()) {
        hasViewList.add(hasView.getResource().trim());
        boolean containsHasView = false;
        for (WebResourceImpl wr : webResources) {
          if (StringUtils.equals(wr.getAbout(), hasView.getResource().trim())) {
            containsHasView = true;
          }
        }
        if (!containsHasView && hasView.getResource().trim() != null) {
          WebResourceImpl wr = new WebResourceImpl();
          wr.setAbout(hasView.getResource().trim());
          webResources.add(wr);
        }
      }
      mongoAggregation.setHasView(hasViewList.toArray(new String[hasViewList.size()]));

    } else {
      mongoAggregation.setHasView(null);

    }
    if (webResources != null) {
      mongoAggregation.setWebResources(webResources);
    }

    return mongoAggregation;
  }
}
