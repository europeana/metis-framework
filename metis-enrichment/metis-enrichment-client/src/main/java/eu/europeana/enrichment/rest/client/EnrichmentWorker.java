package eu.europeana.enrichment.rest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.dereference.client.DereferenceClient;
import eu.europeana.metis.utils.EnrichmentUtils;
import eu.europeana.metis.utils.InputValue;

/*
 * *** To be integrated into eCloud topology. This code serves as an example of enrichment through use of the
 * *** enrichment and dereferencing clients and utils 
 */
public class EnrichmentWorker {

	public static void main(String[] args) {
		RDF rdf = new RDF();
		
		DereferenceClient dereferenceClient = new DereferenceClient();
		EnrichmentClient enrichmentClient = new EnrichmentClient();

		// Create a simple RDF
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

		List<InputValue> fieldsForEnrichment = null;

		// Extract fields for enrichment
		try {
			fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichment(rdf);
		} 
		catch (JiBXException e) {
			e.printStackTrace();
		}	    	

		for (InputValue i : fieldsForEnrichment) {
			System.out.println(+ i.getValue());
		}
		
		
		EnrichmentResultList enrichmentInformation = null;
		
		// Get the information with which to enrich using the extracted fields
		try {
			enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment);
		} catch (UnknownException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(enrichmentInformation);

	}

}
