package eu.europeana.metis.harvesting.oaipmh;

import java.io.Closeable;
import org.dspace.xoai.serviceprovider.client.OAIClient;

/**
 * Implementations of this interface provide OAI access and need to be closed.
 */
public interface CloseableOaiClient extends OAIClient, Closeable {

}
