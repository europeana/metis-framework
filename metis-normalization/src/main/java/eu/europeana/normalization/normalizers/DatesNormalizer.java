package eu.europeana.normalization.normalizers;

import static eu.europeana.normalization.dates.DateNormalizationResultStatus.MATCHED;
import static eu.europeana.normalization.dates.DateNormalizationResultStatus.NO_MATCH;
import static java.util.function.Predicate.not;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.extractors.BcAdDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.BcAdRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.BriefRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.CenturyNumericDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.CenturyRomanDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.CenturyRomanRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.DateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.DcmiPeriodDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.DecadeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.EdtfDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.EdtfRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.LongNegativeYearDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.LongNegativeYearRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.MonthNameDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.NumericPartsDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.NumericPartsRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.extractors.PatternFormatedFullDateDateExtractor;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import eu.europeana.normalization.dates.sanitize.SanitizeOperation;
import eu.europeana.normalization.dates.sanitize.SanitizedDate;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The main class that implements the normalisation procedure.
 * <p>
 * It provides procedures for normalising values of properties that:
 *   <ul>
 *     <li>should contain date values.</li>
 *     <li>may contain dates as well as other kinds of entities (i.e., dc:subject and dc:coverage).</li>
 *   </ul>
 * </p>
 */
public class DatesNormalizer implements RecordNormalizeAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Namespace.Element EDM_PROVIDED_CHO = Namespace.EDM.getElement("ProvidedCHO");
  private static final Namespace.Element EDM_WEB_RESOURCE = Namespace.EDM.getElement("WebResource");
  private static final Namespace.Element EDM_AGENT = Namespace.EDM.getElement("Agent");
  private static final Namespace.Element EDM_PLACE = Namespace.EDM.getElement("Place");
  private static final Namespace.Element EDM_TIMESPAN = Namespace.EDM.getElement("TimeSpan");
  private static final Namespace.Element RDF_ABOUT = Namespace.RDF.getElement("about");
  private static final Namespace.Element SKOS_PREF_LABEL = Namespace.SKOS.getElement("prefLabel");
  private static final Namespace.Element XML_LANG = Namespace.XML.getElement("lang");
  private static final Namespace.Element SKOS_NOTATION = Namespace.SKOS.getElement("notation");
  private static final Namespace.Element SKOS_NOTE = Namespace.SKOS.getElement("note");
  private static final Namespace.Element RDF_DATATYPE = Namespace.RDF.getElement("datatype");
  private static final Namespace.Element RDF_RESOURCE = Namespace.RDF.getElement("resource");
  private static final Namespace.Element EDM_BEGIN = Namespace.EDM.getElement("begin");
  private static final Namespace.Element EDM_END = Namespace.EDM.getElement("end");
  private static final Namespace.Element DC_TERMS_IS_PART_OF = Namespace.DCTERMS.getElement("isPartOf");
  private static final Namespace.Element ORE_PROXY = Namespace.ORE.getElement("Proxy");
  private static final Namespace.Element EDM_EUROPEANA_PROXY = Namespace.EDM.getElement("europeanaProxy");

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_CREATED = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("created"));

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_ISSUED = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("issued"));

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_TEMPORAL = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("temporal"));

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_DATE = getProxySubtagQuery(
      Namespace.DC.getElement("date"));

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_COVERAGE = getProxySubtagQuery(
      Namespace.DC.getElement("coverage"));

  private static final Pair<Namespace.Element, XpathQuery> PROXY_QUERY_SUBJECT = getProxySubtagQuery(
      Namespace.DC.getElement("subject"));

  private static final List<Pair<Namespace.Element, XpathQuery>> DATE_PROPERTY_FIELDS = List.of(
      PROXY_QUERY_CREATED, PROXY_QUERY_ISSUED, PROXY_QUERY_TEMPORAL, PROXY_QUERY_DATE);
  private static final List<Pair<Namespace.Element, XpathQuery>> GENERIC_PROPERTY_FIELDS = List.of(
      PROXY_QUERY_COVERAGE, PROXY_QUERY_SUBJECT);

  private static final XpathQuery EUROPEANA_PROXY = new XpathQuery("/%s/%s[%s='true']",
      XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY);

  private final DateFieldSanitizer dateFieldSanitizer = new DateFieldSanitizer();

  private final List<DateExtractor> extractorsInOrderForDateProperties;
  private final List<DateExtractor> extractorsInOrderForGenericProperties;
  private final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrderDateProperty;
  private final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrderGenericProperty;

  /**
   * Default constructor.
   * <p>Initializes all the internal required properties</p>
   */
  public DatesNormalizer() {
    // The pattern PatternBriefDateRangeDateExtractor needs to be executed before the EDTF pattern.
    // Most values that match this pattern also match the EDTF pattern, but would result in an invalid date.
    // This pattern only matches values that would not be valid EDTF dates.
    extractorsInOrderForDateProperties = List.of(
        new BriefRangeDateExtractor(),
        new EdtfDateExtractor(),
        new EdtfRangeDateExtractor(),
        new CenturyNumericDateExtractor(),
        new CenturyRomanDateExtractor(),
        new CenturyRomanRangeDateExtractor(),
        new DecadeDateExtractor(),
        new NumericPartsRangeDateExtractor(),
        new NumericPartsDateExtractor(),
        new DcmiPeriodDateExtractor(),
        new MonthNameDateExtractor(),
        new PatternFormatedFullDateDateExtractor(),
        new BcAdDateExtractor(),
        new BcAdRangeDateExtractor(),
        new LongNegativeYearDateExtractor(),
        new LongNegativeYearRangeDateExtractor());

    extractorsInOrderForGenericProperties =
        extractorsInOrderForDateProperties.stream()
                                          .filter(not(BriefRangeDateExtractor.class::isInstance)).collect(Collectors.toList());

    normalizationOperationsInOrderDateProperty = List.of(
        input -> normalizeInput(extractorsInOrderForDateProperties, input),
        input -> normalizeInputSanitized(extractorsInOrderForDateProperties, input,
            dateFieldSanitizer::sanitize1stTimeDateProperty,
            SanitizeOperation::isApproximateSanitizeOperationForDateProperty,
            (dateExtractors, sanitizedDate) -> normalizeInput(dateExtractors, sanitizedDate.getSanitizedDateString())),
        input -> normalizeInputSanitized(extractorsInOrderForDateProperties, input,
            dateFieldSanitizer::sanitize2ndTimeDateProperty,
            SanitizeOperation::isApproximateSanitizeOperationForDateProperty,
            (dateExtractors, sanitizedDate) -> normalizeInput(dateExtractors, sanitizedDate.getSanitizedDateString())));

    normalizationOperationsInOrderGenericProperty = List.of(
        input -> normalizeInputGeneric(extractorsInOrderForGenericProperties, input),
        input -> normalizeInputSanitized(extractorsInOrderForGenericProperties, input,
            dateFieldSanitizer::sanitizeGenericProperty,
            SanitizeOperation::isApproximateSanitizeOperationForGenericProperty,
            (dateExtractors, sanitizedDate) -> normalizeInputGeneric(dateExtractors, sanitizedDate.getSanitizedDateString())));
  }

  private static Pair<Namespace.Element, XpathQuery> getProxySubtagQuery(Namespace.Element subtag) {
    return ImmutablePair.of(subtag, new XpathQuery("/%s/%s[not(%s='true')]/%s",
        XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY, subtag));
  }

  @Override
  public NormalizationReport normalize(Document document) throws NormalizationException {

    // Find the Europeana proxy.
    final Element europeanaProxy = XmlUtil.getUniqueElement(EUROPEANA_PROXY, document);

    // Perform the two different kinds of normalizations
    final InternalNormalizationReport report = new InternalNormalizationReport();
    report.mergeWith(normalizeElements(document, europeanaProxy, DATE_PROPERTY_FIELDS,
        this::normalizeDateProperty));
    report.mergeWith(normalizeElements(document, europeanaProxy, GENERIC_PROPERTY_FIELDS,
        this::normalizeGenericProperty));
    return report;
  }

  private InternalNormalizationReport normalizeElements(Document document, Element europeanaProxy,
      List<Pair<Namespace.Element, XpathQuery>> propertyFields,
      Function<String, DateNormalizationResult> normalizationFunction)
      throws NormalizationException {
    final InternalNormalizationReport report = new InternalNormalizationReport();
    for (Pair<Namespace.Element, XpathQuery> query : propertyFields) {
      try {
        final List<Element> elements = XmlUtil.getAsElementList(query.getRight().execute(document));
        for (Element element : elements) {
          normalizeElement(document, element, query.getLeft(), europeanaProxy,
              normalizationFunction, report);
        }
      } catch (XPathExpressionException e) {
        throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
      }
    }
    return report;
  }

  private void normalizeElement(Document document, Element element, Namespace.Element elementType,
      Element europeanaProxy, Function<String, DateNormalizationResult> normalizationFunction,
      InternalNormalizationReport report) {

    // Apply the normalization. If nothing can be done, we return.
    final String elementText = XmlUtil.getElementText(element);
    final DateNormalizationResult dateNormalizationResult = normalizationFunction.apply(elementText);
    if (dateNormalizationResult.getDateNormalizationResultStatus() == NO_MATCH) {
      return;
    }

    // Compute the timespan ID we need.
    final String timespanId = String.format("#%s", URLEncoder.encode(
        dateNormalizationResult.getEdtfDate().toString(), StandardCharsets.UTF_8));

    // Append the timespan to the document.
    appendTimespanEntity(document, dateNormalizationResult.getEdtfDate(), timespanId);

    // Add a reference to the timespan to the Europeana proxy. All elements we're adding
    // go at the beginning of the proxy in a choice, so the order doesn't matter.
    final Element reference = XmlUtil.createElement(elementType, europeanaProxy, List.of());
    final String fullResourceName = XmlUtil.getPrefixedElementName(RDF_RESOURCE,
        reference.lookupPrefix(RDF_RESOURCE.getNamespace().getUri()));
    final Attr dctermsIsPartOfResource = document.createAttributeNS(
        RDF_RESOURCE.getNamespace().getUri(), fullResourceName);
    dctermsIsPartOfResource.setValue(timespanId);
    reference.setAttributeNode(dctermsIsPartOfResource);

    // Update the report.
    report.increment(this.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
  }

  /**
   * Normalizer a property that is expected to be a date.
   *
   * @param input the date
   * @return the date normalization result
   */
  public DateNormalizationResult normalizeDateProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderDateProperty);
  }

  /**
   * Normalizer a property that is expected to be a generic property, so the process is more strict than properties that are
   * expected to be a date.
   *
   * @param input the date
   * @return the date normalization result
   */
  public DateNormalizationResult normalizeGenericProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderGenericProperty);
  }

  private DateNormalizationResult normalizeProperty(
      String input, final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrder) {

    DateNormalizationResult dateNormalizationResult;
    String sanitizedInput = sanitizeCharacters(input);

    //Normalize trying operations in order
    dateNormalizationResult = normalizationOperationsInOrder
        .stream()
        .map(operation -> operation.apply(sanitizedInput))
        .filter(result -> result.getDateNormalizationResultStatus() == MATCHED)
        .findFirst()
        .orElse(DateNormalizationResult.getNoMatchResult(input));

    return dateNormalizationResult;
  }

  private DateNormalizationResult normalizeInput(List<DateExtractor> dateExtractors, String inputDate) {
    return dateExtractors.stream().map(dateExtractor -> dateExtractor.extractDateProperty(inputDate))
                         .filter(dateNormalizationResult -> dateNormalizationResult.getDateNormalizationResultStatus()
                             == MATCHED).findFirst()
                         .orElse(DateNormalizationResult.getNoMatchResult(inputDate));
  }

  private DateNormalizationResult normalizeInputGeneric(List<DateExtractor> dateExtractors, String input) {
    return dateExtractors.stream().map(dateExtractor -> dateExtractor.extractGenericProperty(input))
                         .filter(dateNormalizationResult -> dateNormalizationResult.getDateNormalizationResultStatus()
                             == MATCHED).findFirst()
                         .orElse(DateNormalizationResult.getNoMatchResult(input));
  }

  private DateNormalizationResult normalizeInputSanitized(List<DateExtractor> dateExtractors, String input,
      Function<String, SanitizedDate> sanitizeFunction, Predicate<SanitizeOperation> checkIfApproximateCleanOperationId,
      BiFunction<List<DateExtractor>, SanitizedDate, DateNormalizationResult> normalizeFunction) {
    final SanitizedDate sanitizedDate = sanitizeFunction.apply(input);
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(input);
    if (sanitizedDate != null && StringUtils.isNotEmpty(sanitizedDate.getSanitizedDateString())) {
      final DateQualification dateQualification;
      if (checkIfApproximateCleanOperationId.test(sanitizedDate.getSanitizeOperation())) {
        dateQualification = DateQualification.APPROXIMATE;
      } else {
        dateQualification = DateQualification.NO_QUALIFICATION;
      }
      dateNormalizationResult = normalizeFunction.apply(dateExtractors, sanitizedDate);
      dateNormalizationResult = overwriteQualification(dateQualification, dateNormalizationResult);

      if (dateNormalizationResult.getDateNormalizationResultStatus() == MATCHED) {
        //Re-create result containing sanitization operation.
        dateNormalizationResult = new DateNormalizationResult(dateNormalizationResult, sanitizedDate.getSanitizeOperation());
      }
    }
    return dateNormalizationResult;
  }

  private static DateNormalizationResult overwriteQualification(DateQualification requestedDateQualification,
      DateNormalizationResult extractedDateNormalizationResult) {
    DateNormalizationResult dateNormalizationResult = extractedDateNormalizationResult;
    try {
      if (dateNormalizationResult.getDateNormalizationResultStatus() == MATCHED
          && requestedDateQualification != null && requestedDateQualification != DateQualification.NO_QUALIFICATION) {
        AbstractEdtfDate edtfDate = extractedDateNormalizationResult.getEdtfDate();
        if (edtfDate instanceof InstantEdtfDate) {
          edtfDate = new InstantEdtfDateBuilder((InstantEdtfDate) edtfDate)
              .withDateQualification(requestedDateQualification).build();
        } else if (edtfDate instanceof IntervalEdtfDate) {
          final InstantEdtfDate startEdtfDate = new InstantEdtfDateBuilder(((IntervalEdtfDate) edtfDate).getStart())
              .withDateQualification(requestedDateQualification).build();
          final InstantEdtfDate endEdtfDate;
          endEdtfDate = new InstantEdtfDateBuilder(((IntervalEdtfDate) edtfDate).getEnd())
              .withDateQualification(requestedDateQualification).build();
          edtfDate = new IntervalEdtfDateBuilder(startEdtfDate, endEdtfDate).withLabel(edtfDate.getLabel()).build();
        }
        dateNormalizationResult = new DateNormalizationResult(
            extractedDateNormalizationResult.getDateNormalizationExtractorMatchId(),
            extractedDateNormalizationResult.getOriginalInput(), edtfDate);
      }
    } catch (DateExtractionException e) {
      LOGGER.debug("This should not happen since the date provided should be a valid one.", e);
      dateNormalizationResult = DateNormalizationResult.getNoMatchResult(extractedDateNormalizationResult.getOriginalInput());
    }
    return dateNormalizationResult;
  }

  /**
   * Cleans and normalizes specific characters.
   * <p>
   * Specifically it will in order:
   *   <ul>
   *     <li>Trim the input</li>
   *     <li>Replace non-breaking spaces with normal spaces</li>
   *     <li>Replace en dash by a normal dash</li>
   *   </ul>
   * </p>
   *
   * @param input the string input
   * @return the normalized string
   */
  private static String sanitizeCharacters(String input) {
    String valTrim = input.trim();
    valTrim = valTrim.replace('\u00a0', ' '); // replace non-breaking spaces by normal spaces
    valTrim = valTrim.replace('\u2013', '-'); // replace en dash by normal dash
    return valTrim;
  }

  private void appendTimespanEntity(Document document, AbstractEdtfDate edtfDate, String timespanId) {

    //Check if element with the same id already exists, if so we need to remove it first.
    List<Element> elements = XmlUtil.getAsElementList(document.getDocumentElement()
                                                              .getElementsByTagNameNS(EDM_TIMESPAN.getNamespace().getUri(),
                                                                  EDM_TIMESPAN.getElementName()));
    for (Element element : elements) {
      String aboutValue = element.getAttributeNS(RDF_ABOUT.getNamespace().getUri(), RDF_ABOUT.getElementName());
      if (timespanId.equals(aboutValue)) {
        document.getDocumentElement().removeChild(element);
      }
    }

    // TODO: 09/08/2022 All the element prefixes below are searched first and if not found then the suggested prefix is added.
    //  When it does not exist in the root the namespace is added in the element itself.
    //  Should we be adding it in the root element of the document instead?
    // Create and add timespan element to document (RDF).
    final Element timeSpan = XmlUtil.createElement(EDM_TIMESPAN, document.getDocumentElement(),
        List.of(EDM_PROVIDED_CHO, EDM_AGENT, EDM_PLACE, EDM_WEB_RESOURCE, EDM_TIMESPAN));
    final String fullRdfAboutName = XmlUtil.getPrefixedElementName(RDF_ABOUT,
        document.getDocumentElement().lookupPrefix(RDF_ABOUT.getNamespace().getUri()));
    final Attr rdfAbout = document.createAttributeNS(RDF_ABOUT.getNamespace().getUri(), fullRdfAboutName);
    rdfAbout.setValue(timespanId);
    timeSpan.setAttributeNode(rdfAbout);

    // Create and add skosPrefLabel to timespan
    final Element skosPrefLabel = XmlUtil.createElement(SKOS_PREF_LABEL, timeSpan, null);
    if (StringUtils.isNotBlank(edtfDate.getLabel())) {
      skosPrefLabel.setNodeValue(edtfDate.getLabel());
      skosPrefLabel.appendChild(document.createTextNode(edtfDate.getLabel()));
    } else {
      final String fullLangName = XmlUtil.getPrefixedElementName(XML_LANG,
          timeSpan.lookupPrefix(XML_LANG.getNamespace().getUri()));
      final Attr skosPrefLabelLang = document.createAttributeNS(XML_LANG.getNamespace().getUri(), fullLangName);
      skosPrefLabel.setAttributeNode(skosPrefLabelLang);
      skosPrefLabelLang.setValue("zxx");
      skosPrefLabel.appendChild(document.createTextNode(edtfDate.toString()));
    }

    // Create and add skosNote elements to timespan in case of approximate or uncertain dates.
    if (edtfDate.getDateQualification() == DateQualification.APPROXIMATE) {
      final Element skosNote = XmlUtil.createElement(SKOS_NOTE, timeSpan, null);
      skosNote.appendChild(document.createTextNode("approximate"));
    }
    if (edtfDate.getDateQualification() == DateQualification.UNCERTAIN) {
      final Element skosNote = XmlUtil.createElement(SKOS_NOTE, timeSpan, null);
      skosNote.appendChild(document.createTextNode("uncertain"));
    }

    // Compute the date range and century range.
    final InstantEdtfDate firstDay = edtfDate.getFirstDay();
    final InstantEdtfDate lastDay = edtfDate.getLastDay();
    Integer startCentury = Optional.ofNullable(firstDay)
                                   .map(InstantEdtfDate::getCentury).orElse(null);
    Integer endCentury = Optional.ofNullable(lastDay)
                                 .map(InstantEdtfDate::getCentury).orElse(null);

    // TODO: 25/07/2022 What if both are null, won't the 'for' loop below fail? Is there always at least one century?
    //At this point, everything should be valid so that is not possible.
    //For a sanity check perhaps we can check for that case and throw an exception if that happens.
    //Or the date objects, as before, are by their instantiation validated.
    if (startCentury == null) {
      startCentury = endCentury;
    } else if (endCentury == null) {
      endCentury = startCentury;
    }

    // Create and add the isPartOf
    final String fullResourceName = XmlUtil.getPrefixedElementName(RDF_RESOURCE,
        timeSpan.lookupPrefix(RDF_RESOURCE.getNamespace().getUri()));
    for (int century = Math.max(1, startCentury); century <= Math.max(0, endCentury); century++) {
      final Element dctermsIsPartOf = XmlUtil.createElement(DC_TERMS_IS_PART_OF, timeSpan, null);
      final Attr dctermsIsPartOfResource = document.createAttributeNS(RDF_RESOURCE.getNamespace().getUri(), fullResourceName);
      dctermsIsPartOfResource.setValue("http://data.europeana.eu/timespan/" + century);
      dctermsIsPartOf.setAttributeNode(dctermsIsPartOfResource);
    }

    // Create and add the begin and end.
    if (firstDay != null) {
      final Element edmBegin = XmlUtil.createElement(EDM_BEGIN, timeSpan, null);
      edmBegin.appendChild(document.createTextNode(firstDay.toString()));
    }
    if (lastDay != null) {
      final Element edmEnd = XmlUtil.createElement(EDM_END, timeSpan, null);
      edmEnd.appendChild(document.createTextNode(lastDay.toString()));
    }

    // Create and add skosNotation
    final Element skosNotation = XmlUtil.createElement(SKOS_NOTATION, timeSpan, null);
    final String fullNotationTypeName = XmlUtil.getPrefixedElementName(RDF_DATATYPE,
        timeSpan.lookupPrefix(RDF_DATATYPE.getNamespace().getUri()));
    final Attr skosNotationType = document.createAttributeNS(RDF_DATATYPE.getNamespace().getUri(), fullNotationTypeName);
    skosNotationType.setValue("http://id.loc.gov/datatypes/edtf/EDTF-level1");
    skosNotation.setAttributeNode(skosNotationType);
    skosNotation.appendChild(document.createTextNode(edtfDate.toString()));
  }
}
