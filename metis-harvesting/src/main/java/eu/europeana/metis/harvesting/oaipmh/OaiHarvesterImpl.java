package eu.europeana.metis.harvesting.oaipmh;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.commons.io.IOUtils;
import org.dspace.xoai.model.oaipmh.Header;
import org.dspace.xoai.model.oaipmh.Verb;
import org.dspace.xoai.serviceprovider.ServiceProvider;
import org.dspace.xoai.serviceprovider.exceptions.BadArgumentException;
import org.dspace.xoai.serviceprovider.exceptions.OAIRequestException;
import org.dspace.xoai.serviceprovider.model.Context;
import org.dspace.xoai.serviceprovider.parameters.GetRecordParameters;
import org.dspace.xoai.serviceprovider.parameters.ListIdentifiersParameters;
import org.dspace.xoai.serviceprovider.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This class provides an implementation of the {@link OaiHarvester} functionality.
 */
public class OaiHarvesterImpl implements OaiHarvester {

  private static final Logger LOGGER = LoggerFactory.getLogger(OaiHarvesterImpl.class);

  private static final String COMPLETE_LIST_SIZE_XPATH =
          "/*[local-name()='OAI-PMH']" +
                  "/*[local-name()='ListIdentifiers']" +
                  "/*[local-name()='resumptionToken']";
  public static final String COMPLETE_LIST_SIZE = "completeListSize";

  private final ConnectionClientFactory connectionClientFactory;

  /**
   * Constructor.
   *
   * @param connectionClientFactory A factory for connection clients.
   */
  public OaiHarvesterImpl(ConnectionClientFactory connectionClientFactory) {
    this.connectionClientFactory = connectionClientFactory;
  }

  @Override
  public OaiRecordHeaderIterator harvestRecordHeaders(OaiHarvest harvest)
          throws HarvesterException {
    final ListIdentifiersParameters parameters = prepareListIdentifiersParameters(harvest);
    final Iterator<Header> iterator;
    final CloseableOaiClient client = connectionClientFactory.createConnectionClient(
            harvest.getRepositoryUrl());
    try {
      iterator = new ServiceProvider(new Context().withOAIClient(client))
              .listIdentifiers(parameters);
    } catch (RuntimeException | BadArgumentException e) {
      try {
        client.close();
      } catch (IOException ioException) {
        LOGGER.info("Could not close connection client.", ioException);
      }
      throw new HarvesterException(e.getMessage(), e);
    }
    return new HeaderIterator(iterator, client);
  }

  private static ListIdentifiersParameters prepareListIdentifiersParameters(OaiHarvest harvest) {
    ListIdentifiersParameters parameters = ListIdentifiersParameters.request()
            .withMetadataPrefix(harvest.getMetadataPrefix());
    if (harvest.getFrom() != null) {
      parameters.withFrom(Date.from(harvest.getFrom()));
    }
    if (harvest.getUntil() != null) {
      parameters.withUntil(Date.from(harvest.getUntil()));
    }
    if (harvest.getSetSpec() != null) {
      parameters.withSetSpec(harvest.getSetSpec());
    }
    return parameters;
  }

  @Override
  public InputStream harvestRecord(OaiRepository repository, String oaiIdentifier)
          throws HarvesterException {
    final GetRecordParameters getRecordParameters = GetRecordParameters.request()
            .withIdentifier(oaiIdentifier).withMetadataFormatPrefix(repository.getMetadataPrefix());
    final Parameters parameters = Parameters.parameters().withVerb(Verb.Type.GetRecord)
            .include(getRecordParameters);
    final String record;
    try (final CloseableOaiClient oaiClient = connectionClientFactory
            .createConnectionClient(repository.getRepositoryUrl());
            final InputStream recordStream = performThrowingFunction(oaiClient,
                    client -> client.execute(parameters))) {
      record = IOUtils.toString(recordStream, StandardCharsets.UTF_8);
    } catch (OAIRequestException | IOException e) {
      throw new HarvesterException(String.format(
              "Problem with harvesting record %1$s for endpoint %2$s because of: %3$s",
              oaiIdentifier, repository.getRepositoryUrl(), e.getMessage()), e);
    }
    final OaiRecordParser recordParser = new OaiRecordParser(record);
    if (recordParser.recordIsDeleted()) {
      throw new HarvesterException("The record is deleted");
    }
    return recordParser.getRdfRecord();
  }

