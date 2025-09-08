package eu.europeana.indexing.search.v2.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.PersistentIdentifierImpl;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PIDSolrCreatorTest {

  private SolrInputDocument solrInputDocument;
  private PIDSolrCreator pidSolrCreator;
  private PersistentIdentifierImpl pid1;
  private PersistentIdentifierImpl pid2;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    pidSolrCreator = new PIDSolrCreator();
  }

  @Test
  void addAllToDocumentWithPidSolrCreator() {
    pid1 = new PersistentIdentifierImpl();
    pid1.setAbout("about");
    pid1.setCreated("created");
    pid1.setCreator(Map.of("creator", "creator"));
    pid1.setEquivalentPID(List.of("ark:/12148/bpt6k279985"));
    pid1.setReplacesPID(List.of("ark:/12148/bpt6k279988"));
    pid1.setHasPolicy("has policy");
    pid1.setHasURL("has url");
    pid1.setNotation(List.of("notation"));
    pid1.setInScheme("in scheme");
    pid1.setValue("ark:/12148/bpt6k279982");

    pid2 = new PersistentIdentifierImpl();
    pid2.setAbout("about");
    pid2.setCreated("created");
    pid2.setCreator(Map.of("creator", "creator"));
    pid2.setEquivalentPID(List.of("ark:/12145/bpt6k279983"));
    pid2.setReplacesPID(List.of("ark:/12145/bpt6k279987"));
    pid2.setHasPolicy("has policy");
    pid2.setHasURL("has url");
    pid2.setNotation(List.of("notation"));
    pid2.setInScheme("in scheme");
    pid2.setValue("ark:/12145/bpt6k279981");


    // the method to test
    pidSolrCreator.addAllToDocument(solrInputDocument, List.of(pid1, pid2));

    // assertions
    assertTrue(solrInputDocument.containsKey(SolrV2Field.PID.toString()));
    assertIterableEquals(List.of("ark:/12148/bpt6k279982","ark:/12145/bpt6k279981"), solrInputDocument.getFieldValues(SolrV2Field.PID.toString()));
  }

  @Test
  void addToDocumentWithPidSolrCreator() {
    pid1 = new PersistentIdentifierImpl();
    pid1.setAbout("about");
    pid1.setCreated("created");
    pid1.setCreator(Map.of("creator", "creator"));
    pid1.setEquivalentPID(List.of("ark:/12148/bpt6k279985"));
    pid1.setReplacesPID(List.of("ark:/12148/bpt6k279988"));
    pid1.setHasPolicy("has policy");
    pid1.setHasURL("has url");
    pid1.setNotation(List.of("notation"));
    pid1.setInScheme("in scheme");
    pid1.setValue("ark:/12148/bpt6k279982");

    // the method to test
    pidSolrCreator.addToDocument(solrInputDocument, pid1);

    // assertions
    assertTrue(solrInputDocument.containsKey(SolrV2Field.PID.toString()));
    assertEquals("ark:/12148/bpt6k279982", solrInputDocument.getFieldValue(SolrV2Field.PID.toString()));
  }
}
