package eu.europeana.enrichment.rest.client;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.metis.dereference.client.DereferenceClient;
import eu.europeana.metis.utils.DereferenceUtils;
import eu.europeana.metis.utils.EnrichmentUtils;
import eu.europeana.metis.utils.InputValue;

/*
 * To be integrated into eCloud topology. This code serves as an example of enrichment through use of the
 * enrichment and dereferencing clients and utils. 
 * 
 * WORK IN PROGRESS
 * 
 */
public class EnrichmentWorker {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorker.class);
	private static IBindingFactory enrichmentBaseFactory;
	private static final String ENCODING = StandardCharsets.UTF_8.name();

	/* ***NOTE: Need to create JiBX definition for EnrichmentBase (and subclasses too?) so that EnrichmentClient
	 * results can be converted to XML for further processing.
	 */
	static {
    	try {
    		enrichmentBaseFactory = BindingDirectory.getFactory(EnrichmentBase.class);
    	} 
    	catch (JiBXException e) {
    		LOGGER.error("Unable to get BindingFactory", e);
    		System.exit(-1);
    	}
    }
	
	public static void main(String[] args) {
		
		// TODO JOCHEN provide host URL
		final String hostUrl = "";
		final DereferenceClient dereferenceClient = new DereferenceClient(hostUrl);
		final EnrichmentClient enrichmentClient = new EnrichmentClient(hostUrl);

		// Create a simple RDF
		// TODO JOCHEN Still need to create a better RDF in order to test the flow. This RDF is not sufficient. Need samples!
		Creator creator = new Creator();
		creator.setString("Creator 1");

		Lang language = new Lang();
		language.setLang("English");
		creator.setLang(language);

		Choice choice = new Choice();
		choice.setCreator(creator);

		ProxyType proxy = new ProxyType();
		proxy.setChoiceList(Collections.singletonList(choice));

		RDF rdf = new RDF();
		rdf.setProxyList(Collections.singletonList(proxy));
		
		// Extract fields for enrichment
		final List<InputValue> fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichment(rdf);

		// Get the information with which to enrich using the extracted fields
		List<EnrichmentBase> enrichmentInformation = null;
		try {
			enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment).getResult();
		} catch (UnknownException e) {
			e.printStackTrace();
		}
		
		// Merge the acquired information with the original record
		try {
			IMarshallingContext context = enrichmentBaseFactory.createMarshallingContext();
			context.setIndent(2);
			
			for (EnrichmentBase enrichmentBase : enrichmentInformation) {
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				context.marshalDocument(enrichmentBase, ENCODING, null, out);
				String entity = out.toString(ENCODING);

				// TODO JOCHEN makes no sense to do this for every field? Expensive! This method
				// could also work for multiple entities.
				rdf = EnrichmentUtils.mergeEntityForEnrichment(EnrichmentUtils.convertRDFtoString(rdf), entity, "");
			}
		} catch (JiBXException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Extract fields for dereferencing and merge the dereferenced fields back in.
		for (String field : DereferenceUtils.extractValuesForDereferencing(rdf)) {
			try {
				// TODO JOCHEN makes no sense to do this for every field? Expensive! This method
				// could also work for multiple entities.
				rdf = DereferenceUtils.mergeEntityForDereferencing(EnrichmentUtils.convertRDFtoString(rdf),
						dereferenceClient.dereference(field));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (JiBXException e) {
				e.printStackTrace();
			}
		}

		System.out.println(rdf);
	}
}
