package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.dereference.client.DereferenceClient;
import eu.europeana.metis.utils.DereferenceUtils;
import eu.europeana.metis.utils.EnrichmentUtils;
import eu.europeana.metis.utils.InputValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final static String UTF8= "UTF-8";

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
		System.out.println("W");
		
		RDF rdf = new RDF();
		
		DereferenceClient dereferenceClient = new DereferenceClient();
		EnrichmentClient enrichmentClient = new EnrichmentClient();

		// Create a simple RDF
		// ***NOTE: Still need to create a better RDF in order to test the flow. This RDF is not sufficient. Need samples!
		ProxyType proxy = new ProxyType();

		Choice choice = new Choice();

		Creator creator = new Creator();
		creator.setString("Creator 1");

		Lang language = new Lang();
		language.setLang("English");
		creator.setLang(language);

		choice.setCreator(creator);

		ArrayList<Choice> choiceList = new ArrayList<Choice>();
		choiceList.add(choice);
		proxy.setChoiceList(choiceList);

		ArrayList<ProxyType> proxyList = new ArrayList<ProxyType>();
		proxyList.add(proxy);

		rdf.setProxyList(proxyList);

		
		// Extract fields for enrichment
		List<InputValue> fieldsForEnrichment = null;
		
		try {
			fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichment(rdf);
		} 
		catch (JiBXException e) {
			e.printStackTrace();
		}	    	


		// Get the information with which to enrich using the extracted fields
		EnrichmentResultList enrichmentInformation = null;
		
		try {
			enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment);
		} catch (UnknownException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		// Merge the acquired information with the original record
		IMarshallingContext context = null;
		try {
			context = enrichmentBaseFactory.createMarshallingContext();
			
			context.setIndent(2);
			ByteArrayOutputStream out  = new ByteArrayOutputStream();
			
			for (EnrichmentBase enrichmentBase : enrichmentInformation.getResult()) {
				context.marshalDocument(enrichmentBase, UTF8, null, out);
				
				String entity;
				entity = out.toString(UTF8);

				rdf = EnrichmentUtils.mergeEntityForEnrichment(EnrichmentUtils.convertRDFtoString(rdf), entity, "");
			}
		} catch (JiBXException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		
		// Extract fields for dereferencing
		Set<String> fieldsForDereferencing = null;
		try {
			fieldsForDereferencing = DereferenceUtils.extractValuesForDereferencing(rdf);
		} catch (JiBXException e) {
			e.printStackTrace();
		}
		
		
		// Get the information with which to enrich using the extracted fields (via dereferencing)
		ArrayList<String> enrichmentInformationViaDereferencing = new ArrayList<String>();
		Iterator<String> fieldsIter = fieldsForDereferencing.iterator();
		while (fieldsIter.hasNext()) {
			String field = fieldsIter.next();			
			enrichmentInformationViaDereferencing.add(dereferenceClient.dereference(field));			
		}

		
		// Merge the acquired information with the original record
		for (String entity : enrichmentInformationViaDereferencing) {
			try {
				rdf = DereferenceUtils.mergeEntityForDereferencing(EnrichmentUtils.convertRDFtoString(rdf), entity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (JiBXException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(rdf);
	}
}
