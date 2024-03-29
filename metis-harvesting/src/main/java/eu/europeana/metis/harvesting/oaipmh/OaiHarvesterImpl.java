package eu.europeana.metis.harvesting.oaipmh;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;

import eu.europeana.metis.harvesting.FullRecordHarvestingIterator;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.io.IOUtils;
import io.gdcc.xoai.model.oaipmh.results.record.Header;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.serviceprovider.ServiceProvider;
import io.gdcc.xoai.serviceprovider.exceptions.BadArgumentException;
import io.gdcc.xoai.serviceprovider.exceptions.OAIRequestException;
import io.gdcc.xoai.serviceprovider.model.Context;
import io.gdcc.xoai.serviceprovider.parameters.GetRecordParameters;
import io.gdcc.xoai.serviceprovider.parameters.ListIdentifiersParameters;
import io.gdcc.xoai.serviceprovider.parameters.Parameters;
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
  public OaiRecordHeaderIterator harvestRecordHeaders(OaiHarvest harvest) {
    return new RecordHeaderIterator(connectionClientFactory.createConnectionClient(
        harvest.getRepositoryUrl()), harvest);
  }

  @Override
  public FullRecordHarvestingIterator<OaiRecord, OaiRecordHeader> harvestRecords(OaiHarvest harvest) {
    return new FullRecordIterator(connectionClientFactory.createConnectionClient(
        harvest.getRepositoryUrl()), harvest);
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
  public OaiRecord harvestRecord(OaiRepository repository, String oaiIdentifier)
          throws HarvesterException {
     try (final CloseableOaiClient oaiClient = connectionClientFactory
            .createConnectionClient(repository.getRepositoryUrl())) {
      return harvestRecord(oaiClient, repository, oaiIdentifier);
    } catch (IOException e) {
      throw new HarvesterException(String.format(
              "Problem with harvesting record %1$s for endpoint %2$s because of: %3$s",
              oaiIdentifier, repository.getRepositoryUrl(), e.getMessage()), e);
    }
  }

  private static OaiRecord harvestRecord(CloseableOaiClient oaiClient, OaiRepository repository,
      String oaiIdentifier) throws HarvesterException {
    final GetRecordParameters getRecordParameters = GetRecordParameters.request()
        .withIdentifier(oaiIdentifier).withMetadataFormatPrefix(repository.getMetadataPrefix());
    final Parameters parameters = Parameters.parameters().withVerb(Verb.Type.GetRecord)
        .include(getRecordParameters);
    final byte[] byteArrayRecord;
    try (final InputStream recordStream = performThrowingFunction(oaiClient,
            client -> client.execute(parameters))) {
      byteArrayRecord = IOUtils.toByteArray(recordStream);
    } catch (OAIRequestException | IOException e) {
      throw new HarvesterException(String.format(
          "Problem with harvesting record %1$s for endpoint %2$s because of: %3$s",
          oaiIdentifier, repository.getRepositoryUrl(), e.getMessage()), e);
    }
    return new OaiRecordParser().parseOaiRecord(byteArrayRecord);
  }

  @Override
  public Integer countRecords(OaiHarvest harvest) throws HarvesterException {
    try (HarvestingIterator<?, ?> iterator = harvestRecordHeaders(harvest)) {
      return iterator.countRecords();
    } catch (IOException e) {
      throw new HarvesterException("Problem while closing iterator.", e);
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

  private static class RecordHeaderIterator extends OaiHarvestingIterator<OaiRecordHeader>
      implements OaiRecordHeaderIterator {

    public RecordHeaderIterator(CloseableOaiClient oaiClient, OaiHarvest harvest) {
      super(oaiClient, harvest);
    }

    @Override
    public void forEachFiltered(ReportingIteration<OaiRecordHeader> action,
        Predicate<OaiRecordHeader> filter) throws HarvesterException {
      forEachHeaderFiltered(action, filter);
    }
  }

  private static class FullRecordIterator extends OaiHarvestingIterator<OaiRecord>
      implements FullRecordHarvestingIterator<OaiRecord, OaiRecordHeader> {

    public FullRecordIterator(CloseableOaiClient oaiClient, OaiHarvest harvest) {
      super(oaiClient, harvest);
    }

    @Override
    public void forEachFiltered(ReportingIteration<OaiRecord> action,
        Predicate<OaiRecordHeader> filter) throws HarvesterException {
      forEachRecordFiltered(action, filter);
    }
  }

  /**
   * Iterator for harvesting. It wraps a source iterator and provides additional closing
   * functionality for the connection client.
   */
  private static abstract class OaiHarvestingIterator<R> implements HarvestingIterator<R, OaiRecordHeader> {

    private Iterator<Header> source = null;
    private final CloseableOaiClient oaiClient;
    private final OaiHarvest harvest;

    /**
     * Constructor.
     *
     * @param oaiClient The client to close when the iterator is closed.
     * @param harvest The harvest request to execute.
     */
    public OaiHarvestingIterator(CloseableOaiClient oaiClient, OaiHarvest harvest) {
      this.oaiClient = oaiClient;
      this.harvest = harvest;
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

    public void forEachRecordFiltered(final ReportingIteration<OaiRecord> action,
        final Predicate<OaiRecordHeader> filter) throws HarvesterException {
      final RecordPostProcessing<OaiRecord> postProcessing = header -> harvestRecord(oaiClient, harvest, header.getOaiIdentifier());
      forEachWithPostProcessing(action, postProcessing, filter);
    }

    public void forEachHeaderFiltered(final ReportingIteration<OaiRecordHeader> action,
        final Predicate<OaiRecordHeader> filter) throws HarvesterException {
      forEachWithPostProcessing(action, header -> header, filter);
    }

    public <O> void forEachWithPostProcessing(final ReportingIteration<O> action,
            final RecordPostProcessing<O> postProcessing, final Predicate<OaiRecordHeader> filter)
        throws HarvesterException {
      final SingleIteration<O> singleIteration = new SingleIteration<>(filter, postProcessing, action);
      try {
        while (getOrCreateSource().hasNext()) {
          final Header header = Optional.of(getOrCreateSource().next())
              .orElseThrow(() -> new HarvesterException("Unexpected null header."));
          try {
            if (singleIteration.process(header) == IterationResult.TERMINATE) {
              break;
            }
          } catch (IOException e) {
            throw new HarvesterException("Problem while processing: " + header.getIdentifier(), e);
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
  }

  private interface RecordPostProcessing<O> {

    O postProcess(OaiRecordHeader input) throws HarvesterException;
  }

  private record SingleIteration<O>(Predicate<OaiRecordHeader> filter,
                                    RecordPostProcessing<O> postProcessing,
                                    ReportingIteration<O> action) {

    public IterationResult process(Header input) throws HarvesterException, IOException {
        final OaiRecordHeader header = OaiRecordHeader.convert(input);
        if (filter.test(header)) {
          final O postProcessResult = Optional.ofNullable(postProcessing.postProcess(header))
              .orElseThrow(() -> new HarvesterException("Post processing result cannot be null."));
          return Optional.ofNullable(action.process(postProcessResult))
              .orElseThrow(() -> new HarvesterException("Iteration result cannot be null."));
        }
        return IterationResult.CONTINUE;
      }
    }
}
