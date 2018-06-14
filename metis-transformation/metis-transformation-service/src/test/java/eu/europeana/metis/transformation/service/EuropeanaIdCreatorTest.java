package eu.europeana.metis.transformation.service;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.RDF;

public class EuropeanaIdCreatorTest {

  private static final String RDF_SKELETON = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
      + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n"
      + "    xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"> \n" + "%s\n" + "</rdf:RDF>";
  private static final String PROVIDED_CHO_SKELETON = "<edm:ProvidedCHO %s />";
  private static final String RDF_ABOUT_SKELETON = "rdf:about=\"%s\"";

  private static final String DATASET_ID = "123456";
  private static final String DATASET_ID_PLUS_LETTER = DATASET_ID + "a";

  private static final String RECORD_ID = "testid";
  private static final String RECORD_ID_PATH = "testpath";
  private static final String RECORD_ID_SERVER = "http://www.europeana.eu";
  private static final String RECORD_ID_HTTP =
      RECORD_ID_SERVER + "/" + RECORD_ID_PATH + "/" + RECORD_ID;
  private static final String RECORD_ID_HTTPS =
      "https://www.europeana.eu/" + RECORD_ID_PATH + "/" + RECORD_ID;
  private static final String SECOND_RECORD_ID = "secondid";

  private static String createRdfString(String... ids) {
    final String providedChos = Stream.of(ids)
        .map(id -> id == null ? "" : String.format(RDF_ABOUT_SKELETON, id))
        .map(about -> String.format(PROVIDED_CHO_SKELETON, about)).collect(Collectors.joining());
    return String.format(RDF_SKELETON, providedChos);
  }

  private static RDF createRdf(String... ids) {
    final List<ProvidedCHOType> providedChos = new ArrayList<>();
    for (String id : ids) {
      final ProvidedCHOType providedCho = new ProvidedCHOType();
      if (id != null) {
        providedCho.setAbout(id);
      }
      providedChos.add(providedCho);
    }
    final RDF rdf = new RDF();
    rdf.setProvidedCHOList(providedChos);
    return rdf;
  }

  public void testIdCreation(String expectedLegacy, String datasetId, String... ids)
      throws EuropeanaIdException {
    final RDF rdf = createRdf(ids);
    final String rdfString = createRdfString(ids);
    final EuropeanaIdCreator creator = new EuropeanaIdCreator();
    assertEquals(expectedLegacy, creator.constructEuropeanaId(rdfString, datasetId).getEuropeanaGeneratedId());
    assertEquals(expectedLegacy, creator.constructEuropeanaId(rdf, datasetId).getEuropeanaGeneratedId());
  }

  @Test
  public void testIdCreation() throws EuropeanaIdException {

    final String expected0 = "/" + DATASET_ID + "/" + RECORD_ID_PATH + "_" + RECORD_ID;
    testIdCreation(expected0, DATASET_ID, RECORD_ID_PATH + "/" + RECORD_ID);

    final String expected1 = "/" + DATASET_ID + "/" + RECORD_ID_PATH + "_" + RECORD_ID;
    testIdCreation(expected1, DATASET_ID_PLUS_LETTER, RECORD_ID_PATH + "/" + RECORD_ID);

    final String expected2 = "/" + DATASET_ID + "/" + RECORD_ID_PATH + "_" + RECORD_ID;
    testIdCreation(expected2, DATASET_ID, RECORD_ID_HTTP, SECOND_RECORD_ID);

    final String expected3 = "/" + DATASET_ID + "/" + RECORD_ID_HTTPS.replaceAll("[/./:]", "_");
    testIdCreation(expected3, DATASET_ID, RECORD_ID_HTTPS, SECOND_RECORD_ID);

    final String expected4 = "/" + DATASET_ID + "/" + RECORD_ID_PATH + "_" + RECORD_ID + "_";
    testIdCreation(expected4, DATASET_ID, RECORD_ID_HTTP + "/");

    final String expected5 = "/" + DATASET_ID + "/";
    testIdCreation(expected5, DATASET_ID, RECORD_ID_SERVER);

    final String expected6 = "/" + DATASET_ID + "/";
    testIdCreation(expected6, DATASET_ID, RECORD_ID_SERVER + "/");

  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfIdCreationWithoutCho() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdf(), DATASET_ID);
  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfIdCreationWithoutAbout() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdf(null, RECORD_ID), DATASET_ID);
  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfIdCreationWithEmptyAbout() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdf("", RECORD_ID), DATASET_ID);
  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfStringIdCreationWithoutCho() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdfString(), DATASET_ID);
  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfStringIdCreationWithoutAbout() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdfString(null, RECORD_ID), DATASET_ID);
  }

  @Test(expected = EuropeanaIdException.class)
  public void testRdfStringIdCreationWithEmptyAbout() throws EuropeanaIdException {
    new EuropeanaIdCreator().constructEuropeanaId(createRdfString("", RECORD_ID), DATASET_ID);
  }
}
