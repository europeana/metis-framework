package eu.europeana.enrichment.gui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;
/**
 * Main enrichment service interface exposing the enrich functionality
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@RemoteServiceRelativePath("enrich")
public interface EnrichmentService extends RemoteService {

	/**
	 * Enrichment method
	 * @param values The values to enrich
	 * @param toEdm export as EDM/XML
	 * @return The generated enrichments
	 */
	List<EntityWrapperDTO> enrich(List<InputValueDTO> values, boolean toEdm);
}
