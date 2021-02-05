package eu.europeana.metis.harvesting.oaipmh;

import java.io.Closeable;
import org.dspace.xoai.serviceprovider.client.OAIClient;

public interface CloseableOaiClient extends OAIClient, Closeable {

}
