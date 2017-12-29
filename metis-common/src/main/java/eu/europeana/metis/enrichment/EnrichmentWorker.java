package eu.europeana.metis.enrichment;

import java.util.ArrayList;
import java.util.List;

import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.client.DereferenceClient;
import eu.europeana.metis.utils.EnrichmentUtils;
import eu.europeana.metis.utils.InputValue;

public class EnrichmentWorker {

	public static void main(String[] args) {
		RDF rdf = new RDF();
		
		DereferenceClient dereferenceClient = new DereferenceClient();
		EnrichmentClient enrichmentClient = new EnrichmentClient();

		ProxyType proxy = new ProxyType();

		Choice choice = new Choice();

		Creator creator = new Creator();
		creator.setString("Creator 1");

		Lang language = new Lang();
		language.setLang("Gibberish");
		creator.setLang(language);

		choice.setCreator(creator);

		ArrayList<Choice> choiceList = new ArrayList<Choice>();
		choiceList.add(choice);
		proxy.setChoiceList(choiceList);

		ArrayList<ProxyType> proxyList = new ArrayList<ProxyType>();
		proxyList.add(proxy);

		rdf.setProxyList(proxyList);

		List<InputValue> result = null;

		try {
			result = EnrichmentUtils.extractFieldsForEnrichment(rdf);
		} 
		catch (JiBXException e) {
			e.printStackTrace();
		}	    	


		System.out.println("!!! result size: " + result.size());
		for (InputValue i : result) {
			System.out.println(i.getValue());
		}

	}

}
