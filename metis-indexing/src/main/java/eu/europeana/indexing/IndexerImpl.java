package eu.europeana.indexing;

import eu.europeana.indexing.service.PublishingService;

/**
 * Implementation of {@link Indexer}.
 * 
 * TODO JOCHEN integrate publishing service with this class.
 * 
 * @author jochen
 *
 */
class IndexerImpl implements Indexer {

  private final PublishingService publishingService;

  IndexerImpl(PublishingService publishingService) {
    this.publishingService = publishingService;
  }

  @Override
  public void index(String record) throws IndexingException {
    this.publishingService.process(record);
  }
}
