package eu.europeana.metis.harvesting.oaipmh;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvestingIterator.RecordOaiHeaderPostProcessing;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.serviceprovider.exceptions.OAIRequestException;
import io.gdcc.xoai.serviceprovider.parameters.GetRecordParameters;
import io.gdcc.xoai.serviceprovider.parameters.Parameters;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * This class provides an implementation of the {@link OaiHarvester} functionality.
 */
public class OaiHarvesterImpl implements OaiHarvester {

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
  public HarvestingIterator<OaiRecordHeader, OaiRecordHeader> harvestRecordHeaders(OaiHarvest oaiHarvest) {
    return new OaiHarvestingIterator<>(connectionClientFactory.createConnectionClient(
        oaiHarvest.getRepositoryUrl()), oaiHarvest, (header, oaiClient, harvest) -> header);
  }

  @Override
  public HarvestingIterator<OaiRecord, OaiRecordHeader> harvestRecords(OaiHarvest oaiHarvest) {
    final RecordOaiHeaderPostProcessing<OaiRecord> postProcessing = (header, oaiClient, harvest) -> harvestRecord(oaiClient, harvest,
        header.getOaiIdentifier());
    return new OaiHarvestingIterator<>(connectionClientFactory.createConnectionClient(
        oaiHarvest.getRepositoryUrl()), oaiHarvest, postProcessing);
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
                                                                       .withIdentifier(oaiIdentifier)
                                                                       .withMetadataFormatPrefix(repository.getMetadataPrefix());
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
}
