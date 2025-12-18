package eu.europeana.metis.harvesting.oaipmh;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterRuntimeException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.file.CloseableIterator;
import io.gdcc.xoai.model.oaipmh.results.record.Header;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.serviceprovider.ServiceProvider;
import io.gdcc.xoai.serviceprovider.exceptions.BadArgumentException;
import io.gdcc.xoai.serviceprovider.exceptions.OAIRequestException;
import io.gdcc.xoai.serviceprovider.model.Context;
import io.gdcc.xoai.serviceprovider.parameters.ListIdentifiersParameters;
import io.gdcc.xoai.serviceprovider.parameters.Parameters;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Iterator for harvesting. It wraps a source iterator and provides additional closing functionality for the connection client.
 *
 * @param <R> The type of data returned by the iterator after post-processing the headers.
 */
public class OaiHarvestingIterator<R> implements HarvestingIterator<R, OaiRecordHeader> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String COMPLETE_LIST_SIZE_XPATH =
      "/*[local-name()='OAI-PMH']" +
          "/*[local-name()='ListIdentifiers']" +
          "/*[local-name()='resumptionToken']";
  public static final String COMPLETE_LIST_SIZE = "completeListSize";
  private Iterator<Header> source = null;

  private final CloseableOaiClient oaiClient;
  private final OaiHarvest harvest;
  private final RecordOaiHeaderPostProcessing<R> postProcessing;

  /**
   * Constructor.
   *
   * @param oaiClient The client to close when the iterator is closed.
   * @param harvest The harvest request to execute.
   * @param recordOaiHeaderPostProcessing post processing after harvested header.
   */
  protected OaiHarvestingIterator(CloseableOaiClient oaiClient, OaiHarvest harvest,
      RecordOaiHeaderPostProcessing<R> recordOaiHeaderPostProcessing) {
    this.oaiClient = oaiClient;
    this.harvest = harvest;
    this.postProcessing = recordOaiHeaderPostProcessing;
  }

  private Iterator<Header> getOrCreateSource() throws HarvesterException {
    if (this.source != null) {
      return source;
    }
    final ListIdentifiersParameters parameters = prepareListIdentifiersParameters(harvest);
    try {
      this.source = new ServiceProvider(new Context().withOAIClient(this.oaiClient))
          .listIdentifiers(parameters);
    } catch (RuntimeException | BadArgumentException e) {
      try {
        this.close();
      } catch (IOException ioException) {
        LOGGER.info("Could not close connection client.", ioException);
      }
      throw new HarvesterException(e.getMessage(), e);
    }
    return this.source;
  }

  private static ListIdentifiersParameters prepareListIdentifiersParameters(OaiHarvest harvest) {
    ListIdentifiersParameters parameters = ListIdentifiersParameters.request()
                                                                    .withMetadataPrefix(harvest.getMetadataPrefix());
    if (harvest.getFrom() != null) {
      parameters.withFrom(harvest.getFrom());
    }
    if (harvest.getUntil() != null) {
      parameters.withUntil(harvest.getUntil());
    }
    if (harvest.getSetSpec() != null) {
      parameters.withSetSpec(harvest.getSetSpec());
    }
    return parameters;
  }

  @Override
  public void forEachFiltered(ReportingIteration<R> action,
      Predicate<OaiRecordHeader> filter) throws HarvesterException {
    forEachWithPostProcessing(action, postProcessing, filter);
  }

  private <O> void forEachWithPostProcessing(final ReportingIteration<O> action,
      final RecordOaiHeaderPostProcessing<O> postProcessing, final Predicate<OaiRecordHeader> filter)
      throws HarvesterException {
    final SingleIteration<O> singleIteration = new SingleIteration<>(filter, postProcessing, action, oaiClient, harvest);
    try {
      while (getOrCreateSource().hasNext()) {
        final Header header = Optional.of(getOrCreateSource().next())
                                      .orElseThrow(() -> new HarvesterException("Unexpected null header."));
        if (!singleIteration.process(header)) {
          break;
        }
      }
    } catch (RuntimeException e) {
      throw new HarvesterException("Problem while iterating through OAI headers.", e);
    }
  }

  @Override
  public void forEachNonDeleted(ReportingIteration<R> action) throws HarvesterException {
    forEachFiltered(action, Predicate.not(OaiRecordHeader::isDeleted));
  }

  @Override
  public Integer countRecords() throws HarvesterException {
    final Parameters parameters = Parameters.parameters().withVerb(Verb.Type.ListIdentifiers)
                                            .include(prepareListIdentifiersParameters(harvest));
    try (final InputStream listIdentifiersResponse = performThrowingFunction(oaiClient,
        client -> client.execute(parameters))) {
      return readCompleteListSizeFromXML(listIdentifiersResponse);
    } catch (OAIRequestException | IOException e) {
      throw new HarvesterException(String.format(
          "Problem with counting records for endpoint %1$s because of: %2$s",
          harvest.getRepositoryUrl(), e.getMessage()), e);
    }
  }

  @Override
  public void close() throws IOException {
    this.oaiClient.close();
  }

  @Override
  public CloseableIterator<R> getCloseableIterator() {
    try {
      Iterator<Header> headerIterator = getOrCreateSource();
      return new CloseableIterator<>() {
        @Override
        public boolean hasNext() {
          return headerIterator.hasNext();
        }

        @Override
        public R next() {
          Header header = headerIterator.next();
          try {
            OaiRecordHeader oaiHeader = OaiRecordHeader.convert(header);
            return postProcessing.apply(oaiHeader, oaiClient, harvest);
          } catch (HarvesterException e) {
            throw new HarvesterRuntimeException("Error during OAI iteration", e);
          }
        }

        @Override
        public void close() {
          //Nothing to do.
        }
      };
    } catch (HarvesterException e) {
      throw new HarvesterRuntimeException("Cannot create iterator", e);
    }
  }

  private static Integer readCompleteListSizeFromXML(InputStream stream) throws HarvesterException {
    final XPathExpression expr;
    try {
      final XPathFactory xpathFactory = XPathFactory.newInstance();
      xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final XPath xpath = xpathFactory.newXPath();
      expr = xpath.compile(COMPLETE_LIST_SIZE_XPATH);
    } catch (XPathExpressionException | XPathFactoryConfigurationException e) {
      throw new HarvesterException("Cannot compile xpath expression.", e);
    }
    try {
      final Node resumptionTokenNode = (Node) expr
          .evaluate(new InputSource(stream), XPathConstants.NODE);
      if (resumptionTokenNode != null) {
        final Node node = resumptionTokenNode.getAttributes().getNamedItem(COMPLETE_LIST_SIZE);
        if (node != null) {
          return Integer.valueOf(node.getNodeValue());
        }
      }
    } catch (NumberFormatException | XPathExpressionException e) {
      LOGGER.debug("Cannot read completeListSize from OAI response ", e);
    }
    return null;
  }

  /**
   * Functional interface for record oai header post-processing.
   *
   * @param <O> the return value of the function
   */
  @FunctionalInterface
  public interface RecordOaiHeaderPostProcessing<O> {

    /**
     * Applies this function to the given arguments.
     *
     * @param oaiRecordHeader the oai record header
     * @param oaiClient the oai client
     * @param oaiHarvest the oai harvest
     * @return the result of the function
     * @throws HarvesterException if something went wrong
     */
    O apply(OaiRecordHeader oaiRecordHeader, CloseableOaiClient oaiClient, OaiHarvest oaiHarvest) throws HarvesterException;
  }

  private record SingleIteration<O>(Predicate<OaiRecordHeader> filter,
                                    RecordOaiHeaderPostProcessing<O> recordOaiHeaderPostProcessing,
                                    ReportingIteration<O> action, CloseableOaiClient oaiClient, OaiHarvest harvest) {

    /**
     * Process provided header and return.
     *
     * @param header the header
     * @return the iteration result indicating whether there is more items available.
     * @throws HarvesterException if something went wrong
     */
    public boolean process(Header header) throws HarvesterException {
      final OaiRecordHeader oaiRecordHeader = OaiRecordHeader.convert(header);
      if (filter.test(oaiRecordHeader)) {
        final O postProcessResult = Optional.ofNullable(recordOaiHeaderPostProcessing.apply(oaiRecordHeader, oaiClient, harvest))
                                            .orElseThrow(() -> new HarvesterException("Post processing result cannot be null."));
        try {
          return action.process(postProcessResult);
        } catch (IOException e) {
          throw new HarvesterException("Problem while processing: " + oaiRecordHeader.getOaiIdentifier(), e);
        }
      }
      return true;
    }
  }
}
