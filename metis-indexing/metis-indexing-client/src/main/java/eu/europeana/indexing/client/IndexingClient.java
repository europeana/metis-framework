package eu.europeana.indexing.client;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.client.config.IndexingConfig;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.metis.exception.IndexingException;
import java.util.List;

public class IndexingClient {	
	PublishingService publishingService;
	FullBeanDao fullBeanDao;
	
	public IndexingClient() throws IndexingException
	{		
		IndexingConfig config = IndexingConfig.getInstance();
		fullBeanDao = config.getFullBeanDao();		
		publishingService = config.getPublishingService();
	}
	
	public boolean publish(String record) throws IndexingException {
		return publishingService.process(record);
	}
	
	public List<FullBeanImpl> getAll() {
		return fullBeanDao.getAll();
	}

	public FullBeanImpl getFullBean(String id) {
		return fullBeanDao.getFullBean(id);
	}
}
