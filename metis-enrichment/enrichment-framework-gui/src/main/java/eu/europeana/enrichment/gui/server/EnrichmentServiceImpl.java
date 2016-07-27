package eu.europeana.enrichment.gui.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import eu.europeana.enrichment.gui.client.EnrichmentService;
import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EnrichmentServiceImpl extends RemoteServiceServlet implements
		EnrichmentService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2840650647541713067L;
	EnrichmentDriver driver;

	public EnrichmentServiceImpl() {
		super();
		Properties props = new Properties();
		try {
                    String s = new File(".").getAbsolutePath();
			props.load(getClass().getClassLoader().getResourceAsStream("gui.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver = new EnrichmentDriver(props.getProperty("enrichment.rest"));
	}

	@Override
	public List<EntityWrapperDTO> enrich(List<InputValueDTO> values,
			boolean toEdm) {
            String s = new File(".").getAbsolutePath();
		List<InputValue> inputValues = new ArrayList<InputValue>();
		for (InputValueDTO value : values) {
			InputValue inputValue = new InputValue();
			if (value.getOriginalField() != null) {
				inputValue.setOriginalField(value.getOriginalField());
			}
			inputValue.setValue(value.getValue());
			List<EntityClass> classes = new ArrayList<EntityClass>();
			classes.add(EntityClass.valueOf(value.getVocabulary()));
			inputValue.setVocabularies(classes);
			inputValue.setLanguage(value.getLanguage());
			inputValues.add(inputValue);
		}

		try {
			List<EntityWrapper> reply = driver.enrich(inputValues, toEdm);

			List<EntityWrapperDTO> replyDTO = new ArrayList<EntityWrapperDTO>();
			for (EntityWrapper entity : reply) {
				EntityWrapperDTO entityDTO = new EntityWrapperDTO();
				entityDTO.setClassName(entity.getClassName());
				if (!toEdm) {
					entityDTO.setContextualEntity(entity.getContextualEntity());
				} else {
					entityDTO.setContextualEntity(StringEscapeUtils
							.unescapeXml(entity.getContextualEntity()));
				}
				if (entity.getOriginalField() != null) {
					entityDTO.setOriginalField(entity.getOriginalField());
				} else {
					entityDTO.setOriginalField("");
				}
				replyDTO.add(entityDTO);
			}

			return replyDTO;

		} catch (JsonGenerationException e) {
			log(e.getMessage(), e);
		} catch (JsonMappingException e) {
			log(e.getMessage(), e);
		} catch (IOException e) {
			log(e.getMessage(), e);
		}

		return null;
	}

}
