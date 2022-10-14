package eu.europeana.metis.core.rest.execution.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.LinkCheckingPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.NormalizationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.Topology;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PluginViewTest {

  private PluginView pluginView;

  private static Stream<Arguments> providePluginTestData() {
    return Stream.of(
        Arguments.of(ExecutablePluginFactory.createPlugin(new HTTPHarvestPluginMetadata()),
            PluginType.HTTP_HARVEST, Topology.HTTP_HARVEST),
        Arguments.of(ExecutablePluginFactory.createPlugin(new OaipmhHarvestPluginMetadata()),
            PluginType.OAIPMH_HARVEST, Topology.OAIPMH_HARVEST),
        Arguments.of(ExecutablePluginFactory.createPlugin(new ValidationExternalPluginMetadata()),
            PluginType.VALIDATION_EXTERNAL, Topology.VALIDATION),
        Arguments.of(ExecutablePluginFactory.createPlugin(new TransformationPluginMetadata()),
            PluginType.TRANSFORMATION, Topology.TRANSFORMATION),
        Arguments.of(ExecutablePluginFactory.createPlugin(new LinkCheckingPluginMetadata()),
            PluginType.LINK_CHECKING, Topology.LINK_CHECKING),
        Arguments.of(ExecutablePluginFactory.createPlugin(new ValidationInternalPluginMetadata()),
            PluginType.VALIDATION_INTERNAL, Topology.VALIDATION),
        Arguments.of(ExecutablePluginFactory.createPlugin(new NormalizationPluginMetadata()),
            PluginType.NORMALIZATION, Topology.NORMALIZATION),
        Arguments.of(ExecutablePluginFactory.createPlugin(new EnrichmentPluginMetadata()),
            PluginType.ENRICHMENT, Topology.ENRICHMENT),
        Arguments.of(ExecutablePluginFactory.createPlugin(new MediaProcessPluginMetadata()),
            PluginType.MEDIA_PROCESS, Topology.MEDIA_PROCESS),
        Arguments.of(ExecutablePluginFactory.createPlugin(new IndexToPreviewPluginMetadata()),
            PluginType.PREVIEW, Topology.INDEX),
        Arguments.of(ExecutablePluginFactory.createPlugin(new IndexToPublishPluginMetadata()),
            PluginType.PUBLISH, Topology.INDEX),
        Arguments.of(ExecutablePluginFactory.createPlugin(new DepublishPluginMetadata()),
            PluginType.DEPUBLISH, Topology.DEPUBLISH)
    );
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getPluginType(AbstractMetisPlugin metisPlugin, PluginType expectedPluginType) {
    pluginView = new PluginView(metisPlugin, true);
    assertEquals(expectedPluginType, pluginView.getPluginType());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getId(AbstractMetisPlugin metisPlugin, PluginType expectedPluginType) {
    pluginView = new PluginView(metisPlugin, true);
    assertTrue(pluginView.getId().contains(expectedPluginType.name()));
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getPluginStatus(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertEquals(PluginStatus.INQUEUE, pluginView.getPluginStatus());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getDataStatus(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertEquals(DataStatus.NOT_YET_GENERATED, pluginView.getDataStatus());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getFailMessage(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNull(pluginView.getFailMessage());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getStartedDate(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNull(pluginView.getStartedDate());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getUpdatedDate(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNull(pluginView.getUpdatedDate());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getFinishedDate(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNull(pluginView.getFinishedDate());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getExternalTaskId(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNull(pluginView.getExternalTaskId());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getExecutionProgress(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNotNull(pluginView.getExecutionProgress());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getTopologyName(AbstractMetisPlugin metisPlugin, PluginType pluginType, Topology topology) {
    pluginView = new PluginView(metisPlugin, true);
    assertEquals(topology.getTopologyName(), pluginView.getTopologyName());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void isCanDisplayRawXml(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertTrue(pluginView.isCanDisplayRawXml());
  }

  @ParameterizedTest
  @MethodSource("providePluginTestData")
  void getPluginMetadata(AbstractMetisPlugin metisPlugin) {
    pluginView = new PluginView(metisPlugin, true);
    assertNotNull(pluginView.getPluginMetadata());
  }
}
