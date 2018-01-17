package eu.europeana.enrichment.rest.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.metis.dereference.DereferenceUtils;
import eu.europeana.metis.dereference.client.DereferenceClient;

/*
 * To be integrated into eCloud topology. This code serves as an example of enrichment through use of the
 * enrichment and dereferencing clients and utils. 
 * 
 */
public class EnrichmentWorker {
	public static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorker.class);
	
	public static void main(String[] args) throws JiBXException {
		final DereferenceClient dereferenceClient = new DereferenceClient("http://metis-dereference-rest-test.eanadev.org");
		final EnrichmentClient enrichmentClient = new EnrichmentClient("http://metis-enrichment-rest-test.eanadev.org");

		String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<rdf:RDF xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
				"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
				"xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"" +
				"xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"" +
				"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"" +
				"xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\"" +
				"xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\"" +
				"xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" +
				"xmlns:ore=\"http://www.openarchives.org/ore/terms/\"" +
				"xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"" +
				"xmlns:dcterms=\"http://purl.org/dc/terms/\"" +
				"xmlns:sch=\"http://purl.oclc.org/dsdl/schematron\"" +
				"xmlns:cc=\"http://creativecommons.org/ns#\"" +
				"xmlns:dcat=\"http://www.w3.org/ns/dcat#\"" +
				"xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\"" +
				"xmlns:adms=\"http://www.w3.org/ns/adms#\"" +
				"xmlns:svcs=\"http://rdfs.org/sioc/services#\"" +
				"xmlns:doap=\"http://usefulinc.com/ns/doap#\"" +
				"xmlns:wdrs=\"http://www.w3.org/2007/05/powder-s#\">" +
				"<edm:ProvidedCHO rdf:about=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>" +
				"<edm:WebResource rdf:about=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>" +
				"<edm:TimeSpan rdf:about=\"#Timespan_Photoconsortium_1937-1938\">" +
				"<skos:prefLabel xml:lang=\"en\">1937-1938</skos:prefLabel>" +
				"<edm:begin>1937-01-01</edm:begin>" +
				"<edm:end>1938-12-31</edm:end>" +
				"</edm:TimeSpan>" +
				"<ore:Aggregation rdf:about=\"Bolton Council/1993.83.27.19\">" +
				"<edm:aggregatedCHO rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>" +
				"<edm:dataProvider>Bolton Council</edm:dataProvider>" +
				"<edm:isShownAt rdf:resource=\"http://boltonworktown.co.uk/photograph/washing-day-2\"/>" +
				"<edm:isShownBy rdf:resource=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>" +
				"<edm:object rdf:resource=\"http://boltonworktown.co.uk/wp-content/uploads/1993.83.27.19.jpg\"/>" +
				"<edm:provider>AthenaPlus</edm:provider>" +
				"<dc:rights>Bolton Council</dc:rights>" +
				"<edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>" +
				"</ore:Aggregation>" +
				"<ore:Proxy rdf:about=\"ProvidedCHO_Bolton_Council_1993_83_27_19\">" +
				"<dc:creator rdf:resource=\"http://vocab.getty.edu/ulan/500021114\"/>" +
				"<dc:description xml:lang=\"pl\">Washing day near Snowden St. Park Mill is visible in the" +
				"background.</dc:description>" +
				"<dc:description xml:lang=\"pl\">Jour de lessive près de Snowden Street. L'usine Park Mills est visible en arrière-plan.</dc:description>" +
				"<dc:title xml:lang=\"en\">Washing Day</dc:title>" +
				"<dc:title xml:lang=\"fr\">Jour de lessive</dc:title>" +
				"<dc:identifier>1993.83.27.19</dc:identifier>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300008247\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300386103\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300247617\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300006321\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300264626\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300008436\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300053042\"/>" +
				"<dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300005730\"/>" +
				"<dc:type rdf:resource=\"http://vocab.getty.edu/aat/300046300\"/>" +
				"<dc:format rdf:resource=\"http://vocab.getty.edu/aat/300128361\"/>" +
				"<dcterms:medium rdf:resource=\"http://vocab.getty.edu/aat/300127149\"/>" +
				"<dcterms:created rdf:resource=\"#Timespan_Photoconsortium_1937-1938\"/>" +
				"<dcterms:provenance xml:lang=\"en\">Bolton Library and Museum Services</dcterms:provenance>" +
				"<dcterms:spatial rdf:resource=\"http://sws.geonames.org/2655237\"/>" +
				"<ore:proxyFor rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>" +
				"<ore:proxyIn rdf:resource=\"Bolton Council/1993.83.27.19\"/>" +
				"<edm:type>IMAGE</edm:type>" +
				"</ore:Proxy>" +
				"<edm:EuropeanaAggregation rdf:about=\"Bolton Council/1993.83.27.19\">" +
				"<edm:aggregatedCHO rdf:resource=\"ProvidedCHO_Bolton_Council_1993_83_27_19\"/>" +
				"<edm:country>Poland</edm:country>" +
				"<edm:language>pl</edm:language>" +
				"</edm:EuropeanaAggregation>" +
				"</rdf:RDF>";

		RDF rdf = DereferenceUtils.toRDF(sample);
						
		try {
			System.out.println("Using the following RDF:\n\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}		
             
		// ==============================================
		// [1] Extract fields from the RDF for enrichment
		// ==============================================
		System.out.println("[1] Extracting fields from RDF for enrichment...");
		final List<InputValue> fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichmentFromRDF(rdf);

		System.out.println("Extracted Fields");
		System.out.println("================");
		
		if (fieldsForEnrichment == null) {
			System.out.println("Got a null result!\n");
		} else if (fieldsForEnrichment.size() == 0) 
			System.out.println("Got an empty result.\n");				
		else {
			int count = 0;
			for (InputValue i : fieldsForEnrichment) {
				count++;
				System.out.println(count + ". " + i.getLanguage() + " " + i.getOriginalField() + " " + i.getValue() + " " + i.getVocabularies());
			}
			System.out.println();

			// ===============================================================================
			// [2] Get the information with which to enrich the RDF using the extracted fields
			// ===============================================================================
			System.out.println("[2] Using extracted fields to gather enrichment information...");
			EnrichmentResultList enrichmentInformation = null;
			try {
				enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment);
			} catch (UnknownException e) {
				e.printStackTrace();
			}

			System.out.println("Enrichment Information");
			System.out.println("======================");
			if (enrichmentInformation == null || enrichmentInformation.getResult() == null)
				System.out.println("Got a NULL result!\n");
			else if (enrichmentInformation.getResult().size() == 0)
				System.out.println("Got an empty result. Nothing to merge.\n");
			else {
				count = 1;
				for (EnrichmentBase enrichmentBase : enrichmentInformation.getResult()) {					
					System.out.println(count + ". " + enrichmentBase);
					count++;
				}				

				// ===============================================
				// [3] Merge the acquired information into the RDF
				// ===============================================
				System.out.println("[3] Merging Enrichment Information...\n");
				try {
					System.out.println("***Pre-Merge RDF***:\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				ArrayList<EnrichmentBase> enrichmentBaseList = (ArrayList<EnrichmentBase>) enrichmentInformation.getResult();			
				rdf = EnrichmentUtils.mergeEntity(rdf, enrichmentBaseList, "");					

				try {
					System.out.println("***Post-Merge RDF***:\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}		
		}
					
		// =================================================
        // [4] Extract fields from the RDF for dereferencing
		// =================================================
		System.out.println("[4] Extracting fields from RDF for dereferencing...");
		Set<String> fieldsForDereferencing = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		System.out.println("Extracted Fields");
		System.out.println("================");

		if (fieldsForDereferencing == null) {
			System.out.println("Got a NULL result!\n");
		} else if (fieldsForDereferencing.size() == 0) {
			System.out.println("Got an empty result.\n");				
		} else {				
			Iterator<String> i = fieldsForDereferencing.iterator();

			int count = 0;
			while (i.hasNext()) {
				System.out.println(count + ". " + i.next());
				count++;
			}
			System.out.println();

			// ===================================================================================================
			// [5] Get the information with which to enrich (via dereferencing) the RDF using the extracted fields
			// ===================================================================================================
			System.out.println("[5] Using extracted fields to gather enrichment-via-dereferencing information...");
			ArrayList<EnrichmentResultList> dereferenceInformation = new ArrayList<EnrichmentResultList>();
			try {
				i = fieldsForDereferencing.iterator();

				while (i.hasNext()) {
					String url = (String)i.next();

					if (url != null)
					{
						System.out.print("Processing " + url + "...");
						EnrichmentResultList result = dereferenceClient.dereference(url);					

						if (result != null)
							dereferenceInformation.add(result);
						else
							System.out.print("got null...");
						
						
						System.out.println("Done.");
					}
				}		
			} catch (UnknownException e) {
				e.printStackTrace();
			}

			System.out.println();
			System.out.println("Dereference Information");
			System.out.println("=======================");

			if (dereferenceInformation.size() == 0)
				System.out.println("Got an empty result. Nothing to merge.\n");
			else {		
				int contentCount = 0;

				for (EnrichmentResultList enrichmentResultList : dereferenceInformation) {
					if (enrichmentResultList.getResult() != null 
							&& !enrichmentResultList.getResult().isEmpty())
						contentCount++;
				}

				if (contentCount == 0)
					System.out.println("Got an empty result. Nothing to merge.\n");
				else {
					count = 1;
					for (EnrichmentResultList dereferenceResultList : dereferenceInformation) {
						for (EnrichmentBase enrichmentBase : dereferenceResultList.getResult()) {
							System.out.println(
									count 
									+ ". About: " + enrichmentBase.getAbout()
									+ " AltLabelList: " + enrichmentBase.getAltLabelList()
									+ " Notes: " + enrichmentBase.getNotes()
									+ " PrefLabelListL " + enrichmentBase.getPrefLabelList());
							count++;
						}
					}
					System.out.println();

					// ===============================================
					// [6] Merge the acquired information into the RDF
					// ===============================================
					System.out.println("[6] Merging Dereference Information...\n");
					try {
						System.out.println("***Pre-Merge RDF***:\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					for (EnrichmentResultList dereferenceResultList : dereferenceInformation) {
						ArrayList<EnrichmentBase> dereferenceBaseList = (ArrayList<EnrichmentBase>) dereferenceResultList.getResult();			
						rdf = DereferenceUtils.mergeEntity(rdf, dereferenceBaseList);					
					}

					try {
						System.out.println("***Post-Merge RDF***:\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}							
		}					
	}
}
