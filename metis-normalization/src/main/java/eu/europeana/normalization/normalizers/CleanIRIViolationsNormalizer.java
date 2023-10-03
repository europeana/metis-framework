package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XpathQuery;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.Violation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This normalizer cleans, or 'escapes', media link values, replacing all IRI Violation characters
 * with their respective encoding character.
 *
 * @author Joana Sousa
 */

public class CleanIRIViolationsNormalizer implements ValueNormalizeAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Namespace.Element RDF_RESOURCE = Namespace.RDF.getElement("resource");
  private static final Namespace.Element RDF_ABOUT = Namespace.RDF.getElement("about");
  private static final Namespace.Element ORE_AGGREGATION = Namespace.ORE.getElement("Aggregation");
  private static final Namespace.Element EDM_IS_SHOWN_BY = Namespace.EDM.getElement("isShownBy");
  private static final Namespace.Element EDM_IS_SHOWN_AT = Namespace.EDM.getElement("isShownAt");
  private static final Namespace.Element EDM_OBJECT = Namespace.EDM.getElement("object");
  private static final Namespace.Element EDM_HAS_VIEW = Namespace.EDM.getElement("hasView");
  private static final Namespace.Element EDM_WEB_RESOURCE = Namespace.EDM.getElement("WebResource");

  private static final XpathQuery RESOURCE_IS_SHOWN_BY_QUERY = createResourceQueries(EDM_IS_SHOWN_BY);

  private static final XpathQuery RESOURCE_HAS_VIEW_QUERY = createResourceQueries(EDM_HAS_VIEW);

  private static final XpathQuery RESOURCE_IS_SHOWN_AT_QUERY = createResourceQueries(EDM_IS_SHOWN_AT);

  private static final XpathQuery RESOURCE_OBJECT_QUERY = createResourceQueries(EDM_OBJECT);

  private static final XpathQuery WEB_RESOURCE_ABOUT_QUERY = new XpathQuery(
      "/%s/%s/@%s", XpathQuery.RDF_TAG, EDM_WEB_RESOURCE, RDF_ABOUT);

  private final IRIFactory iriFactory = IRIFactory.iriImplementation();
  private IRI iri = null;


  @Override
  public RecordNormalizeAction getAsRecordNormalizer() {
    return new ValueNormalizeActionWrapper(this, RESOURCE_IS_SHOWN_BY_QUERY,
        RESOURCE_HAS_VIEW_QUERY, RESOURCE_IS_SHOWN_AT_QUERY, RESOURCE_OBJECT_QUERY,
        WEB_RESOURCE_ABOUT_QUERY);
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String value) {
    List<NormalizedValueWithConfidence> result = new ArrayList<>();

    if (StringUtils.isNotBlank(value)) {
      iri = iriFactory.create(value.trim());
      final String normalizedValue;

      try {
        normalizedValue = iri.toURI().toString();
        result = Collections.singletonList(new NormalizedValueWithConfidence(normalizedValue, 1));
      } catch (URISyntaxException e) {
        LOGGER.debug("There was some trouble normalizing the value for IRI Violation", e);
      }
    }
    return result;
  }

  /**
   * This method checks the violations the previously given value to the IRIFactory contains
   *
   * @return An iterator that contains the violations the IRIFactory detected. It returns the
   * violations without any warnings related to each element.
   */
  Iterator<Violation> getViolations() {
    return (iri == null) ? Collections.emptyIterator() : iri.violations(false);
  }

  private static XpathQuery createResourceQueries(Namespace.Element edmValue) {
    return new XpathQuery("/%s/%s/%s/@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, edmValue,
        RDF_RESOURCE);
  }

}
