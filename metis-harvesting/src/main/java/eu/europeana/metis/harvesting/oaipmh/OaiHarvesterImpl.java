package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
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

public class OaiHarvesterImpl implements OaiHarvester {

  private static final Logger LOGGER = LoggerFactory.getLogger(OaiHarvesterImpl.class);

  private final ConnectionClientFactory connectionClientFactory;

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
    final GetRecordParameters getRecordParameters = new GetRecordParameters()
            .withIdentifier(oaiIdentifier).withMetadataFormatPrefix(repository.getMetadataPrefix());
    final Parameters parameters = Parameters.parameters().withVerb(Verb.Type.GetRecord)
            .include(getRecordParameters);
    final String record;
    try (final CloseableOaiClient client = connectionClientFactory.createConnectionClient(
            repository.getRepositoryUrl());
            final InputStream recordStream = client.execute(parameters)) {
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

  /**
   * Auxiliary interface for harvesting records
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
   * Iterator for harvesting
   */
  private static class HeaderIterator implements OaiRecordHeaderIterator {

    private final Iterator<Header> source;
    private final CloseableOaiClient oaiClient;

    public HeaderIterator(Iterator<Header> source, CloseableOaiClient oaiClient) {
      this.source = source;
      this.oaiClient = oaiClient;
    }

    @Override
    public void forEachFiltered(Predicate<OaiRecordHeader> action, Predicate<OaiRecordHeader> filter)
            throws HarvesterException {
      try {
        while (source.hasNext()) {
          final Header nextInput = source.next();
          final OaiRecordHeader nextHeader = new OaiRecordHeader(nextInput.getIdentifier(),
                  nextInput.isDeleted(),
                  Optional.ofNullable(nextInput.getDatestamp()).map(Date::toInstant).orElse(null));
          if (filter.test(nextHeader)) {
            final boolean continueProcessing = action.test(nextHeader);
            if (!continueProcessing) {
              break;
            }
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
}
