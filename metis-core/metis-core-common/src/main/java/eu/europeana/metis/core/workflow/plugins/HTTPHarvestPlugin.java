package eu.europeana.metis.core.workflow.plugins;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.metis.CommonStringValues;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractMetisPlugin {

	private final String topologyName = Topology.HTTP_HARVEST.getTopologyName();

	/**
	 * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
	 */
	public HTTPHarvestPlugin() {
		//Required for json serialization
		super(PluginType.HTTP_HARVEST);
	}

	/**
	 * Constructor to initialize the plugin with pluginMetadata.
	 * <p>Initializes the {@link #pluginType} as well.</p>
	 *
	 * @param pluginMetadata should be {@link HTTPHarvestPluginMetadata}
	 */
	public HTTPHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
		super(PluginType.HTTP_HARVEST, pluginMetadata);
	}

	@Override
	public String getTopologyName() {
		return topologyName;
	}

	@Override
	DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
		String httpUrl = ((HTTPHarvestPluginMetadata) getPluginMetadata()).getUrl();				
		DpsTask dpsTask = new DpsTask();

		Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
		List<String> urls = new ArrayList<>();
		urls.add(httpUrl);
		dataEntries.put(InputDataType.REPOSITORY_URLS, urls);
		dpsTask.setInputData(dataEntries);

		Map<String, String> parameters = new HashMap<>();
		parameters.put("PROVIDER_ID", ecloudProvider);
		parameters.put("OUTPUT_DATA_SETS", String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE, 
				ecloudBaseUrl, ecloudProvider, ecloudDataset));
		parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());		
		dpsTask.setParameters(parameters);

		dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudProvider));				
	
		return dpsTask;
	}
}