  @Override
  public Integer countRecords(OaiHarvest harvest) throws HarvesterException {
    final Parameters parameters = Parameters.parameters().withVerb(Verb.Type.ListIdentifiers)
            .include(prepareListIdentifiersParameters(harvest));
    try (final CloseableOaiClient oaiClient = connectionClientFactory
            .createConnectionClient(harvest.getRepositoryUrl());
            final InputStream listIdentifiersResponse = performThrowingFunction(oaiClient,
                    client -> client.execute(parameters))) {
      return readCompleteListSizeFromXML(listIdentifiersResponse);
    } catch (OAIRequestException | IOException e) {
      throw new HarvesterException(String.format(
              "Problem with counting records for endpoint %1$s because of: %2$s",
              harvest.getRepositoryUrl(), e.getMessage()), e);
    }
  }

  private Integer readCompleteListSizeFromXML(InputStream stream) throws HarvesterException {
    final XPathExpression expr;
    try {
      final XPathFactory xpathFactory = XPathFactory.newInstance();
      xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final XPath xpath = xpathFactory.newXPath();
      expr = xpath.compile(COMPLETE_LIST_SIZE_XPATH);
    } catch (XPathExpressionException | XPathFactoryConfigurationException e) {
      throw new HarvesterException("Cannot compile xpath expression.", e);
    }
    final InputSource inputSource = new SAXSource(new InputSource(stream)).getInputSource();
    try {
      final Node resumptionTokenNode = (Node) expr.evaluate(inputSource, XPathConstants.NODE);
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
   * Implementations of this interface can provide connection clients.
   */
  public interface ConnectionClientFactory {

    /**
     * Construct a connection client based on the supplied information.
     *
     * @param oaiPmhEndpoint The base URL.
     * @return A connection instance.
     */
    CloseableOaiClient createConnectionClient(String oaiPmhEndpoint);
  }

  /**
   * Iterator for harvesting. It wraps a source iterator and provides additional closing
   * functionality for the connection client.
   */
  private static class HeaderIterator implements OaiRecordHeaderIterator {

    private final Iterator<Header> source;
    private final CloseableOaiClient oaiClient;

    /**
     * Constructor.
     *
     * @param source The source iterator.
     * @param oaiClient The client to close when the iterator is closed.
     */
    public HeaderIterator(Iterator<Header> source, CloseableOaiClient oaiClient) {
      this.source = source;
      this.oaiClient = oaiClient;
    }

    @Override
    public void forEachFiltered(final ReportingIteration<OaiRecordHeader> action,
            final Predicate<OaiRecordHeader> filter) throws HarvesterException {
      final ReportingIterationWrapper singleIteration = new ReportingIterationWrapper(action,
              filter);
      try {
        while (source.hasNext()) {
          final IterationResult result = singleIteration.process(source.next());
          if (IterationResult.TERMINATE == result) {
            break;
          }
        }
      } catch (RuntimeException e) {
        throw new HarvesterException("Problem while iterating through OAI headers.", e);
      }
    }

    @Override
    public void close() throws IOException {
      this.oaiClient.close();
    }
  }

  private static class ReportingIterationWrapper implements ReportingIteration<Header> {

    final ReportingIteration<OaiRecordHeader> action;
    final Predicate<OaiRecordHeader> filter;

    public ReportingIterationWrapper(ReportingIteration<OaiRecordHeader> action,
            Predicate<OaiRecordHeader> filter) {
      this.action = action;
      this.filter = filter;
    }

    @Override
    public IterationResult process(Header input) {
      final OaiRecordHeader header = new OaiRecordHeader(input.getIdentifier(), input.isDeleted(),
              Optional.ofNullable(input.getDatestamp()).map(Date::toInstant).orElse(null));
      final IterationResult result;
      if (filter.test(header)) {
        result = action.process(header);
        if (result == null) {
          throw new IllegalArgumentException("Iteration result cannot be null.");
        }
      } else {
        result = IterationResult.CONTINUE;
      }
      return result;
    }
  }
}
