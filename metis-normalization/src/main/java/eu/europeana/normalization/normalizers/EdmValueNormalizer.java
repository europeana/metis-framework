package eu.europeana.normalization.normalizers;

import java.util.Arrays;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.Namespace.Element;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This class represents a normalizer that normalizes values in EDM tags an EDM DOM tree.
 */
public abstract class EdmValueNormalizer implements ValueNormalizeAction {

  private static final Element[] ELEMENTS_TO_QUERY = {Namespace.ORE.getElement("Proxy"),
      Namespace.ORE.getElement("Aggregation"), Namespace.EDM.getElement("WebResource"),
      Namespace.EDM.getElement("Agent"), Namespace.EDM.getElement("Place"),
      Namespace.EDM.getElement("Event"), Namespace.EDM.getElement("TimeSpan"),
      Namespace.EDM.getElement("PhysicalThing"), Namespace.SKOS.getElement("Concept")};

  private static final XpathQuery RECORD_NORMALIZATION_QUERY =
      XpathQuery.combine(Arrays.stream(ELEMENTS_TO_QUERY).map(EdmValueNormalizer::getRdfSubtagQuery)
          .toArray(XpathQuery[]::new));

  private static final XpathQuery getRdfSubtagQuery(Element subtag) {
    return XpathQuery.create("/%s/%s/*", XpathQuery.RDF_TAG, subtag);
  }

  @Override
  public RecordNormalizeAction getAsRecordNormalizer() {
    return new ValueNormalizeActionWrapper(this, RECORD_NORMALIZATION_QUERY);
  }
}
