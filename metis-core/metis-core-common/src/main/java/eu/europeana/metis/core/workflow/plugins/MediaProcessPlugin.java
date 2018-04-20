package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.metis.CommonStringValues;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.MEDIA_PROCESS.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public MediaProcessPlugin() {
    //Required for json serialization
    super(PluginType.MEDIA_PROCESS);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link MediaProcessPluginMetadata}
   */
  public MediaProcessPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.MEDIA_PROCESS, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(
        InputDataType.class);
    inputDataTypeListHashMap.put(InputDataType.DATASET_URLS,
        Collections.singletonList(
            String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
                ecloudBaseUrl, ecloudProvider, ecloudDataset)));
    dpsTask.setInputData(inputDataTypeListHashMap);

    Map<String, String> parameters = new HashMap<>();
    parameters.put("REPRESENTATION_NAME", getRepresentationName());
    parameters.put("REVISION_NAME", getPluginMetadata().getRevisionNamePreviousPlugin());
    parameters.put("REVISION_PROVIDER", ecloudProvider);
    DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT);
    parameters.put("REVISION_TIMESTAMP",
        dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBaseUrl, ecloudProvider, ecloudDataset));
    dpsTask.setParameters(parameters);
    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudProvider));
    return dpsTask;
  }

}
