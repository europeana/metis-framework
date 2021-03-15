package eu.europeana.metis.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.core.dataset.Dataset;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TransformationParametersTest {

  @Test
  void testTransformationParametersConstruction() {
    final Dataset dataset = new Dataset();
    dataset.setDatasetId("exampleDatasetId");
    dataset.setDatasetName("exampleDatasetName");
    dataset.setCountry(Country.GREECE);
    dataset.setLanguage(Language.EL);
    final TransformationParameters transformationParameters = new TransformationParameters(dataset);
    assertEquals(dataset.getDatasetId() + "_" + dataset.getDatasetName(), transformationParameters.getDatasetName());
    assertEquals(dataset.getCountry().getName(), transformationParameters.getEdmCountry());
    assertEquals(dataset.getLanguage().name().toLowerCase(Locale.US), transformationParameters.getEdmLanguage());
  }
}