package eu.europeana.metis.data.checker.service.persistence;

import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.Indexer;

/**
 * Created by ymamakis on 9/5/16.
 */
public class TestRecordDao {

  @Test
  public void test() throws Exception {

    final Indexer indexer = Mockito.mock(Indexer.class);
    final RecordDao recordDao = Mockito.spy(new RecordDao(null, null, indexer));

    final RDF rdf = new RDF();
    recordDao.createRecord(rdf);

    Mockito.verify(indexer, Mockito.times(1)).indexRdf(Mockito.any(), Mockito.anyBoolean());
    Mockito.verify(indexer, Mockito.times(1)).indexRdf(Mockito.eq(rdf), Mockito.anyBoolean());

  }
}
