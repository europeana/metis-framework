package eu.europeana.metis.data.checker.service.executor;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.data.checker.common.model.DatasetProperties;
import eu.europeana.metis.transformation.service.EuropeanaGeneratedIdsMap;
import eu.europeana.metis.transformation.service.EuropeanaIdCreator;
import eu.europeana.metis.transformation.service.EuropeanaIdException;
import eu.europeana.metis.transformation.service.TransformationException;
import eu.europeana.metis.transformation.service.XsltTransformer;
import eu.europeana.validation.model.ValidationResult;

/**
 * Task for the multi-threaded implementation of the validation service Created by ymamakis on
 * 9/23/16.
 */
public class ValidationTask implements Callable<ValidationTaskResult> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTask.class);

  private final ValidationUtils validationUtils;
  private final boolean applyTransformation;
  private final IBindingFactory bFact;
  private final String incomingRecord;
  private final DatasetProperties datasetProperties;

  /**
   * Default constructor of the validation service
   *
   * @param validationUtils Utils class for validation tasks
   * @param applyTransformation Whether the record needs to be transformed
   * @param bFact The JibX binding factory for the conversion of the XML to RDF class
   * @param incomingRecord The record to be validated and transformed
   * @param datasetProperties The dataset properties that need to be enforced.
   */
  public ValidationTask(ValidationUtils validationUtils, boolean applyTransformation,
      IBindingFactory bFact, String incomingRecord, DatasetProperties datasetProperties) {
    this.validationUtils = validationUtils;
    this.applyTransformation = applyTransformation;
    this.bFact = bFact;
    this.incomingRecord = incomingRecord;
    this.datasetProperties = datasetProperties;
  }

  /**
   * Execution of transformation, id-generation and validation for Europeana Data Checker Service
   */
  @Override
  public ValidationTaskResult call()
      throws JiBXException, IndexingException, EuropeanaIdException, TransformationException {
    try {
      return invoke();
    } catch (JiBXException | IndexingException | EuropeanaIdException | TransformationException e) {
      LOGGER.error("An error occurred while processing", e);
      throw e;
    }
  }

  private ValidationTaskResult invoke()
      throws JiBXException, IndexingException, EuropeanaIdException, TransformationException {

    // Validate the data.
    final ValidationResult validationResult;
    if (applyTransformation) {
      validationResult = validationUtils.validateRecordBeforeTransformation(incomingRecord);
    } else {
      validationResult = validationUtils.validateRecordAfterTransformation(incomingRecord);
    }

    // If validation failed, report this to the user.
    if (!validationResult.isSuccess()) {
      return new ValidationTaskResult(null, validationResult, false);
    }

    // Transform the data if necessary, otherwise set the changed id properties.
    final RDF rdf;
    if (applyTransformation) {
      rdf = convertToRdf(transformRecord());
    } else {
      rdf = convertToRdf(incomingRecord);
      setDatasetProperties(rdf);
    }

    // Publish/index the data.
    validationUtils.persist(rdf);

    // Return the result.
    final String recordId = rdf.getProvidedCHOList().get(0).getAbout();
    return new ValidationTaskResult(recordId, validationResult, true);
  }

  private RDF convertToRdf(String rdfString) throws JiBXException {
    final IUnmarshallingContext uctx = bFact.createUnmarshallingContext();
    return (RDF) uctx.unmarshalDocument(new StringReader(rdfString));
  }

  private String transformRecord() throws EuropeanaIdException, TransformationException {
    final EuropeanaGeneratedIdsMap europeanaGeneratedIdsMap = new EuropeanaIdCreator()
        .constructEuropeanaId(incomingRecord, datasetProperties.getDatasetId());
    final XsltTransformer transformer =
        validationUtils.createTransformer(datasetProperties.getDatasetName(),
            datasetProperties.getEdmCountry(), datasetProperties.getEdmLanguage());
    return transformer
        .transform(incomingRecord.getBytes(StandardCharsets.UTF_8), europeanaGeneratedIdsMap)
        .toString();
  }

  private static <T> Stream<T> stream(Collection<T> collection) {
    return collection == null ? Stream.empty() : collection.stream();
  }

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() != null && proxy.getEuropeanaProxy().isEuropeanaProxy();
  }

  private static boolean isProviderProxy(ProxyType proxy) {
    return !isEuropeanaProxy(proxy);
  }

  private void setDatasetProperties(RDF rdf) throws EuropeanaIdException {

    // Generate a new ID for the record and set it.
    final EuropeanaGeneratedIdsMap ids = new EuropeanaIdCreator()
        .constructEuropeanaId(incomingRecord, datasetProperties.getDatasetId());
    rdf.getProvidedCHOList().get(0).setAbout(ids.getEuropeanaGeneratedId());
    stream(rdf.getAggregationList()).map(Aggregation::getAggregatedCHO).filter(Objects::nonNull)
        .forEach(cho -> cho.setResource(ids.getEuropeanaGeneratedId()));
    stream(rdf.getEuropeanaAggregationList()).map(EuropeanaAggregationType::getAggregatedCHO)
        .filter(Objects::nonNull).forEach(cho -> cho.setResource(ids.getEuropeanaGeneratedId()));
    stream(rdf.getProxyList()).map(ProxyType::getProxyFor).filter(Objects::nonNull)
        .forEach(proxyFor -> proxyFor.setResource(ids.getEuropeanaGeneratedId()));

    // Set the derived aggregation IDs
    stream(rdf.getProxyList()).filter(ValidationTask::isEuropeanaProxy)
        .map(ProxyType::getProxyInList).filter(Objects::nonNull).flatMap(List::stream)
        .forEach(proxyIn -> proxyIn.setResource(ids.getEuropeanaAggregationAboutPrefixed()));
    stream(rdf.getEuropeanaAggregationList())
        .forEach(aggregation -> aggregation.setAbout(ids.getEuropeanaAggregationAboutPrefixed()));
    stream(rdf.getProxyList()).filter(ValidationTask::isProviderProxy)
        .map(ProxyType::getProxyInList).filter(Objects::nonNull).flatMap(List::stream)
        .forEach(proxyIn -> proxyIn.setResource(ids.getAggregationAboutPrefixed()));
    stream(rdf.getAggregationList())
        .forEach(aggregation -> aggregation.setAbout(ids.getAggregationAboutPrefixed()));

    // Set the derived proxy IDs
    stream(rdf.getProxyList()).filter(ValidationTask::isEuropeanaProxy)
        .forEach(proxy -> proxy.setAbout(ids.getEuropeanaProxyAboutPrefixed()));
    stream(rdf.getProxyList()).filter(ValidationTask::isProviderProxy)
        .forEach(proxy -> proxy.setAbout(ids.getProxyAboutPrefixed()));

    // Set the dc:identifier in the Europeana proxies.
    stream(rdf.getProxyList()).filter(ValidationTask::isEuropeanaProxy)
        .map(ProxyType::getChoiceList).filter(Objects::nonNull).flatMap(List::stream)
        .filter(Choice::ifIdentifier).map(Choice::getIdentifier)
        .forEach(identifier -> identifier.setString(ids.getEuropeanaGeneratedId()));
  }
}
