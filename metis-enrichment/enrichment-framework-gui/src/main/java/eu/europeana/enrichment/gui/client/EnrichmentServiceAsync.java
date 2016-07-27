package eu.europeana.enrichment.gui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

/**
 * Asynchronous enrichment service
 * @see EnrichmentService
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public interface EnrichmentServiceAsync {
	
	/**
	 * Asynchronous enrichment method
	 * 
	 * @see  EnrichmentService.enrich
	 * 
	 * @param values
	 * @param toEdm
	 * @param entities
	 */
	void enrich(List<InputValueDTO> values, boolean toEdm, AsyncCallback<List<EntityWrapperDTO>> entities);
	
}
