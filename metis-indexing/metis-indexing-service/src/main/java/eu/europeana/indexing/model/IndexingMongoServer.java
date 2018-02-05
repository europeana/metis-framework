package eu.europeana.indexing.model;

import org.mongodb.morphia.Datastore;

import eu.europeana.corelib.storage.MongoServer;

public class IndexingMongoServer implements MongoServer {
	private Datastore ds;
	
	public IndexingMongoServer(Datastore ds) {
		this.ds = ds;
	}

	@Override
	public Datastore getDatastore() {
		return ds;
	}

	@Override
	public void close() {
	}
}
