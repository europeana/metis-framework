package eu.europeana.indexing.client;

import eu.europeana.indexing.client.config.IndexingConfig;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.metis.exception.IndexingException;

public class IndexingClient {	
	PublishingService publishingService;
	
	public IndexingClient() throws IndexingException
	{		
		publishingService = IndexingConfig.getInstance().getPublishingService();
	}
	
	public boolean publish(String record) throws IndexingException {
		return publishingService.process(record);
	}
}
