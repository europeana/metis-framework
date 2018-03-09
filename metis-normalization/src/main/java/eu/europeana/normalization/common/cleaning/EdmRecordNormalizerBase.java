package eu.europeana.normalization.common.cleaning;

import java.util.Arrays;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.Namespace.Element;
import eu.europeana.normalization.util.XpathQuery;

public abstract class EdmRecordNormalizerBase implements ValueNormalization {

  private static final Element[] ELEMENTS_TO_QUERY = {Namespace.ORE.getElement("Proxy"),
      Namespace.ORE.getElement("Aggregation"), Namespace.EDM.getElement("WebResource"),
      Namespace.EDM.getElement("Agent"), Namespace.EDM.getElement("Place"),
      Namespace.EDM.getElement("Event"), Namespace.EDM.getElement("TimeSpan"),
      Namespace.EDM.getElement("PhysicalThing"), Namespace.SKOS.getElement("Concept")};

  private static final XpathQuery RECORD_NORMALIZATION_QUERY =
      XpathQuery.combine(Arrays.stream(ELEMENTS_TO_QUERY)
          .map(EdmRecordNormalizerBase::getRdfSubtagQuery).toArray(XpathQuery[]::new));

  private static final XpathQuery getRdfSubtagQuery(Element subtag) {
    return XpathQuery.create("/%s/%s/*", XpathQuery.RDF_TAG, subtag);
  }

  @Override
  public RecordNormalization toEdmRecordNormalizer() {
    return new ValueToRecordNormalizationWrapper(this, false, RECORD_NORMALIZATION_QUERY);
  }
}
