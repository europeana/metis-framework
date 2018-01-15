package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.dereference.client.DereferenceClient;
import eu.europeana.metis.utils.DereferenceUtils;
import eu.europeana.metis.utils.EnrichmentUtils;
import eu.europeana.metis.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		
		//final DereferenceClient dereferenceClient = new DereferenceClient("http://localhost:8080/deref");
		//final EnrichmentClient enrichmentClient = new EnrichmentClient("http://localhost:8080/enrich");
	
		RDF rdf = null;

		String sample = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
				"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\""+
				"xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\""+
				"xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:svcs=\"http://rdfs.org/sioc/services#\">"+
				"<record rdf:about=\"http://dbnl.org/tekst/kloo003verz01_01/\">"+
				"<dc:title xml:lang=\"nl\">Verzen</dc:title>"+
				"<dc:creator rdf:resource=\"https://viaf.org/viaf/78822798\"/>"+
				"<dc:contributor rdf:resource=\"https://viaf.org/viaf/100910150\"/>"+
				"<dc:publisher rdf:resource=\"http://vocab.getty.edu/ulan/500105151\"/>"+
				"<dc:language>nl</dc:language>"+
				"<dc:coverage rdf:resource=\"http://sws.geonames.org/2950159\"/>"+
				"<dc:subject rdf:resource=\"http://iconclass.org/rkd/11F/\"/>"+
				"<dc:type rdf:resource=\"http://vocab.getty.edu/aat/300178924\"/>"+
				"<dc:type rdf:resource=\"http://www.mimo-db.eu/HornbostelAndSachs/356/\"/>"+
				"<dcterms:spatial rdf:resource=\"http://sws.geonames.org/2759793/\"/>"+
				"<dcterms:medium rdf:resource=\"http://vocab.getty.edu/aat/300011851\"/>"+
				"<dc:identifier>http://dbnl.org/tekst/kloo003verz01_01/</dc:identifier>"+
				"<edm:isShownAt rdf:resource=\"http://dbnl.org/tekst/kloo003verz01_01/\"/>"+
				"<edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/>"+
				"<edm:isShownBy rdf:resource=\"http://dbnl.org/tekst/kloo003verz01_01/kloo003verz01_01.pdf\"/>"+
				"<edm:type>TEXT</edm:type>"+
				"<edm:dataProvider>National Library of the Netherlands - Koninklijke Bibliotheek</edm:dataProvider>"+
				"<edm:intermediateProvider>Digitale Bibliotheek voor de Nederlandse Letteren</edm:intermediateProvider>"+
				"<edm:provider>National Library of the Netherlands - Koninklijke Bibliotheek</edm:provider>"+
				"</record>"+
				"</rdf:RDF>";

		String sample2 =  "<?xml version=\"1.0\" encoding=\"utf-8\"?><rdf:RDF xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
				"<edm:ProvidedCHO rdf:about=\"/00903/1008362\"/>" +
				"<edm:WebResource rdf:about=\"/00903/1008362\">" +
				"<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
				"</edm:WebResource>" +
				"<ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"1008362\">" +
				"<edm:aggregatedCHO rdf:resource=\"/00903/1008362\"/>" +
				"<edm:dataProvider>Österreichische Mediathek</edm:dataProvider>" +
				"<edm:isShownAt rdf:resource=\"http://www.mediathek.at/virtuelles-museum/Schifter/Der_Sammler/Nachkriegszeit/Seite_24_24.htm/zone_doc_id=1000867\"/>" +
				"<edm:provider>Österreichische Mediathek</edm:provider>" +
				"<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:rights>" +
				"<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
				"</ore:Aggregation>" +
				"<ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"/00903/1008362\">" +
				"<dc:contributor xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:contributor>" +
				"<dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\">20. Jahrhundert</dc:coverage>" +
				"<dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Schifter, Günther</dc:creator>" +
				"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1998-12-04T00:00:00+00:00</dc:date>" +
				"<dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1998 12 04</dc:date>" +
				"<dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\">flv</dc:format>" +
				"<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1008362</dc:identifier>" +
				"<dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">DE</dc:language>" +
				"<dc:publisher xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek</dc:publisher>" +
				"<dc:source xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Österreichische Mediathek, V-09157</dc:source>" +
				"<dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Erinnerung - Rückblicke - 1946 bis 1970</dc:subject>" +
				"<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Anfänge bei Rot-Weiß-Rot</dc:title>" +
				"<dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\">video</dc:type>" +
				"<edm:type>VIDEO</edm:type>" +
				"</ore:Proxy>" +
				"<edm:EuropeanaAggregation rdf:about=\"1008362\">" +
				"<edm:aggregatedCHO rdf:resource=\"1008362\"/>" +
				"<edm:country>Austria</edm:country>" +
				"<edm:language>de</edm:language>" +
				"<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +
				"</edm:EuropeanaAggregation>" +
				"</rdf:RDF>";

		String sample3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<rdf:RDF xmlns=\"http://www.europeana.eu/schemas/edm/\"" +
				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
				"xmlns:dcterms=\"http://purl.org/dc/terms/\"" +
				"xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"" +
				"xmlns:enrichment=\"http://www.europeana.eu/schemas/edm/enrichment\"" +
				"xmlns:europeana=\"http://www.europeana.eu/schemas/ese/\"" +
				"xmlns:oai=\"http://www.openarchives.org/OAI/2.0\"" +
				"xmlns:ore=\"http://www.openarchives.org/ore/terms/\"" +
				"xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" +
				"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
				"xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"" +
				"xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				"<edm:ProvidedCHO rdf:about=\"providedCHO_PT/TT/LO_PT/TT/LO/003/31/64\"/>" +
				"<edm:WebResource rdf:about=\"http://digitarq.arquivos.pt/details?id=4223286\">" +
				"<dc:description>PT/TT/LO/003/31/64</dc:description>" +
				"<edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc-nd/2.5/pt/\"/>" +
				"</edm:WebResource>" +
				"<ore:Aggregation rdf:about=\"aggregation_PT/TT/LO_PT/TT/LO/003/31/64\">" +
				"<edm:aggregatedCHO rdf:resource=\"providedCHO_PT/TT/LO_PT/TT/LO/003/31/64\"/>" +
				"<edm:dataProvider>Arquivo Nacional Torre do Tombo</edm:dataProvider>" +
				"<edm:isShownAt rdf:resource=\"http://digitarq.arquivos.pt/details?id=4223286\"/>" +
				"<edm:isShownBy rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/scans/PT-TT-LO-003-31-64_m0001_derivada_adapted.jpg\"/>" +
				"<edm:object rdf:resource=\"http://digitarq.arquivos.pt/vault/?id=THUMB/620F23E483A260352C8E9FCE9A039F88&amp;a=false&amp;m=image/jpeg\"/>" +
				"<edm:provider>Archives Portal Europe</edm:provider>" +
				"<edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc-nd/2.5/pt/\"/>" +
				"</ore:Aggregation>" +
				"<ore:Proxy rdf:about=\"providedCHO_PT/TT/LO_PT/TT/LO/003/31/64\">" +
				"<dc:identifier>PT/TT/LO/003/31/64</dc:identifier>" +
				"<dc:date>1867-06-26/1867-07-01</dc:date>" +
				"<dcterms:created>1867-06-26/1867-07-01</dcterms:created>" +
				"<dc:subject>Democracy</dc:subject>" +
				"<dc:description>Inclui o Decreto n.º 141 das Cortes Gerais de 26 de Junho de 1867, e o texto da reforma penal e de prisões e da abolição da pena de morte. A carta de lei apresenta um selo de chapa.</dc:description>" +
				"<dc:type>Archival material</dc:type>" +
				"<dcterms:extent>3 doc. (12 f.; 400 x 255 x 4 mm); papel</dcterms:extent>" +
				"<dcterms:provenance>O documento encontrava-se dentro da \"Pasta Azul\". A Carta de Lei de Abolição da Pena de Morte (1867) recebeu, no dia 15 de abril de 2015, a Marca do Património Europeu.</dcterms:provenance>" +
				"<dc:title>Carta de Lei pela qual D. Luís sanciona o Decreto das Cortes Gerais de 26 de Junho de 1867 que aprova a reforma penal e das prisões, com abolição da pena de morte</dc:title>" +
				"<dc:language>pt</dc:language>" +
				"<ore:proxyFor rdf:resource=\"providedCHO_PT/TT/LO_PT/TT/LO/003/31/64\"/>" +
				"<ore:proxyIn rdf:resource=\"aggregation_PT/TT/LO_PT/TT/LO/003/31/64\"/>" +
				"<edm:type>TEXT</edm:type>" +
				"</ore:Proxy>" +
				"<edm:EuropeanaAggregation rdf:about=\"aggregation_PT/TT/LO_PT/TT/LO/003/31/64\">" +
				"<edm:aggregatedCHO rdf:resource=\"providedCHO_PT/TT/LO_PT/TT/LO/003/31/64\"/>" +
				"<edm:country>Portugal</edm:country>" +
				"<edm:language>pt</edm:language>" +
				"</edm:EuropeanaAggregation>" +
				"</rdf:RDF>";

		String sample4 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<rdf:RDF xmlns:dcterms=\"http://purl.org/dc/terms/\"" +
				"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"" +
				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" + 
				"xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"" + 
				"xmlns:rdaGr2=\"http://RDVocab.info/ElementsGr2/\"" + 
				"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema\"" + 
				"xmlns:cc=\"http://creativecommons.org/ns\"" + 
				"xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + 
				"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + 
				"xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"" + 
				"xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"" + 
				"xmlns:metis=\"http://www.europeana.eu/schemas/metis\">" +
				"<edm:Agent rdf:about=\"http://vocab.getty.edu/ulan/500115588\"/>" +
				"</rdf:RDF>";

		rdf = DereferenceUtils.toRDF(sample3);
				
		// Add some extra information to the RDF
		PlaceType place = new PlaceType();
		place.setAbout("http://dummy1.dum");

		IsPartOf ipo = new IsPartOf();

		ResourceOrLiteralType.Lang language = new ResourceOrLiteralType.Lang();
		language.setLang("English");
		ipo.setLang(language);

		ipo.setString("");

		ResourceOrLiteralType.Resource resource = new ResourceOrLiteralType.Resource();
		resource.setResource("http://sws.geonames.org/3020251");
		ipo.setResource(resource);

		ArrayList<IsPartOf> ipoList = new ArrayList<IsPartOf>();
		ipoList.add(ipo);

		place.setIsPartOfList(ipoList);

		ArrayList<PlaceType> placeList = new ArrayList<PlaceType>();
		placeList.add(place);

		rdf.setPlaceList(placeList);		
			
		try {
			System.out.println("Using the following RDF:\n\n" + EnrichmentUtils.convertRDFtoString(rdf) + "\n");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}		
             
		// [1] Extract fields from the RDF for enrichment
		System.out.println("[1] Extracting fields from RDF for enrichment...");
		final List<InputValue> fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichmentFromRDF(rdf);

		System.out.println("Extracted Fields");
		System.out.println("================");
		
		if (fieldsForEnrichment == null) {
			System.out.println("Got a NULL result!\n");
		} else if (fieldsForEnrichment.size() == 0) 
			System.out.println("Got an empty result.\n");				
		else {
			int count = 0;
			for (InputValue i : fieldsForEnrichment) {
				count++;
				System.out.println(count + ". " + i.getLanguage() + " " + i.getOriginalField() + " " + i.getValue() + " " + i.getVocabularies());
			}
			System.out.println();

			// [2] Get the information with which to enrich the RDF using the extracted fields
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

				// [3] Merge the acquired information in the RDF
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

					
        // [4] Extract fields for dereferencing
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

			// [5] Get the information with which to enrich (via dereferencing) the RDF using the extracted fields
			System.out.println("[5] Using extracted fields to gather enrichment-via-dereferencing information...");
			ArrayList<EnrichmentResultList> dereferenceInformation = new ArrayList<EnrichmentResultList>();
			try {
				i = fieldsForDereferencing.iterator();

				while (i.hasNext()) {
					String url = (String)i.next();

					if (url != null)
					{
						EnrichmentResultList result = dereferenceClient.dereference(url);					

						if (result != null)
							dereferenceInformation.add(result);
					}
				}		
			} catch (UnknownException e) {
				e.printStackTrace();
			}

			System.out.println("Dereference Information");
			System.out.println("=======================");

			if (dereferenceInformation == null)
				System.out.println("Got a NULL result!\n");
			else if (dereferenceInformation.size() == 0)
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

					// [6] Merge the acquired information in the RDF
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
