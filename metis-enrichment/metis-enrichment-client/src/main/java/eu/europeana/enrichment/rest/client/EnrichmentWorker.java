package eu.europeana.enrichment.rest.client;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Resource;

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Alt;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.Begin;
import eu.europeana.corelib.definitions.jibx.BiographicalInformation;
import eu.europeana.corelib.definitions.jibx.BroadMatch;
import eu.europeana.corelib.definitions.jibx.Broader;
import eu.europeana.corelib.definitions.jibx.CloseMatch;
import eu.europeana.corelib.definitions.jibx.Date;
import eu.europeana.corelib.definitions.jibx.DateOfBirth;
import eu.europeana.corelib.definitions.jibx.DateOfDeath;
import eu.europeana.corelib.definitions.jibx.DateOfEstablishment;
import eu.europeana.corelib.definitions.jibx.DateOfTermination;
import eu.europeana.corelib.definitions.jibx.End;
import eu.europeana.corelib.definitions.jibx.ExactMatch;
import eu.europeana.corelib.definitions.jibx.Gender;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.Lat;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.NarrowMatch;
import eu.europeana.corelib.definitions.jibx.Narrower;
import eu.europeana.corelib.definitions.jibx.Notation;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.jibx.ProfessionOrOccupation;
import eu.europeana.corelib.definitions.jibx.Concept.Choice;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.InScheme;
import eu.europeana.corelib.definitions.jibx.IsNextInSequence;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Related;
import eu.europeana.corelib.definitions.jibx.RelatedMatch;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx._Long;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
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
	private static IBindingFactory rdfFactory;
	private static final String ENCODING = StandardCharsets.UTF_8.name();
	private final static String UTF8= "UTF-8";

	static {
		try {
			rdfFactory = BindingDirectory.getFactory(RDF.class);
		} 
		catch (JiBXException e) {
			LOGGER.error("Unable to get BindingFactory", e);
			System.exit(-1);
		}
	}
	
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
				
		rdf = DereferenceUtils.toRDF(sample3);
             
		// Extract fields for enrichment
		final List<InputValue> fieldsForEnrichment = EnrichmentUtils.extractFieldsForEnrichmentFromRDF(rdf);

		int count = 0;
		System.out.println("The following fields were extracted for enrichment:");
		for (InputValue i : fieldsForEnrichment) {
			count++;
			System.out.println(count + ". " + i.getLanguage() + " " + i.getOriginalField() + " " + i.getValue() + " " + i.getVocabularies());
		}

		// Get the information with which to enrich using the extracted fields
		List<EnrichmentBase> enrichmentInformation = null;
		EnrichmentResultList enrichmentResults = null;
		try {
			enrichmentResults = enrichmentClient.enrich(fieldsForEnrichment);
		} catch (UnknownException e) {
			e.printStackTrace();
		}
		
		System.out.println("Enrichment Information:");
		if (enrichmentResults == null)
			System.out.println("NULL");
		else {
			
			enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment).getResult();
			
			if (enrichmentInformation.size() == 0)
				System.out.println("Empty");
			else {
			
				for (EnrichmentBase enrichmentBase : enrichmentInformation) {
					System.out.println(enrichmentBase);				
				}
			}		
		}
				
		/*
		
		// Merge the acquired information with the original record
		try {
			IMarshallingContext context = agentFactory.createMarshallingContext();
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
		*/
		
		
		System.out.println();
		
		
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
		
        // Extract fields for dereferencing
		Set<String> fieldsForDereferencing = DereferenceUtils.extractValuesForDereferencing(rdf);
		
		System.out.println("The following fields were extracted for dereferencing:");
		
		Iterator i = fieldsForDereferencing.iterator();
		
		count = 0;
		while (i.hasNext()) {
			System.out.println(count + ". " + i.next());
			count++;
		}
		System.out.println();
		
		// Get the information with which to dereference using the extracted fields
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
		
		count = 1;
		for (EnrichmentResultList enrichmentResultList : dereferenceInformation) {
			System.out.println("Raw Result:");
			System.out.println(count + "." + enrichmentResultList + "\n");
			count++;
			
			ArrayList<EnrichmentBase> enrichmentBaseList = (ArrayList<EnrichmentBase>) enrichmentResultList.getResult();
			
			System.out.println("Processed Result:");
			for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
				
				if (enrichmentBase.getClass() == Place.class) {
					System.out.println("Got a place");
					
					try {
						System.out.println(EnrichmentUtils.convertRDFtoString(rdf));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					Place place_ = (Place)enrichmentBase;
					PlaceType placeType = new PlaceType();
					
					// about
					placeType.setAbout(place_.getAbout());
				
					// alt					
					String place_Alt = place_.getAlt();
					if (place_Alt != null) {
						Alt alt = new Alt();
						alt.setAlt(Float.valueOf(place_.getAlt()));
						placeType.setAlt(alt);
					}

					// altLabelList					
					if (place_.getAltLabelList() != null) {
						ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
						for (Label label : place_.getAltLabelList()) {
							if (label != null) {
								AltLabel altLabel = new AltLabel();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								altLabel.setLang(lang);
								altLabel.setString(label.getValue());
								altLabelList.add(altLabel);
							}
						}	
						placeType.setAltLabelList(altLabelList);
					}
					

					// hasPartList					
					if (place_.getHasPartsList() != null) {
						ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
						for (Part part : place_.getHasPartsList()) {
							if (part != null) {
								HasPart hasPart = new HasPart();
								ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
								resrc.setResource(part.getResource());
								hasPart.setResource(resrc);
								hasPartList.add(hasPart);
							}
						}	
						placeType.setHasPartList(hasPartList);
					}
					

					// isPartOfList
					if (place_.getIsPartOfList() != null) {
						ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
						for (Part part : place_.getIsPartOfList()) {
							if (part != null) {
								IsPartOf isPartOf = new IsPartOf();							
								ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
								resrc.setResource(part.getResource());
								isPartOf.setResource(resrc);
								isPartOf.setString("");
								isPartOfList.add(isPartOf);
							}
						}		
						placeType.setIsPartOfList(isPartOfList);
					}
						

					// lat	
					if (place_.getLat() != null) {
						Lat lat = new Lat();
						lat.setLat(Float.valueOf(place_.getLat()));
						placeType.setLat(lat);
					}

					// _long
					if (place_.getLon() != null) {
						_Long _long = new _Long();
						_long.setLong(Float.valueOf(place_.getLon()));
						placeType.setLong(_long);
					}

					// noteList					
					if (place_.getNotes() != null) {
						ArrayList<Note> noteList = new ArrayList<Note>();
						for (Label label : place_.getNotes()) {
							if (label != null) {
								Note note = new Note();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								note.setLang(lang);
								note.setString(label.getValue());
								noteList.add(note);
							}
						}		
						placeType.setNoteList(noteList);
					}
					
					
					// prefLabelList
					if (place_.getPrefLabelList() != null) {
						ArrayList<PrefLabel> prefLabelList = new ArrayList<PrefLabel>();
						for (Label label : place_.getPrefLabelList()) {
							if (label != null) {
								PrefLabel prefLabel = new PrefLabel();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								prefLabel.setLang(lang);
								prefLabel.setString(label.getValue());
								prefLabelList.add(prefLabel);
							}
						}
						placeType.setPrefLabelList(prefLabelList);
					}
					
					
					// sameAsList					
					if (place_.getSameAs() != null) {
						ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
						for (Part part : place_.getSameAs()) {
							if (part != null) {
								SameAs sameAs = new SameAs();						
								sameAs.setResource(part.getResource());												
								sameAsList.add(sameAs);
							}
						}
						placeType.setSameAList(sameAsList);
					}
								
					
					// isNextInSequence not available
				
					
					
					rdf.getPlaceList().add(placeType);
					
					
				
					try {
						System.out.println(EnrichmentUtils.convertRDFtoString(rdf));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
				}
				
				if (enrichmentBase.getClass() == Agent.class) {
					System.out.println("Got an agent");
					
					Agent agent = (Agent)enrichmentBase;
					AgentType agentType = new AgentType();
					
					// about
					agentType.setAbout(agent.getAbout());
					
					// altLabelList
					ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
					if (agent.getAltLabelList() != null) {
						for (Label label : agent.getAltLabelList()) {
							if (label != null) {
								AltLabel altLabel = new AltLabel();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								altLabel.setLang(lang);
								altLabel.setString(label.getValue());
								altLabelList.add(altLabel);
							}
						}
					}
					agentType.setAltLabelList(altLabelList);
					
					// begin
					ArrayList<Label> beginList = (ArrayList<Label>) agent.getBeginList();
					if (beginList != null && beginList.size()>0) {
						Begin begin = new Begin();
						Label label = beginList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							begin.setLang(lang);
							begin.setString(label.getValue());
						}
						agentType.setBegin(begin);
					}
					
					// biographicalInformation
					ArrayList<Label> bioInfoList = (ArrayList<Label>) agent.getBiographicaInformation();
					if (bioInfoList != null && bioInfoList.size()>0) {
						BiographicalInformation bioInfo = new BiographicalInformation();
						Label label = bioInfoList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							bioInfo.setLang(lang);
							bioInfo.setString(label.getValue());
						}
						agentType.setBiographicalInformation(bioInfo);
					}
						
					// dateList
					ArrayList<Date> dateList_ = new ArrayList<Date>();					
					ArrayList<Label> dateList = (ArrayList<Label>) agent.getBiographicaInformation();
					if (dateList != null && dateList.size()>0) {						
						Label label = dateList.get(0);
						Date date_ = new Date();
						if (label != null) {							
							ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
							lang.setLang(label.getLang());
							date_.setLang(lang);
							date_.setString(label.getValue());
						}			
						dateList_.add(date_);
					}
					agentType.setDateList(dateList_);
					
					// dateOfBirth
					ArrayList<Label> dateOfBirthList = (ArrayList<Label>) agent.getDateOfBirth();
					if (dateOfBirthList != null && dateOfBirthList.size()>0) {
						DateOfBirth dateOfBirth = new DateOfBirth();
						Label label = dateOfBirthList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							dateOfBirth.setLang(lang);
							dateOfBirth.setString(label.getValue());
						}
						agentType.setDateOfBirth(dateOfBirth);
					}
					
					// dateofDeath				
					ArrayList<Label> dateOfDeathList = (ArrayList<Label>) agent.getDateOfDeath();
					if (dateOfDeathList != null && dateOfDeathList.size()>0) {
						DateOfDeath dateOfDeath = new DateOfDeath();
						Label label = dateOfDeathList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							dateOfDeath.setLang(lang);
							dateOfDeath.setString(label.getValue());
						}
						agentType.setDateOfDeath(dateOfDeath);
					}
					
					// dateOfEstablishment
					ArrayList<Label> dateOfEstablishmentList = (ArrayList<Label>) agent.getDateOfEstablishment();
					if (dateOfEstablishmentList != null && dateOfEstablishmentList.size()>0) {
						DateOfEstablishment dateOfEstablishment = new DateOfEstablishment();
						Label label = dateOfEstablishmentList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							dateOfEstablishment.setLang(lang);
							dateOfEstablishment.setString(label.getValue());
						}
						agentType.setDateOfEstablishment(dateOfEstablishment);
					}
					
					// dateofTermination
					ArrayList<Label> dateOfTerminationList = (ArrayList<Label>) agent.getDateOfTermination();
					if (dateOfTerminationList != null && dateOfTerminationList.size()>0) {
						DateOfTermination dateOfTermination = new DateOfTermination();
						Label label = dateOfTerminationList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							dateOfTermination.setLang(lang);
							dateOfTermination.setString(label.getValue());
						}
						agentType.setDateOfTermination(dateOfTermination);
					}
					
					// end
					ArrayList<Label> endList = (ArrayList<Label>) agent.getEndList();
					if (endList != null && endList.size()>0) {
						End end = new End();
						Label label = endList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							end.setLang(lang);
							end.setString(label.getValue());
						}
						agentType.setEnd(end);
					}
					
					
					// gender
					ArrayList<Label> genderList = (ArrayList<Label>) agent.getGender();
					if (genderList != null && genderList.size()>0) {
						Gender gender = new Gender();
						Label label = genderList.get(0);
						if (label != null) {
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							gender.setLang(lang);
							gender.setString(label.getValue());
						}
						agentType.setGender(gender);
					}
					
					
					// hasMetList
					ArrayList<HasMet> hasMetList_ = new ArrayList<HasMet>();					
					ArrayList<Label> hasMetList = (ArrayList<Label>) agent.getHasMet();
					if (hasMetList != null && hasMetList.size()>0) {						
						Label label = hasMetList.get(0);
						HasMet hasMet_ = new HasMet();
						if (label != null)
							hasMet_.setResource(label.getValue());		
						hasMetList_.add(hasMet_);
					}
					agentType.setHasMetList(hasMetList_);
					
					// hasPartList not available
					
					// identifierList
					ArrayList<Identifier> identifierList_ = new ArrayList<Identifier>();					
					ArrayList<Label> identifierList = (ArrayList<Label>) agent.getIdentifier();
					if (identifierList != null && identifierList.size()>0) {						
						Label label = identifierList.get(0);
						Identifier identifier_ = new Identifier();
						if (label != null)
						{
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							identifier_.setLang(lang);
							identifier_.setString(label.getValue());
						}
						identifierList_.add(identifier_);
					}
					agentType.setIdentifierList(identifierList_);
					
					// isPartOfList not available
						
					// isRelatedToList
					ArrayList<IsRelatedTo> isRelatedToList_ = new ArrayList<IsRelatedTo>();					
					ArrayList<LabelResource> isRelatedToList = (ArrayList<LabelResource>) agent.getIsRelatedTo();
					if (isRelatedToList != null && isRelatedToList.size()>0) {						
						LabelResource labelResource = isRelatedToList.get(0);
						IsRelatedTo isRelatedTo_ = new IsRelatedTo();
						if (labelResource != null)
						{
							isRelatedTo_.setString(labelResource.getValue());
							ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
							lang.setLang(labelResource.getLang());
							isRelatedTo_.setLang(lang);	
							ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
							resrc.setResource(labelResource.getResource());
							isRelatedTo_.setResource(resrc);
						}
						isRelatedToList_.add(isRelatedTo_);
					}
					agentType.setIsRelatedToList(isRelatedToList_);
					
					// nameList not available
				
					// noteList
					ArrayList<Note> noteList_ = new ArrayList<Note>();					
					ArrayList<Label> noteList = (ArrayList<Label>) agent.getNotes();
					if (noteList != null && noteList.size()>0) {						
						Label label = noteList.get(0);
						Note note_ = new Note();
						if (label != null)
						{
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							note_.setLang(lang);
							note_.setString(label.getValue());
						}
						noteList_.add(note_);
					}
					agentType.setNoteList(noteList_);
					
					
					// placeofBirth not available
				
					// placeofDeath not available
							
					// prefLabelList
					ArrayList<PrefLabel> prefLabelList_ = new ArrayList<PrefLabel>();					
					ArrayList<Label> prefLabelList = (ArrayList<Label>) agent.getPrefLabelList();
					if (prefLabelList != null && prefLabelList.size()>0) {						
						Label label = prefLabelList.get(0);
						PrefLabel prefLabel_ = new PrefLabel();
						if (label != null)
						{
							LiteralType.Lang lang = new LiteralType.Lang();
							lang.setLang(label.getLang());
							prefLabel_.setLang(lang);
							prefLabel_.setString(label.getValue());
						}
						prefLabelList_.add(prefLabel_);
					}
					agentType.setPrefLabelList(prefLabelList_);
					
					
					// professionOrOccupationList
					ArrayList<ProfessionOrOccupation> professionOrOccupationList_ = new ArrayList<ProfessionOrOccupation>();					
					ArrayList<LabelResource> professionOrOccupationList = (ArrayList<LabelResource>) agent.getProfessionOrOccupation();
					if (professionOrOccupationList != null && professionOrOccupationList.size()>0) {						
						LabelResource labelResource = professionOrOccupationList.get(0);
						ProfessionOrOccupation professionOrOccupation_ = new ProfessionOrOccupation();
						if (labelResource != null)
						{
							professionOrOccupation_.setString(labelResource.getValue());
							ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
							lang.setLang(labelResource.getLang());
							professionOrOccupation_.setLang(lang);	
							ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
							resrc.setResource(labelResource.getResource());
							professionOrOccupation_.setResource(resrc);
						}
						professionOrOccupationList_.add(professionOrOccupation_);
					}
					agentType.setProfessionOrOccupationList(professionOrOccupationList_);
					
					// sameAsList
					ArrayList<SameAs> sameAsList_ = new ArrayList<SameAs>();					
					ArrayList<Part> sameAsList = (ArrayList<Part>) agent.getSameAs();
					if (sameAsList != null && sameAsList.size()>0) {						
						Part part = sameAsList.get(0);
						SameAs sameAs_ = new SameAs();
						if (part != null)
							sameAs_.setResource(part.getResource());
						sameAsList_.add(sameAs_);
					}
					agentType.setSameAList(sameAsList_);
				}
				
				if (enrichmentBase.getClass() == Concept.class) {
					System.out.println("Got a concept");
					
					Concept concept = (Concept)enrichmentBase;
					eu.europeana.corelib.definitions.jibx.Concept concept_ = new eu.europeana.corelib.definitions.jibx.Concept();
					
					// about
					concept_.setAbout(concept.getAbout());
					
					// choiceList
					ArrayList<Choice> choiceList_ = new ArrayList<Choice>();
								
					Choice choice_ = new Choice();
					
					ArrayList<Label> altLabelList = (ArrayList<Label>) concept.getAltLabelList();
					AltLabel altLabel_ = new AltLabel();
					if (altLabelList != null) {
						Label altLabel = altLabelList.get(0);
						LiteralType.Lang lang1 = new LiteralType.Lang();
						lang1.setLang(altLabel.getLang());
						altLabel_.setLang(lang1);
						altLabel_.setString(altLabel.getValue());
					}
					choice_.setAltLabel(altLabel_);
										
					ArrayList<Resource> broadMatch = (ArrayList<Resource>)concept.getBroadMatch();
					BroadMatch broadMatch_ = new BroadMatch();
					if (broadMatch != null)
						broadMatch_.setResource(broadMatch.get(0).getResource());
					choice_.setBroadMatch(broadMatch_);
			
					ArrayList<Resource> broader = (ArrayList<Resource>)concept.getBroader();
					Broader broader_ = new Broader();
					if (broader != null)
						broader_.setResource(broader.get(0).getResource());
					choice_.setBroader(broader_);
					
					ArrayList<Resource> closeMatch = (ArrayList<Resource>)concept.getCloseMatch();
					CloseMatch closeMatch_ = new CloseMatch();
					if (closeMatch != null)
						closeMatch_.setResource(closeMatch.get(0).getResource());
					choice_.setCloseMatch(closeMatch_);
					
					ArrayList<Resource> exactMatch = (ArrayList<Resource>)concept.getExactMatch();
					ExactMatch exactMatch_ = new ExactMatch();
					if (exactMatch != null)
						exactMatch_.setResource(exactMatch.get(0).getResource());
					choice_.setExactMatch(exactMatch_);
					
					ArrayList<Resource> inScheme = (ArrayList<Resource>)concept.getInScheme();
					InScheme inScheme_ = new InScheme();
					if (inScheme != null)
						inScheme_.setResource(inScheme.get(0).getResource());
					choice_.setInScheme(inScheme_);
					
					ArrayList<Resource> narrower = (ArrayList<Resource>)concept.getNarrower();
					Narrower narrower_ = new Narrower();
					if (narrower != null)
						narrower_.setResource(narrower.get(0).getResource());
					choice_.setNarrower(narrower_);
					
					ArrayList<Resource> narrowMatch = (ArrayList<Resource>)concept.getNarrowMatch();
					NarrowMatch narrowMatch_ = new NarrowMatch();
					if (narrowMatch != null)
						narrowMatch_.setResource(narrowMatch.get(0).getResource());
					choice_.setNarrowMatch(narrowMatch_);
					
					ArrayList<Label> notationList = (ArrayList<Label>) concept.getNotation();
					Notation notation_ = new Notation();
					if (notationList != null) {
						Label notation = notationList.get(0);
						LiteralType.Lang lang2 = new LiteralType.Lang();
						lang2.setLang(notation.getLang());
						notation_.setLang(lang2);
						notation_.setString(notation.getValue());
					}
					choice_.setNotation(notation_);
					
					ArrayList<Label> noteList = (ArrayList<Label>) concept.getNotes();
					Note note_ = new Note();
					if (noteList != null) {
						Label note = noteList.get(0);
						LiteralType.Lang lang3 = new LiteralType.Lang();
						lang3.setLang(note.getLang());
						note_.setLang(lang3);
						note_.setString(note.getValue());
					}
					choice_.setNote(note_);
					
					ArrayList<Label> prefLabelList = (ArrayList<Label>) concept.getPrefLabelList();
					PrefLabel prefLabel_ = new PrefLabel();
					if (prefLabelList != null) {
						Label prefLabel = prefLabelList.get(0);
						LiteralType.Lang lang4 = new LiteralType.Lang();
						lang4.setLang(prefLabel.getLang());
						prefLabel_.setLang(lang4);
						prefLabel_.setString(prefLabel.getValue());
					}
					choice_.setPrefLabel(prefLabel_);
					
					ArrayList<Resource> related = (ArrayList<Resource>)concept.getRelated();
					Related related_ = new Related();
					if (related != null)
						related_.setResource(related.get(0).getResource());
					choice_.setRelated(related_);
					
					ArrayList<Resource> relatedMatch = (ArrayList<Resource>)concept.getRelatedMatch();
					RelatedMatch relatedMatch_ = new RelatedMatch();
					if (relatedMatch != null)
						relatedMatch_.setResource(relatedMatch.get(0).getResource());
					choice_.setRelatedMatch(relatedMatch_);
					
					choiceList_.add(choice_);
					concept_.setChoiceList(choiceList_);
					ArrayList<eu.europeana.corelib.definitions.jibx.Concept> conceptList = new ArrayList<eu.europeana.corelib.definitions.jibx.Concept>();
					conceptList.add(concept_);
					rdf.setConceptList(conceptList);
				}
				
				if (enrichmentBase.getClass() == Timespan.class) {
					System.out.println("Got a timespan");
					
					Timespan timespan = (Timespan)enrichmentBase;
					TimeSpanType timeSpanType = new TimeSpanType();
					
					
					
					// about
					timeSpanType.setAbout(timespan.getAbout());
					
					// altLabelList
					ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
					if (timespan.getAltLabelList() != null) {
						for (Label label : timespan.getAltLabelList()) {
							if (label != null) {
								AltLabel altLabel = new AltLabel();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								altLabel.setLang(lang);
								altLabel.setString(label.getValue());
								altLabelList.add(altLabel);
							}
						}
					}
					timeSpanType.setAltLabelList(altLabelList);
					
					// begin		
					ArrayList<Label> beginList = (ArrayList<Label>) timespan.getBeginList();
					Begin begin_ = new Begin();
					if (beginList != null) {
						Label begin = beginList.get(0);
						LiteralType.Lang lang2 = new LiteralType.Lang();
						lang2.setLang(begin.getLang());
						begin_.setLang(lang2);
						begin_.setString(begin.getValue());
					}
					timeSpanType.setBegin(begin_);
				
					
					// end		
					ArrayList<Label> endList = (ArrayList<Label>) timespan.getEndList();
					End end_ = new End();
					if (endList != null) {
						Label end = endList.get(0);
						LiteralType.Lang lang2 = new LiteralType.Lang();
						lang2.setLang(end.getLang());
						end_.setLang(lang2);
						end_.setString(end.getValue());
					}
					timeSpanType.setEnd(end_);
					
					
					// hasPartList
					ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
					if (timespan.getHasPartsList() != null) {
						for (Part part : timespan.getHasPartsList()) {
							if (part != null) {
								HasPart hasPart = new HasPart();
								ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
								resrc.setResource(part.getResource());
								hasPart.setResource(resrc);
								hasPartList.add(hasPart);
							}
						}
					}
					timeSpanType.setHasPartList(hasPartList);
					
					
					// isNextInSequence not available
				
					// isPartOfList
					ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
					if (timespan.getIsPartOfList() != null) {
						for (Part part : timespan.getIsPartOfList()) {
							if (part != null) {
								IsPartOf isPartOf = new IsPartOf();
								ResourceOrLiteralType.Resource resrc = new ResourceOrLiteralType.Resource();
								resrc.setResource(part.getResource());
								isPartOf.setResource(resrc);
								isPartOfList.add(isPartOf);
							}
						}
					}
					timeSpanType.setIsPartOfList(isPartOfList);
					
					
					// noteList
					ArrayList<Note> noteList = new ArrayList<Note>();
					if (timespan.getNotes() != null) {
						for (Label label : timespan.getNotes()) {
							if (label != null) {
								Note note = new Note();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								note.setLang(lang);
								note.setString(label.getValue());								
								noteList.add(note);
							}
						}
					}
					timeSpanType.setNoteList(noteList);
					
					
					// prefLabelList
					ArrayList<PrefLabel> prefLabelList = new ArrayList<PrefLabel>();
					if (timespan.getPrefLabelList() != null) {
						for (Label label : timespan.getPrefLabelList()) {
							if (label != null) {
								PrefLabel prefLabel = new PrefLabel();
								LiteralType.Lang lang = new LiteralType.Lang();
								lang.setLang(label.getLang());
								prefLabel.setLang(lang);
								prefLabel.setString(label.getValue());
								prefLabelList.add(prefLabel);
							}
						}
					}
					timeSpanType.setPrefLabelList(prefLabelList);
					
					
					// sameAsList
					ArrayList<SameAs> sameAsList = new ArrayList<SameAs>();
					if (timespan.getSameAs() != null) {
						for (Part part : timespan.getSameAs()) {
							if (part != null) {
								SameAs sameAs = new SameAs();
								sameAs.setResource(part.getResource());
								sameAsList.add(sameAs);
							}
						}
					}
					timeSpanType.setSameAList(sameAsList);
				}
			}
			
			System.out.println();
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
			
		/*
		// Merge the dereferenced information fields back in.
		count = 1;
		for (String enrichmentResult : dereferenceInformation) {
			System.out.println(count);
			
			try {
				EnrichmentUtils.mergeEntityForEnrichment(
						EnrichmentUtils.convertRDFtoString(rdf), 
						enrichmentResult, 
						"");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			count++;
		}
		*/
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		String newSample = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
		"<metis:results xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:rdaGr2=\"http://RDVocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema\" xmlns:cc=\"http://creativecommons.org/ns\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:metis=\"http://www.europeana.eu/schemas/metis\">"+
		  "<edm:Place rdf:about=\"http://sws.geonames.org/3020251/\">"+
		    "<skos:altLabel xml:lang=\"la\">Aebura</skos:altLabel>"+
		    "<skos:altLabel xml:lang=\"oc\">Ambrun</skos:altLabel>"+
		    "<skos:altLabel xml:lang=\"la\">Eburodunum</skos:altLabel>"+
		    "<skos:prefLabel>Embrun</skos:prefLabel>"+
		    "<skos:note></skos:note>"+
		    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/3017382/\"/>"+
		    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/6446638/\"/>"+
		    "<wgs84_pos:lat>44.56387</wgs84_pos:lat>"+
		    "<wgs84_pos:long>6.49526</wgs84_pos:long>"+
		  "</edm:Place>"+
		"</metis:results>";
		
		String newSample2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
				"<rdf:RDF xmlns=\"http://www.europeana.eu/schemas/edm/\""+
				  "xmlns:dc=\"http://purl.org/dc/elements/1.1/\""+
				  "xmlns:dcterms=\"http://purl.org/dc/terms/\""+
				  "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\""+
				  "xmlns:enrichment=\"http://www.europeana.eu/schemas/edm/enrichment\""+
				  "xmlns:europeana=\"http://www.europeana.eu/schemas/ese/\""+
				  "xmlns:oai=\"http://www.openarchives.org/OAI/2.0\""+
				  "xmlns:ore=\"http://www.openarchives.org/ore/terms/\""+
				  "xmlns:owl=\"http://www.w3.org/2002/07/owl#\""+
				  "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+
				  "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\""+
				  "xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
				  "<edm:Place rdf:about=\"http://sws.geonames.org/3020251/\">"+
				    "<skos:altLabel xml:lang=\"la\">Aebura</skos:altLabel>"+
				    "<skos:altLabel xml:lang=\"oc\">Ambrun</skos:altLabel>"+
				    "<skos:altLabel xml:lang=\"la\">Eburodunum</skos:altLabel>"+
				    "<skos:prefLabel>Embrun</skos:prefLabel>"+
				    "<skos:note></skos:note>"+
				    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/3017382/\"/>"+
				    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/6446638/\"/>"+
				    "<wgs84_pos:lat>44.56387</wgs84_pos:lat>"+
				    "<wgs84_pos:long>6.49526</wgs84_pos:long>"+
				  "</edm:Place>"+
				"</rdf:RDF>";		
		
		String newSample3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
		"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\">"+
		  "<edm:Place rdf:about=\"Testing PlaceType\"/>"+
		"</rdf:RDF>";
		
		String newSample4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
				  "<edm:Place rdf:about=\"Testing PlaceType\"/>";
			
		String newSample5 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+
				"<rdf:RDF xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:rdaGr2=\"http://RDVocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema\" xmlns:cc=\"http://creativecommons.org/ns\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:metis=\"http://www.europeana.eu/schemas/metis\">"+
				  "<edm:Place rdf:about=\"http://sws.geonames.org/3020251/\">"+
				    "<skos:altLabel xml:lang=\"la\">Aebura</skos:altLabel>"+
				    "<skos:altLabel xml:lang=\"oc\">Ambrun</skos:altLabel>"+
				    "<skos:altLabel xml:lang=\"la\">Eburodunum</skos:altLabel>"+
				    "<skos:prefLabel>Embrun</skos:prefLabel>"+
				    "<skos:note></skos:note>"+
				    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/3017382/\"/>"+
				    "<dcterms:isPartOf rdf:resource=\"http://sws.geonames.org/6446638/\"/>"+
				    "<wgs84_pos:lat>44.56387</wgs84_pos:lat>"+
				    "<wgs84_pos:long>6.49526</wgs84_pos:long>"+
				  "</edm:Place>"+
				"</rdf:RDF>";		
		
		/*
		
		PlaceType p = new PlaceType();
		p.setAbout("Testing PlaceType");
		
		ArrayList<PlaceType> pl = new ArrayList<PlaceType>();
		pl.add(p);
		
		RDF r = new RDF();
		r.setPlaceList(pl);
		
		AboutType a = new AboutType();
		
		IMarshallingContext ctx = placeFactory.createMarshallingContext();
		ctx.setIndent(2);
		StringWriter stringWriter = new StringWriter();
		ctx.setOutput(stringWriter);
		ctx.marshalDocument(p, "UTF-8", null);
		String output = stringWriter.toString();
		
		System.out.println(output);
		*/
	
		/*
		try {
			RDF r = DereferenceUtils.mergeEntityForDereferencing(
					EnrichmentUtils.convertRDFtoString(rdf), 
					newSample3);

			System.out.println(r.getPlaceList().get(0).getAbout());
			System.out.println(r.getPlaceList().get(1).getAbout());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		

		/*
		// Merge the dereferenced information fields back in.
		for (EnrichmentResultList enrichmentResultList : dereferenceInformation) {
			ArrayList<EnrichmentBase> enrichmentBaseList = (ArrayList<EnrichmentBase>) enrichmentResultList.getResult();

			for (EnrichmentBase enrichmentBase : enrichmentBaseList) {
				if (enrichmentBase.getClass() == Place.class) {
					System.out.println("GOT A PLACE");
					Place eBPlace = (Place)enrichmentBase;

					PlaceType placeType = new PlaceType();
					placeType.setAbout(eBPlace.getAbout());

					Alt alt = new Alt();

					String eBPlaceAlt = eBPlace.getAlt();
					if (eBPlaceAlt != null)
						alt.setAlt(Float.valueOf(eBPlace.getAlt()));
					else
						alt.setAlt(0f);
					placeType.setAlt(alt);

					ArrayList<AltLabel> altLabelList = new ArrayList<AltLabel>();
					for (Label label : eBPlace.getAltLabelList()) {
						AltLabel altLabel = new AltLabel();

						LiteralType.Lang lang = new LiteralType.Lang();
						lang.setLang(label.getLang());
						altLabel.setLang(lang);

						altLabel.setString(label.getValue());

						altLabelList.add(altLabel);
					}		
					placeType.setAltLabelList(altLabelList);

					ArrayList<HasPart> hasPartList = new ArrayList<HasPart>();
					for (Part part : eBPlace.getHasPartsList()) {
						HasPart hasPart = new HasPart();

						Resource resrc = new Resource();
						resrc.setResource(part.getResource());
						hasPart.setResource(resrc);

						hasPartList.add(hasPart);
					}		
					placeType.setHasPartList(hasPartList);

					ArrayList<IsPartOf> isPartOfList = new ArrayList<IsPartOf>();
					for (Part part : eBPlace.getIsPartOfList()) {
						IsPartOf isPartOf = new IsPartOf();

						Resource resrc = new Resource();
						resrc.setResource(part.getResource());
						isPartOf.setResource(resrc);

						ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
						lang.setLang("");							
						isPartOf.setLang(lang);

						isPartOf.setString("");

						isPartOfList.add(isPartOf);
					}		
					placeType.setIsPartOfList(isPartOfList);		

					Lat lat = new Lat();
					lat.setLat(Float.valueOf(eBPlace.getLat()));
					placeType.setLat(lat);

					_Long _long = new _Long();
					_long.setLong(Float.valueOf(eBPlace.getLon()));
					placeType.setLong(_long);

					ArrayList<Note> noteList = new ArrayList<Note>();
					for (Label label : eBPlace.getNotes()) {
						Note note = new Note();

						LiteralType.Lang lang = new LiteralType.Lang();
						lang.setLang(label.getLang());
						note.setLang(lang);

						note.setString(label.getValue());

						noteList.add(note);
					}		
					placeType.setNoteList(noteList);

					rdf.getPlaceList().add(placeType);
				}
				if (enrichmentBase.getClass() == Agent.class) {
					System.out.println("GOT AN AGENT");
				}
				if (enrichmentBase.getClass() == Timespan.class) {
					System.out.println("GOT A TIMESPAN");
				}
				if (enrichmentBase.getClass() == Timespan.class) {
					System.out.println("GOT A CONCEPT");
				}
			}
		}
		*/
		
	}
}
