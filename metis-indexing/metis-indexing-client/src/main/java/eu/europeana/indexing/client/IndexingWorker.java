package eu.europeana.indexing.client;

import eu.europeana.indexing.client.IndexingClient;
import eu.europeana.metis.exception.IndexingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingWorker {
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingWorker.class);	
	
	private final IndexingClient indexingClient;
	
	/**
	 * Constructor.
	 * 
	 * @param indexingClient The indexing client
	 * @throws IndexingException 
	 */
	public IndexingWorker() throws IndexingException {		
		this.indexingClient = new IndexingClient();
	}
	
	 /**
	   * Publishes a record
	   * 
	   * @param record The record to be published
	   * @return true (success) or false (failure)
	   * @throws IndexingException
	   */
	public boolean publish(String record) throws IndexingException {
		LOGGER.info("Publishing record...");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Record to publish: " + record);
		}
		
		boolean result = indexingClient.publish(record);
		
		if (result)
			LOGGER.info("Successfully published record.");
		else
			LOGGER.debug("Failed to publish record.");

		return result;
	}
}
