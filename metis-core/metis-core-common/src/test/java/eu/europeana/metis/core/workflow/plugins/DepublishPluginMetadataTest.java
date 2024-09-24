package eu.europeana.metis.core.workflow.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.utils.DepublicationReason;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DepublishPluginMetadataTest {

  DepublishPluginMetadata depublishPluginMetadata = new DepublishPluginMetadata();

  @Test
  void getExecutablePluginType() {
    assertEquals(ExecutablePluginType.DEPUBLISH, depublishPluginMetadata.getExecutablePluginType());
  }

  @Test
  void isDatasetDepublish() {
    final boolean datasetDepublish = true;
    depublishPluginMetadata.setDatasetDepublish(datasetDepublish);
    boolean actualDepublish = depublishPluginMetadata.isDatasetDepublish();
    assertEquals(datasetDepublish, actualDepublish);
  }

  @Test
  void getRecordIdsToDepublish() {
    Set<String> recordIdsToDepublish = Set.of("t1r", "t2r", "t3r", "t4r", "t5r");
    depublishPluginMetadata.setRecordIdsToDepublish(recordIdsToDepublish);
    Set<String> actualDepublish = depublishPluginMetadata.getRecordIdsToDepublish();
    assertEquals(recordIdsToDepublish, actualDepublish);
  }

  @ParameterizedTest
  @EnumSource(DepublicationReason.class)
  void getDepublicationReason(DepublicationReason depublicationReason) {

    depublishPluginMetadata.setDepublicationReason(depublicationReason);

    assertEquals(depublicationReason, depublishPluginMetadata.getDepublicationReason());
  }
}
