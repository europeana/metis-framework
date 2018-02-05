package eu.europeana.indexing.client;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.client.config.Application;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IndexingClient {	
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingClient.class);
	
	PublishingService publishingService;
	FullBeanDao fullBeanDao;
	
	public IndexingClient()
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);		
		publishingService = ctx.getBean(PublishingService.class);
		fullBeanDao = ctx.getBean(FullBeanDao.class);
		ctx.close();
	}
	
	public boolean process(String record) throws IndexingException {
		try {
			return publishingService.process(record);
		} catch (JiBXException | SolrServerException e) {
			LOGGER.warn(e.getMessage());
			return false;
		}
	}
	
	public List<FullBeanImpl> getAll() {
		return fullBeanDao.getAll();
	}
	
	public FullBeanImpl getFullBEan(String id) {
		return fullBeanDao.getFullBean(id);
	}
}
