package eu.europeana.normalization.dates;

import static java.util.function.Predicate.not;

import eu.europeana.normalization.dates.cleaning.CleanOperation;
import eu.europeana.normalization.dates.cleaning.CleanResult;
import eu.europeana.normalization.dates.cleaning.Cleaner;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfSerializer;
import eu.europeana.normalization.dates.edtf.EdtfValidator;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.dateextractors.DateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.DcmiPeriodDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBcAdDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBriefDateRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternCenturyDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDateExtractorYyyyMmDdSpacesDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDecadeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternFormatedFullDateDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternLongNegativeYearDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternMonthNameDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsDateExtractor;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.normalizers.InternalNormalizationReport;
import eu.europeana.normalization.normalizers.RecordNormalizeAction;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
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

  private static final XpathQuery PROXY_QUERY_CREATED = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("created"));

  private static final XpathQuery PROXY_QUERY_ISSUED = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("issued"));

  private static final XpathQuery PROXY_QUERY_TEMPORAL = getProxySubtagQuery(
      Namespace.DCTERMS.getElement("temporal"));

  private static final XpathQuery PROXY_QUERY_DATE = getProxySubtagQuery(
      Namespace.DC.getElement("date"));

  private static final XpathQuery PROXY_QUERY_COVERAGE = getProxySubtagQuery(
      Namespace.DC.getElement("coverage"));

  private static final XpathQuery PROXY_QUERY_SUBJECT = getProxySubtagQuery(
      Namespace.DC.getElement("subject"));

  private static final List<XpathQuery> DATE_PROPERTY_FIELDS = List.of(
      PROXY_QUERY_CREATED, PROXY_QUERY_ISSUED, PROXY_QUERY_TEMPORAL, PROXY_QUERY_DATE);

  private static final Namespace.Element EDM_TIMESPAN = Namespace.EDM.getElement("edm:TimeSpan");
  private static final Namespace.Element RDF_ABOUT = Namespace.RDF.getElement("rdf:about");
  private static final Namespace.Element SKOS_PREFLABEL = Namespace.SKOS.getElement("skos:prefLabel");
  private static final Namespace.Element XML_LANG = Namespace.XML.getElement("xml:lang");
  private static final Namespace.Element SKOS_NOTATION = Namespace.SKOS.getElement("skos:notation");
  private static final Namespace.Element SKOS_NOTE = Namespace.SKOS.getElement("skos:note");
  private static final Namespace.Element RDF_TYPE = Namespace.RDF.getElement("rdf:type");
  private static final Namespace.Element RDF_RESOURCE = Namespace.RDF.getElement("rdf:resource");
  private static final Namespace.Element EDM_BEGIN = Namespace.EDM.getElement("edm:begin");
  private static final Namespace.Element EDM_END = Namespace.EDM.getElement("edm:end");
  private static final Namespace.Element DCTERMS_ISPARTOF = Namespace.DCTERMS.getElement("dcterms:isPartOf");

  private static final List<XpathQuery> GENERIC_PROPERTY_FIELDS = List.of(PROXY_QUERY_COVERAGE, PROXY_QUERY_SUBJECT);
  private final Cleaner cleaner = new Cleaner();

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
        new PatternBriefDateRangeDateExtractor(),
        new PatternEdtfDateExtractor(),
        new PatternCenturyDateExtractor(),
        new PatternDecadeDateExtractor(),
        new PatternNumericDateRangeExtractorWithMissingPartsDateExtractor(),
        new PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor(),
        new PatternNumericDateExtractorWithMissingPartsDateExtractor(),
        new PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor(),
        new PatternDateExtractorYyyyMmDdSpacesDateExtractor(),
        new DcmiPeriodDateExtractor(),
        new PatternMonthNameDateExtractor(),
        new PatternFormatedFullDateDateExtractor(),
        new PatternBcAdDateExtractor(),
        new PatternLongNegativeYearDateExtractor());

    extractorsInOrderForGenericProperties = extractorsInOrderForDateProperties.stream().filter(
        not(PatternBriefDateRangeDateExtractor.class::isInstance)).collect(
        Collectors.toList());

    normalizationOperationsInOrderDateProperty = List.of(
        input -> normalizeInput(extractorsInOrderForDateProperties, input),
        input -> normalizeInput(extractorsInOrderForDateProperties, input, cleaner::clean1stTime),
        input -> normalizeInput(extractorsInOrderForDateProperties, input, cleaner::clean2ndTime));

    normalizationOperationsInOrderGenericProperty = List.of(
        input -> normalizeInput(extractorsInOrderForGenericProperties, input),
        input -> normalizeInput(extractorsInOrderForGenericProperties, input, cleaner::cleanGenericProperty));
  }

  private static XpathQuery getProxySubtagQuery(Namespace.Element subtag) {
    return new XpathQuery("/%s/%s[not(%s='true')]/%s", XpathQuery.RDF_TAG,
        Namespace.ORE.getElement("Proxy"), Namespace.EDM.getElement("europeanaProxy"), subtag);
  }

  @Override
  public NormalizationReport normalize(Document document) throws NormalizationException {

    final InternalNormalizationReport report = new InternalNormalizationReport();
    report.mergeWith(normalizeElements(document, DATE_PROPERTY_FIELDS, this::normalizeDateProperty));
    report.mergeWith(normalizeElements(document, GENERIC_PROPERTY_FIELDS, this::normalizeGenericProperty));

    return report;
  }

  private InternalNormalizationReport normalizeElements(Document document, List<XpathQuery> propertyFields,
      Function<String, DateNormalizationResult> normalizationFunction) throws NormalizationException {
    final InternalNormalizationReport report = new InternalNormalizationReport();
    for (XpathQuery query : propertyFields) {
      try {
        final List<Element> elements = XmlUtil.getAsElementList(query.execute(document));
        for (Element element : elements) {
          normalizeElement(document, element, normalizationFunction, report);
        }
      } catch (XPathExpressionException e) {
        throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
      }
    }
    return report;
  }

  private void normalizeElement(Document document, Element element,
      Function<String, DateNormalizationResult> normalizationFunction,
      InternalNormalizationReport report) throws NormalizationException {
    final String elementText = XmlUtil.getElementText(element);
    final DateNormalizationResult dateNormalizationResult = normalizationFunction.apply(elementText);
    if (dateNormalizationResult.getDateNormalizationExtractorMatchId() != DateNormalizationExtractorMatchId.NO_MATCH) {
      createTimespanEntity(document, dateNormalizationResult.getEdtfDate());
      report.increment(this.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
    }
  }

  // TODO: 25/07/2022 This can be made private
  public DateNormalizationResult normalizeDateProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderDateProperty,
        dateNormalizationResult -> false,
        CleanOperation::isApproximateCleanOperationIdForDateProperty,
        this::validateAndFix,
        this::noMatchIfValidAndTimeOnly);
  }

  // TODO: 21/07/2022 To check. This method does not do dates switching like the other method
  // TODO: 21/07/2022 Did not have unit tests originally.
  // TODO: 25/07/2022 This can be made private
  public DateNormalizationResult normalizeGenericProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderGenericProperty,
        dateNormalizationResult -> !dateNormalizationResult.isCompleteDate(),
        CleanOperation::isApproximateCleanOperationIdForGenericProperty,
        this::validate,
        dateNormalizationResult -> {//NOOP
        });
  }

  private DateNormalizationResult normalizeProperty(
      String input, final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrder,
      Predicate<DateNormalizationResult> extraCheckForNoMatch,
      Predicate<CleanOperation> checkIfApproximateCleanOperationId,
      Consumer<DateNormalizationResult> validationOperation,
      Consumer<DateNormalizationResult> postProcessingMatchId) {

    DateNormalizationResult dateNormalizationResult;
    String sanitizedInput = sanitizeCharacters(input);

    //Normalize trying operations in order
    dateNormalizationResult = normalizationOperationsInOrder
        .stream()
        .map(operation -> operation.apply(sanitizedInput))
        .filter(Objects::nonNull).findFirst().orElse(null);

    //Check if we have a match
    if (Objects.isNull(dateNormalizationResult) || extraCheckForNoMatch.test(dateNormalizationResult)) {
      return DateNormalizationResult.getNoMatchResult(input);
    } else {
      //Check if we did a clean operation and update approximate
      if (dateNormalizationResult.getCleanOperationMatchId() != null) {
        dateNormalizationResult.getEdtfDate().setApproximate(
            checkIfApproximateCleanOperationId.test(dateNormalizationResult.getCleanOperationMatchId()));
      }
      validationOperation.accept(dateNormalizationResult);
      postProcessingMatchId.accept(dateNormalizationResult);
      return dateNormalizationResult;
    }
  }

  private void noMatchIfValidAndTimeOnly(DateNormalizationResult dateNormalizationResult) {
    if (dateNormalizationResult.getDateNormalizationExtractorMatchId() != DateNormalizationExtractorMatchId.INVALID
        && dateNormalizationResult.getEdtfDate().isTimeOnly()) {
      // TODO: 21/07/2022 In the result only the match id is declared NO_MATCH but the contents are still present in the object. Is that okay?
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.NO_MATCH);
    }
  }

  private DateNormalizationResult normalizeInput(List<DateExtractor> dateExtractors, String input) {
    return dateExtractors.stream().map(dateExtractor -> dateExtractor.extract(input))
                         .filter(Objects::nonNull).findFirst().orElse(null);
  }

  private DateNormalizationResult normalizeInput(List<DateExtractor> dateExtractors, String input,
      Function<String, CleanResult> cleanFunction) {
    final CleanResult cleanedInput = cleanFunction.apply(input);
    DateNormalizationResult dateNormalizationResult = null;
    if (cleanedInput != null && StringUtils.isNotEmpty(cleanedInput.getCleanedValue())) {
      dateNormalizationResult = normalizeInput(dateExtractors, cleanedInput.getCleanedValue());
      if (dateNormalizationResult != null) {
        dateNormalizationResult.setCleanOperationMatchId(cleanedInput.getCleanOperation());
        // TODO: 21/07/2022 Perhaps this should be done differently, because it pollutes the map of the extractor and its id
        //Update the extractor match id.
        if (dateNormalizationResult.getDateNormalizationExtractorMatchId() == DateNormalizationExtractorMatchId.EDTF) {
          dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.EDTF_CLEANED);
        }
      }
    }
    return dateNormalizationResult;
  }

  private void validateAndFix(DateNormalizationResult dateNormalizationResult) {
    final AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
    if (!EdtfValidator.validate(edtfDate, false)) {
      switchAndValidate(dateNormalizationResult, edtfDate);
    }
  }

  private void validate(DateNormalizationResult dateNormalizationResult) {
    final AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
    if (!EdtfValidator.validate(edtfDate, false)) {
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.INVALID);
    }
  }

  private void switchAndValidate(DateNormalizationResult dateNormalizationResult, AbstractEdtfDate edtfDate) {
    if (edtfDate instanceof IntervalEdtfDate) {
      ((IntervalEdtfDate) edtfDate).switchStartWithEnd();
      if (!EdtfValidator.validate(edtfDate, false)) {
        //Revert the start/end
        ((IntervalEdtfDate) edtfDate).switchStartWithEnd();
        switchDayWithMonthAndValidate(dateNormalizationResult, edtfDate);
      }
    } else {
      switchDayWithMonthAndValidate(dateNormalizationResult, edtfDate);
    }
  }

  private void switchDayWithMonthAndValidate(DateNormalizationResult dateNormalizationResult, AbstractEdtfDate edtfDate) {
    edtfDate.switchDayAndMonth();
    if (EdtfValidator.validate(edtfDate, false)) {
      dateNormalizationResult.setEdtfDate(edtfDate);
    } else {
      edtfDate.switchDayAndMonth();
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.INVALID);
    }
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


  private void createTimespanEntity(Document document, AbstractEdtfDate edtfDate) throws NormalizationException {
    final String edtfDateString = EdtfSerializer.serialize(edtfDate);
    String uri;
    try {
      uri = String.format("#%s", URLEncoder.encode(edtfDateString, StandardCharsets.UTF_8.name()));
    } catch (UnsupportedEncodingException e) {
      throw new NormalizationException(e.getMessage(), e);
    }

    //Initialize timespan element
    final Element timeSpan = document.createElementNS(EDM_TIMESPAN.getNamespace().getUri(), EDM_TIMESPAN.getElementName());
    final Attr rdfAbout = document.createAttributeNS(RDF_ABOUT.getNamespace().getUri(), RDF_ABOUT.getElementName());
    rdfAbout.setValue(uri);
    timeSpan.setAttributeNode(rdfAbout);

    //skosPrefLabel
    final Element skosPrefLabel = document.createElementNS(SKOS_PREFLABEL.getNamespace().getUri(),
        SKOS_PREFLABEL.getElementName());
    final Attr skosPrefLabelLang = document.createAttributeNS(XML_LANG.getNamespace().getUri(), XML_LANG.getElementName());
    skosPrefLabel.setAttributeNode(skosPrefLabelLang);
    if (StringUtils.isNotBlank(edtfDate.getLabel())) {
      skosPrefLabel.setNodeValue(edtfDate.getLabel());
      skosPrefLabel.appendChild(document.createTextNode(edtfDate.getLabel()));
    } else {
      skosPrefLabelLang.setValue("zxx");
      skosPrefLabel.appendChild(document.createTextNode(uri));
    }
    timeSpan.appendChild(skosPrefLabel);

    //skosNote
    if (edtfDate.isApproximate()) {
      final Element skosNote = document.createElementNS(SKOS_NOTE.getNamespace().getUri(), SKOS_NOTE.getElementName());
      skosNote.appendChild(document.createTextNode("approximate"));
      timeSpan.appendChild(skosNote);
    }
    if (edtfDate.isUncertain()) {
      final Element skosNote = document.createElementNS(SKOS_NOTE.getNamespace().getUri(), SKOS_NOTE.getElementName());
      skosNote.appendChild(document.createTextNode("uncertain"));
      timeSpan.appendChild(skosNote);
    }

    //begin/end
    Integer startCentury = null;
    Integer endCentury = null;
    InstantEdtfDate firstDay = edtfDate.getFirstDay();
    InstantEdtfDate lastDay = edtfDate.getLastDay();
    Element edmBegin = null;
    Element edmEnd = null;
    if (firstDay != null) {
      edmBegin = document.createElementNS(EDM_BEGIN.getNamespace().getUri(), EDM_BEGIN.getElementName());
      edmBegin.appendChild(document.createTextNode(EdtfSerializer.serialize(firstDay)));
      startCentury = firstDay.getCentury();
    }
    if (lastDay != null) {
      edmEnd = document.createElementNS(EDM_END.getNamespace().getUri(), EDM_END.getElementName());
      edmEnd.appendChild(document.createTextNode(EdtfSerializer.serialize(lastDay)));
      endCentury = lastDay.getCentury();
    }

    //isPartOf
    // TODO: 25/07/2022 What if both are null, won't the for below fail?
    if (startCentury == null) {
      startCentury = endCentury;
    } else if (endCentury == null) {
      endCentury = startCentury;
    }
    for (int century = Math.max(1, startCentury); century <= Math.max(0, endCentury); century++) {
      final Element dctermsIsPartOf = document.createElementNS(DCTERMS_ISPARTOF.getNamespace().getUri(),
          DCTERMS_ISPARTOF.getElementName());
      final Attr dctermsIsPartOfResource = document.createAttributeNS(RDF_RESOURCE.getNamespace().getUri(),
          RDF_RESOURCE.getElementName());
      dctermsIsPartOfResource.setValue("http://data.europeana.eu/timespan/" + century);
      dctermsIsPartOf.setAttributeNode(dctermsIsPartOfResource);
      timeSpan.appendChild(dctermsIsPartOf);
    }

    if (edmBegin != null) {
      timeSpan.appendChild(edmBegin);
    }
    if (edmEnd != null) {
      timeSpan.appendChild(edmEnd);
    }

    //skosNotation
    final Element skosNotation = document.createElementNS(SKOS_NOTATION.getNamespace().getUri(), SKOS_NOTATION.getElementName());
    final Attr skosNotationType = document.createAttributeNS(RDF_TYPE.getNamespace().getUri(), RDF_TYPE.getElementName());
    skosNotationType.setValue("http://id.loc.gov/datatypes/edtf/EDTF-level1");
    skosNotation.setAttributeNode(skosNotationType);
    skosNotation.appendChild(document.createTextNode(uri));
    timeSpan.appendChild(skosNotation);

    document.getDocumentElement().appendChild(timeSpan);
  }
}
