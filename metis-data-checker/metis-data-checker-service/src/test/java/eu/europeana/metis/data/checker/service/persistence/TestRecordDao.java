package eu.europeana.metis.data.checker.service.persistence;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.Indexer;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by ymamakis on 9/5/16.
 */
class TestRecordDao {

  @Test
  void test() throws Exception {

    final Indexer indexer = Mockito.mock(Indexer.class);
    final RecordDao recordDao = Mockito.spy(new RecordDao(null, null, indexer));

    final RDF rdf = new RDF();
    Date recordDate = new java.util.Date();
    recordDao.createRecord(rdf, recordDate);

    Mockito.verify(indexer, Mockito.times(1)).indexRdf(Mockito.any(), Mockito.any(Date.class),
        Mockito.anyBoolean(), Mockito.any(), Mockito.anyBoolean()
    );
    Mockito.verify(indexer, Mockito.times(1)).indexRdf(Mockito.eq(rdf), Mockito.any(Date.class),
        Mockito.anyBoolean(), Mockito.any(), Mockito.anyBoolean()
    );

  }
}
