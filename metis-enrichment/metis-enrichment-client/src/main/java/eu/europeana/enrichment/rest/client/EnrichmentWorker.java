package eu.europeana.enrichment.rest.client;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
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
		
		RDF rdf = new RDF();

		// TODO JOCHEN provide host URL
		final String hostUrl = "";
		final DereferenceClient dereferenceClient = new DereferenceClient(hostUrl);
		final EnrichmentClient enrichmentClient = new EnrichmentClient(hostUrl);

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
		final List<InputValue> fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichment(rdf);

		// Get the information with which to enrich using the extracted fields
		EnrichmentResultList enrichmentInformation = null;
		
		try {
			enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment);
		} catch (UnknownException e) {
			e.printStackTrace();
		}

		
		// Merge the acquired information with the original record
		IMarshallingContext context = null;
		try {
			context = enrichmentBaseFactory.createMarshallingContext();
			
			context.setIndent(2);
			ByteArrayOutputStream out  = new ByteArrayOutputStream();
			
			for (EnrichmentBase enrichmentBase : enrichmentInformation.getResult()) {
				context.marshalDocument(enrichmentBase, ENCODING, null, out);
				
				String entity;
				entity = out.toString(ENCODING);

				rdf = EnrichmentUtils.mergeEntityForEnrichment(EnrichmentUtils.convertRDFtoString(rdf), entity, "");
			}
		} catch (JiBXException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		
		// Extract fields for dereferencing
		final Set<String> fieldsForDereferencing = DereferenceUtils.extractValuesForDereferencing(rdf);

		// Get the information with which to enrich using the extracted fields (via dereferencing)
		ArrayList<String> enrichmentInformationViaDereferencing = new ArrayList<>();
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
