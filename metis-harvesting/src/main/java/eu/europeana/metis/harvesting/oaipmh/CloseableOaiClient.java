package eu.europeana.metis.harvesting.oaipmh;

import io.gdcc.xoai.serviceprovider.client.OAIClient;

import java.io.Closeable;

/**
 * Implementations of this interface provide OAI access and need to be closed.
 */
public abstract class CloseableOaiClient extends OAIClient implements Closeable {

}
