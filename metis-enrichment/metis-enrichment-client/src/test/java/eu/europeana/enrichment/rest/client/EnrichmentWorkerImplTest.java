package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.rest.client.dereference.DereferencerProvider;
import eu.europeana.enrichment.rest.client.enrichment.EnricherProvider;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.ProcessEnriched;
import eu.europeana.enrichment.rest.client.report.ProcessedEncrichment;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.dereference.DereferencerImpl;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnricherImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.metis.schema.convert.SerializationException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnrichmentWorkerImplTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImplTest.class);

  private static Stream<Arguments> providedMapException() {
    return Stream.of(
        //        Arguments.of("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:svcs=\"http://rdfs.org/sioc/services#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
        //            + "   <edm:ProvidedCHO rdf:about=\"/262/C_PY_001146179\"/><edm:TimeSpan rdf:about=\"#1988\"><skos:prefLabel xml:lang=\"zxx\">1988</skos:prefLabel><dcterms:isPartOf rdf:resource=\"http://data.europeana.eu/timespan/20\"/><edm:begin>1988-01-01</edm:begin><edm:end>1988-12-31</edm:end><skos:notation rdf:datatype=\"http://id.loc.gov/datatypes/edtf/EDTF-level1\">1988</skos:notation></edm:TimeSpan><edm:TimeSpan rdf:about=\"#1988\"><skos:prefLabel xml:lang=\"zxx\">1988</skos:prefLabel><dcterms:isPartOf rdf:resource=\"http://data.europeana.eu/timespan/20\"/><edm:begin>1988-01-01</edm:begin><edm:end>1988-12-31</edm:end><skos:notation rdf:datatype=\"http://id.loc.gov/datatypes/edtf/EDTF-level1\">1988</skos:notation></edm:TimeSpan>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/provider/262/C_PY_001146179\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/262/C_PY_001146179\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"cs\">Archeologický informační systém České republiky</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"https://digiarchiv.aiscr.cz/id/C-PY-001146179\"/>\n"
        //            + "      <edm:isShownBy rdf:resource=\"https://digiarchiv.aiscr.cz/img?full=true&amp;id=1520979273537_CPY001146179.tif\"/>\n"
        //            + "      <edm:provider>CARARE</edm:provider>\n"
        //            + "      <edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc/4.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/provider/262/C_PY_001146179\">\n"
        //            + "      <dc:creator xml:lang=\"cs\">Čížek, Jiří</dc:creator>\n"
        //            + "      <dc:date>1988</dc:date>\n"
        //            + "      <dc:description>Hrádky a tvrze v Čechách. Jižní Čechy - Dodatky. Tvrziště Vlčetín.</dc:description>\n"
        //            + "      <dc:language>CS</dc:language>\n"
        //            + "      <dc:publisher rdf:resource=\"http://www.aiscr.cz/\"/>\n"
        //            + "      <dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300054328\"/>\n"
        //            + "      \n"
        //            + "      <dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300034122\"/>\n"
        //            + "      <dc:title xml:lang=\"cs\">C-PY-001146179 - plán lokality</dc:title>\n"
        //            + "      <dc:type xml:lang=\"cs\">plán lokality</dc:type>\n"
        //            + "      <dcterms:created>1988</dcterms:created>\n"
        //            + "      <dcterms:isPartOf>Europeana Archaeology</dcterms:isPartOf>\n"
        //            + "      <dcterms:medium xml:lang=\"cs\">papír sešit</dcterms:medium>\n"
        //            + "      <dcterms:provenance xml:lang=\"cs\">Uložení originálu: archiv ARÚP</dcterms:provenance>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/262/C_PY_001146179\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/provider/262/C_PY_001146179\"/>\n"
        //            + "      <edm:type>IMAGE</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/europeana/262/C_PY_001146179\"><dc:date rdf:resource=\"#1988\"/><dcterms:created rdf:resource=\"#1988\"/>\n"
        //            + "      <dc:identifier>C-PY-001146179</dc:identifier><dc:language>ces</dc:language>\n"
        //            + "      <edm:europeanaProxy>true</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/262/C_PY_001146179\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/europeana/262/C_PY_001146179\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/262/C_PY_001146179\"/>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <edm:EuropeanaAggregation rdf:about=\"/aggregation/europeana/262/C_PY_001146179\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/262/C_PY_001146179\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">Europeana Foundation</edm:dataProvider>\n"
        //            + "      <edm:provider xml:lang=\"en\">Europeana Foundation</edm:provider>\n"
        //            + "      <edm:datasetName>262_CARARE_AISCR</edm:datasetName>\n"
        //            + "      <edm:country>Czech Republic</edm:country>\n"
        //            + "      <edm:language>cs</edm:language>\n"
        //            + "   </edm:EuropeanaAggregation>\n"
        //            + "</rdf:RDF>"),
        //        Arguments.of("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:svcs=\"http://rdfs.org/sioc/services#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
        //            + "   <edm:ProvidedCHO rdf:about=\"/262/C_TX_201400843\"/>\n"
        //            + "   <edm:Place rdf:about=\"C-TX-201400843-D01#place\">\n"
        //            + "      <skos:prefLabel xml:lang=\"cs\">DOLNÍ TŘEBONÍN</skos:prefLabel>\n"
        //            + "      <skos:note xml:lang=\"cs\">Samoty na J straně Věncové hory, okolí menšího vrcholu masivu - Plechaté hory.</skos:note>\n"
        //            + "      <dcterms:isPartOf xml:lang=\"cs\">ČESKÝ KRUMLOV</dcterms:isPartOf>\n"
        //            + "   </edm:Place><edm:TimeSpan rdf:about=\"#2013\"><skos:prefLabel xml:lang=\"zxx\">2013</skos:prefLabel><dcterms:isPartOf rdf:resource=\"http://data.europeana.eu/timespan/21\"/><edm:begin>2013-01-01</edm:begin><edm:end>2013-12-31</edm:end><skos:notation rdf:datatype=\"http://id.loc.gov/datatypes/edtf/EDTF-level1\">2013</skos:notation></edm:TimeSpan><edm:TimeSpan rdf:about=\"#2013\"><skos:prefLabel xml:lang=\"zxx\">2013</skos:prefLabel><dcterms:isPartOf rdf:resource=\"http://data.europeana.eu/timespan/21\"/><edm:begin>2013-01-01</edm:begin><edm:end>2013-12-31</edm:end><skos:notation rdf:datatype=\"http://id.loc.gov/datatypes/edtf/EDTF-level1\">2013</skos:notation></edm:TimeSpan>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/provider/262/C_TX_201400843\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/262/C_TX_201400843\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"cs\">Archeologický informační systém České republiky</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"https://digiarchiv.aiscr.cz/id/C-TX-201400843\"/>\n"
        //            + "      <edm:provider>CARARE</edm:provider>\n"
        //            + "      <edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc/4.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/provider/262/C_TX_201400843\">\n"
        //            + "      <dc:creator xml:lang=\"cs\">Valkony, Jiří</dc:creator>\n"
        //            + "      <dc:creator xml:lang=\"cs\">Archaia Jih</dc:creator>\n"
        //            + "      <dc:date>2013</dc:date>\n"
        //            + "      <dc:description>Kabel NN, Věncová hora.</dc:description>\n"
        //            + "      <dc:language>CS</dc:language>\n"
        //            + "      <dc:publisher rdf:resource=\"http://www.aiscr.cz/\"/>\n"
        //            + "      <dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300054328\"/>\n"
        //            + "      \n"
        //            + "      <dc:subject rdf:resource=\"http://vocab.getty.edu/aat/300027267\"/>\n"
        //            + "      <dc:title xml:lang=\"cs\">C-TX-201400843 - nálezová zpráva</dc:title>\n"
        //            + "      <dc:type xml:lang=\"cs\">nálezová zpráva</dc:type>\n"
        //            + "      <dcterms:created>2013</dcterms:created>\n"
        //            + "      <dcterms:isPartOf>Europeana Archaeology</dcterms:isPartOf>\n"
        //            + "      <dcterms:medium xml:lang=\"cs\">papír</dcterms:medium>\n"
        //            + "      <dcterms:provenance xml:lang=\"cs\">Uložení originálu: archiv ARÚP</dcterms:provenance>\n"
        //            + "      <dcterms:spatial rdf:resource=\"C-TX-201400843-D01#place\"/>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/262/C_TX_201400843\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/provider/262/C_TX_201400843\"/>\n"
        //            + "      <edm:type>TEXT</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/europeana/262/C_TX_201400843\"><dc:date rdf:resource=\"#2013\"/><dcterms:created rdf:resource=\"#2013\"/>\n"
        //            + "      <dc:identifier>C-TX-201400843</dc:identifier><dc:language>ces</dc:language>\n"
        //            + "      <edm:europeanaProxy>true</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/262/C_TX_201400843\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/europeana/262/C_TX_201400843\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/262/C_TX_201400843\"/>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <edm:EuropeanaAggregation rdf:about=\"/aggregation/europeana/262/C_TX_201400843\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/262/C_TX_201400843\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">Europeana Foundation</edm:dataProvider>\n"
        //            + "      <edm:provider xml:lang=\"en\">Europeana Foundation</edm:provider>\n"
        //            + "      <edm:datasetName>262_CARARE_AISCR</edm:datasetName>\n"
        //            + "      <edm:country>Czech Republic</edm:country>\n"
        //            + "      <edm:language>cs</edm:language>\n"
        //            + "   </edm:EuropeanaAggregation>\n"
        //            + "</rdf:RDF>"),
        //        Arguments.of("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:svcs=\"http://rdfs.org/sioc/services#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
        //            + "   <edm:ProvidedCHO rdf:about=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/provider/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <edm:dataProvider>Lietuvos Centrinis Valstybés Archyvas</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"http://www.e-kinas.lt/objektas/kinas/0963/fly-white-swans\"/>\n"
        //            + "      <edm:object rdf:resource=\"http://www.e-kinas.lt/search-results-order/ioresource/16214?type=image\"/>\n"
        //            + "      <edm:provider xml:lang=\"en\">EFG - The European Film Gateway</edm:provider>\n"
        //            + "      <dc:rights>Lietuvos Centrinis Valstybes Archyvas</dc:rights>\n"
        //            + "      <edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/aggregator/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">EFG - The European Film Gateway</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"http://www.e-kinas.lt/objektas/kinas/0963/fly-white-swans\"/>\n"
        //            + "      <edm:provider xml:lang=\"en\">EFG - The European Film Gateway</edm:provider>\n"
        //            + "      <edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/provider/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <dc:contributor>Rimantas Budrys</dc:contributor>\n"
        //            + "      <dc:contributor>Robertas Verba</dc:contributor>\n"
        //            + "      <dc:creator>Algirdas Tumas</dc:creator>\n"
        //            + "      <dc:description>Filme pasakojama apie laukinius gyvūnus, kurie susiduria su sunkumais žiemą ir pavasarį. Gyvūnams išgyventi šiais sezonais padeda žmogus. Žiemą jis veža į mišką maistą ir kovoja su plėšrūnais, o pavasarį gelbėja skęstančius žvėrelius. Gamtos vaizdai žiemą. Stirnos, elniai, briedžiai, šernai, kiškiai ir kiti gyvūnai žiemos metu. Vyras rogėmis veža maistą žvėreliams. Miške medžioja medžiotojai. Nušaunamas vilkas. Sodo bityne vaikšto kurapkos, vyras joms beria pašarą. Žmonės gelbėja lede įšalusią gulbę (Ventės ragas?). Skyla ledas. Ledonešis. Potvynis. Gyvūnai gelbėjasi iš apsemtų vietų. Virš užlietų laukų skrenda paukščiai. Žmonės surenka gyvūnus iš apsemtų vietų. Gandras. Dega laužai. Žmonės paleidžia išgelbėtus gyvūnus. Vyras paleidžia į laisvę išgelbėtą gulbę. Skrenda gulbės.</dc:description>\n"
        //            + "      <dc:description xml:lang=\"en\">Documentary tells about wild animals which face with difficulties in the winter and spring. Animals are helped to survive by man. In winter a man carries food into the forest and fights with predators, and in the spring a man saves drowning animals. Views of nature in winter. Roe deers, deers, elks, wild boars, hares and other animals during the winter. A man transports food for animals by sled. Hunters in the forest. Wolf is shot down. Partridges walk in the apiary of a garden. A man pours forage. People save the swan frozen in the ice. Ice splits. Ice run. Flood. Animals rescue themselves from flooded places. Birds over the flooded fields. People gather animals from flooded places. Stork. Bonfires burn. People let the saved animals go. A man releases the saved swan. Swans fly.</dc:description>\n"
        //            + "      <dc:identifier>f7746ee8a5c74886b3ed1c555f4025a1</dc:identifier>\n"
        //            + "      <dc:subject xml:lang=\"lt\">Augalija</dc:subject>\n"
        //            + "      <dc:subject xml:lang=\"lt\">Gamta</dc:subject>\n"
        //            + "      <dc:subject xml:lang=\"lt\">Gyvūnija</dc:subject>\n"
        //            + "      <dc:subject xml:lang=\"en\">Nature</dc:subject>\n"
        //            + "      <dc:title>Skriskite, baltosios gulbės</dc:title>\n"
        //            + "      <dc:type rdf:resource=\"http://vocab.getty.edu/aat/300136900\"/>\n"
        //            + "      <dcterms:alternative xml:lang=\"en\">Fly, white swans</dcterms:alternative>\n"
        //            + "      <dcterms:created xml:lang=\"en\">1965</dcterms:created>\n"
        //            + "      <dcterms:isPartOf xml:lang=\"en\">Europeana XX: Century of Change</dcterms:isPartOf>\n"
        //            + "      <dcterms:provenance>Lietuvos Centrinis Valstybés Archyvas</dcterms:provenance>\n"
        //            + "      <dcterms:spatial rdf:resource=\"https://sws.geonames.org/597427/\"/>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/provider/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <edm:type>VIDEO</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/aggregator/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <dc:contributor rdf:resource=\"http://www.wikidata.org/entity/Q16471109\"/>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/aggregator/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <edm:type>VIDEO</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/europeana/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <dc:identifier>f7746ee8a5c74886b3ed1c555f4025a1</dc:identifier>\n"
        //            + "      <edm:europeanaProxy>true</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/europeana/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/aggregator/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <edm:EuropeanaAggregation rdf:about=\"/aggregation/europeana/08635/f7746ee8a5c74886b3ed1c555f4025a1\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/f7746ee8a5c74886b3ed1c555f4025a1\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">Europeana Foundation</edm:dataProvider>\n"
        //            + "      <edm:provider xml:lang=\"en\">Europeana Foundation</edm:provider>\n"
        //            + "      <edm:datasetName>08635_EFG_LCVA</edm:datasetName>\n"
        //            + "      <edm:country>Lithuania</edm:country>\n"
        //            + "      <edm:language>mul</edm:language>\n"
        //            + "   </edm:EuropeanaAggregation>\n"
        //            + "</rdf:RDF>"),
        //        Arguments.of("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:svcs=\"http://rdfs.org/sioc/services#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
        //            + "   <edm:ProvidedCHO rdf:about=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/provider/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <edm:dataProvider>Lietuvos Centrinis Valstybés Archyvas</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"http://www.e-kinas.lt/objektas/kinas/3085-16/lithuanian-national-costumes-dream-in-blooming-garden\"/>\n"
        //            + "      <edm:object rdf:resource=\"http://www.e-kinas.lt/search-results-order/ioresource/9179?type=image\"/>\n"
        //            + "      <edm:provider xml:lang=\"en\">EFG - The European Film Gateway</edm:provider>\n"
        //            + "      <dc:rights>Lietuvos Centrinis Valstybes Archyvas</dc:rights>\n"
        //            + "      <edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Aggregation rdf:about=\"/aggregation/aggregator/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">EFG - The European Film Gateway</edm:dataProvider>\n"
        //            + "      <edm:isShownAt rdf:resource=\"http://www.e-kinas.lt/objektas/kinas/3085-16/lithuanian-national-costumes-dream-in-blooming-garden\"/>\n"
        //            + "      <edm:provider xml:lang=\"en\">EFG - The European Film Gateway</edm:provider>\n"
        //            + "      <edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>\n"
        //            + "   </ore:Aggregation>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/provider/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <dc:contributor>Antanas Tamošaitis</dc:contributor>\n"
        //            + "      <dc:contributor>Kazys ir Mečys Motūzai (Matuzai)</dc:contributor>\n"
        //            + "      <dc:creator>Kazys ir Mečys Motūzai (Matuzai)</dc:creator>\n"
        //            + "      <dc:description>Užrašas: vilnietė Vilnius, aukštaitė Panevėžys, aukštaitė Biržai, žemaitė Telšiai, žemaitė Raseiniai, dzūkė Alytus, zanavykė Šakiai, zanavykė Griškabūdis, kapsė Vilkaviškis, mažlietuvė Pagėgiai. Žydintis sodas. Obelų šakos. Skamba muzika. Sode prie stalelio sėdi tautodailininkas Antanas Tamošaitis ir žiūrinėja lietuvių moterų tautinių drabužių eskizus. A. Tamošaitis užsnūsta ir sapne regi – lietuvės moterys apsirengusios jo sukurtais Aukštaitijos, Žemaitijos, Suvalkijos, Dzūkijos ir Klaipėdos kraštų tautiniais rūbais.</dc:description>\n"
        //            + "      <dc:description xml:lang=\"en\">The essay about Lithuanian national costumes from different regions. Folk artist Antanas Tamošaitis sits in the blooming garden and looks at the sketches of girls' national costumes. He falls asleep and has a dream where Lithuanian girls wear national costumes of different regions.</dc:description>\n"
        //            + "      <dc:identifier>34afe0df15644a48bbb9a50278ba756c</dc:identifier>\n"
        //            + "      <dc:subject xml:lang=\"lt\">Tautodailė</dc:subject>\n"
        //            + "      <dc:title>Lietuvių moterų tautiniai drabužiai (Sapnas žydinčiam sode)</dc:title>\n"
        //            + "      <dc:type rdf:resource=\"http://vocab.getty.edu/aat/300136900\"/>\n"
        //            + "      <dcterms:alternative xml:lang=\"en\">Lithuanian national costumes (Dream in blooming garden)</dcterms:alternative>\n"
        //            + "      <dcterms:created xml:lang=\"en\">1938</dcterms:created>\n"
        //            + "      <dcterms:isPartOf xml:lang=\"en\">Europeana XX: Century of Change</dcterms:isPartOf>\n"
        //            + "      <dcterms:provenance>Lietuvos Centrinis Valstybés Archyvas</dcterms:provenance>\n"
        //            + "      <dcterms:spatial rdf:resource=\"https://sws.geonames.org/597427/\"/>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/provider/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <edm:type>VIDEO</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/aggregator/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <dcterms:spatial rdf:resource=\"http://www.wikidata.org/entity/Q216\"/>\n"
        //            + "      <dcterms:spatial rdf:resource=\"http://www.wikidata.org/entity/Q37\"/>\n"
        //            + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/aggregator/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <edm:type>VIDEO</edm:type>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <ore:Proxy rdf:about=\"/proxy/europeana/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <dc:identifier>34afe0df15644a48bbb9a50278ba756c</dc:identifier>\n"
        //            + "      <edm:europeanaProxy>true</edm:europeanaProxy>\n"
        //            + "      <ore:proxyFor rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:proxyIn rdf:resource=\"/aggregation/europeana/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/provider/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <ore:lineage rdf:resource=\"/proxy/aggregator/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "   </ore:Proxy>\n"
        //            + "   <edm:EuropeanaAggregation rdf:about=\"/aggregation/europeana/08635/34afe0df15644a48bbb9a50278ba756c\">\n"
        //            + "      <edm:aggregatedCHO rdf:resource=\"/08635/34afe0df15644a48bbb9a50278ba756c\"/>\n"
        //            + "      <edm:dataProvider xml:lang=\"en\">Europeana Foundation</edm:dataProvider>\n"
        //            + "      <edm:provider xml:lang=\"en\">Europeana Foundation</edm:provider>\n"
        //            + "      <edm:datasetName>08635_EFG_LCVA</edm:datasetName>\n"
        //            + "      <edm:country>Lithuania</edm:country>\n"
        //            + "      <edm:language>mul</edm:language>\n"
        //            + "   </edm:EuropeanaAggregation>\n"
        //            + "</rdf:RDF>"),
        Arguments.of(
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:doap=\"http://usefulinc.com/ns/doap#\" xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:svcs=\"http://rdfs.org/sioc/services#\" xmlns:wgs84_pos=\"http://www.w3.org/2003/01/geo/wgs84_pos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "   <edm:ProvidedCHO rdf:about=\"/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "   <ore:Aggregation rdf:about=\"/aggregation/provider/08635/7b498e8404a14793a79c67a3faaf5502\">\n"
                + "      <edm:aggregatedCHO rdf:resource=\"/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <edm:dataProvider>Lietuvos Centrinis Valstybés Archyvas</edm:dataProvider>\n"
                + "      <edm:isShownAt rdf:resource=\"http://www.e-kinas.lt/objektas/kinas/1382/consecration-of-the-church-foundation-in-klaipeda\"/>\n"
                + "      <edm:object rdf:resource=\"http://www.e-kinas.lt/search-results-order/ioresource/534?type=image\"/>\n"
                + "      <edm:provider xml:lang=\"en\">EFG - The European Film Gateway</edm:provider>\n"
                + "      <dc:rights>Lietuvos Centrinis Valstybes Archyvas</dc:rights>\n"
                + "      <edm:rights rdf:resource=\"http://rightsstatements.org/vocab/InC/1.0/\"/>\n"
                + "   </ore:Aggregation>\n"
                + "   <ore:Proxy rdf:about=\"/proxy/provider/08635/7b498e8404a14793a79c67a3faaf5502\">\n"
                + "      <dc:contributor>Piotras Kalabuchovas</dc:contributor>\n"
                + "      <dc:description>1957-06-30. Būsimos bažnyčios statybos aikštė, išpuošta berželiais, pilna tikinčiųjų. Lauko altorių puošia didelis kryžius ir Dievo motinos Marijos skulptūra. Mišioms vadovauja Telšių vyskupas Petras Maželis.  Eina procesija. Vyskupas laimina tikinčiuosius. Jis laimina pasipuošusius vaikus, kurie priima pirmąją Komuniją. Vyskupas pašventina plytą, kuri įmūrijama į būsimos bažnyčios pamatą. Aikštės vaizdas iš viršaus. Mišios baigėsi. Tikintieji skirstosi pagrindine Klaipėdos miesto gatve. 2 dalis. Klebonas Liudvikas Povilionis skaito pamokslą iš sakyklos. Mišios. Gieda choristai .</dc:description>\n"
                + "      <dc:description xml:lang=\"en\">The Klaipėda church construction square decorated with birch trees. Outdoor altar decorated with a large cross and a statue of Mary, Mother of God. Mass led by Bishop Petras Maželis. The bishop blesses the faithful. He blesses children who receive First Communion. Bishop consecrates the first brick of the church foundation. Mass is over. Believers go the main street of the city of Klaipėda. Part 2. The pastor Liudvikas Povilionis reads a sermon from the pulpit. Sings the choir.</dc:description>\n"
                + "      <dc:identifier>7b498e8404a14793a79c67a3faaf5502</dc:identifier>\n"
                // + "      <dc:subject xml:lang=\"lt\">Religija</dc:subject>\n"
                // + "      <dc:subject xml:lang=\"en\">Religion</dc:subject>\n"
                + "      <dc:subject xml:lang=\"en\">Paranguaricutirimicuaro</dc:subject>\n"
                + "      <dc:title>Klaipėdos bažnyčios pamatų įšventinimas</dc:title>\n"
                + "      <dc:type rdf:resource=\"http://vocab.getty.edu/aat/300136900\"/>\n"
                + "      <dcterms:alternative xml:lang=\"en\">Consecration of the Church Foundation in Klaipėda</dcterms:alternative>\n"
                + "      <dcterms:created xml:lang=\"en\">1957</dcterms:created>\n"
                + "      <dcterms:provenance>Lietuvos Centrinis Valstybés Archyvas</dcterms:provenance>\n"
                + "      <dcterms:spatial rdf:resource=\"https://sws.geonames.org/597427/\"/>\n"
                + "      <edm:europeanaProxy>false</edm:europeanaProxy>\n"
                + "      <ore:proxyFor rdf:resource=\"/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <ore:proxyIn rdf:resource=\"/aggregation/provider/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <edm:type>VIDEO</edm:type>\n"
                + "   </ore:Proxy>\n"
                + "   <ore:Proxy rdf:about=\"/proxy/europeana/08635/7b498e8404a14793a79c67a3faaf5502\">\n"
                + "      <dc:identifier>7b498e8404a14793a79c67a3faaf5502</dc:identifier>\n"
                + "      <edm:europeanaProxy>true</edm:europeanaProxy>\n"
                + "      <ore:proxyFor rdf:resource=\"/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <ore:proxyIn rdf:resource=\"/aggregation/europeana/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <ore:lineage rdf:resource=\"/proxy/provider/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "   </ore:Proxy>\n"
                + "   <edm:EuropeanaAggregation rdf:about=\"/aggregation/europeana/08635/7b498e8404a14793a79c67a3faaf5502\">\n"
                + "      <edm:aggregatedCHO rdf:resource=\"/08635/7b498e8404a14793a79c67a3faaf5502\"/>\n"
                + "      <edm:dataProvider xml:lang=\"en\">Europeana Foundation</edm:dataProvider>\n"
                + "      <edm:provider xml:lang=\"en\">Europeana Foundation</edm:provider>\n"
                + "      <edm:datasetName>08635_EFG_LCVA</edm:datasetName>\n"
                + "      <edm:country>Lithuania</edm:country>\n"
                + "      <edm:language>mul</edm:language>\n"
                + "   </edm:EuropeanaAggregation>\n"
                + "</rdf:RDF>")
    );
  }

  @ParameterizedTest
  @MethodSource("providedMapException")
  void testEnrichmentWorkerHappyFlowTe(String inputRecord)
      throws DereferenceException, EnrichmentException, SerializationException {
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    EnricherProvider enricherProvider = new EnricherProvider();
    enricherProvider.setEnrichmentPropertiesValues("https://entity-management-production.eanadev.org/entity",
        "https://entity-api-v2-production.eanadev.org/entity",
        "api2demo");

    final Enricher enricher = enricherProvider.create();

    DereferencerProvider dereferencerProvider = new DereferencerProvider();
    dereferencerProvider.setEnrichmentPropertiesValues("https://entity-management-production.eanadev.org/entity",
        "https://entity-api-v2-production.eanadev.org/entity",
        "api2demo");
    dereferencerProvider.setDereferenceUrl("https://metis-dereference-rest-production.eanadev.org/");

    final Dereferencer dereferencer = dereferencerProvider.create();

    // Execute the worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(dereferencer, enricher);
    RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    final RDF inputRdf = rdfConversionUtils.convertStringToRdf(inputRecord);
    worker.cleanupPreviousEnrichmentEntities(inputRdf);
    ProcessedEncrichment<RDF> output = worker.process(inputRdf, modeSetWithBoth);

    LOGGER.info("REPORT: {}", output.getReport());
    LOGGER.info("RECORD: {}", rdfConversionUtils.convertRdfToString(output.getEnrichedRecord()));
  }

  @Test
  void testEnrichmentWorkerHappyFlow()
      throws DereferenceException, EnrichmentException {
    TreeSet<Mode> modeSetWithOnlyEnrichment = new TreeSet<>();
    TreeSet<Mode> modeSetWithOnlyDereference = new TreeSet<>();
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();

    modeSetWithOnlyEnrichment.add(Mode.ENRICHMENT);
    testEnrichmentWorkerHappyFlow(modeSetWithOnlyEnrichment);
    modeSetWithOnlyDereference.add(Mode.DEREFERENCE);
    testEnrichmentWorkerHappyFlow(modeSetWithOnlyDereference);
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    testEnrichmentWorkerHappyFlow(modeSetWithBoth);
  }

  @Test
  void testEnrichmentWorkerNullFlow()
      throws DereferenceException, EnrichmentException {
    TreeSet<Mode> modeSetWithOnlyEnrichment = new TreeSet<>();
    TreeSet<Mode> modeSetWithOnlyDereference = new TreeSet<>();
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();

    modeSetWithOnlyEnrichment.add(Mode.ENRICHMENT);
    testEnrichmentWorkerNullFlow(modeSetWithOnlyEnrichment);
    modeSetWithOnlyDereference.add(Mode.DEREFERENCE);
    testEnrichmentWorkerNullFlow(modeSetWithOnlyDereference);
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    testEnrichmentWorkerNullFlow(modeSetWithBoth);
  }

  private void testEnrichmentWorkerHappyFlow(Set<Mode> modes)
      throws DereferenceException, EnrichmentException {

    // Create enricher and mock it.
    final Enricher enricher = mock(EnricherImpl.class);

    final Dereferencer dereferencer = mock(DereferencerImpl.class);

    // Execute the worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(dereferencer, enricher);
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, modes);

    // Counters of method calls depend on the mode
    final boolean doDereferencing = modes.contains(Mode.DEREFERENCE);
    final boolean doEnrichment = modes.contains(Mode.ENRICHMENT);

    // Check the performed tasks
    verifyDereferencingHappyFlow(doDereferencing, dereferencer, inputRdf);
    verifyEnrichmentHappyFlow(doEnrichment, enricher, inputRdf);
    //    verifyMergeHappyFlow(doEnrichment, doDereferencing, entityMergeEngine);
  }

  private void testEnrichmentWorkerNullFlow(Set<Mode> modes)
      throws DereferenceException, EnrichmentException {

    // Create enrichment worker and mock the enrichment and dereferencing results.
    final Enricher enricher = mock(EnricherImpl.class);

    final Dereferencer dereferencer = mock(DereferencerImpl.class);

    // Execute the worker
    final EnrichmentWorkerImpl worker =
        spy(new EnrichmentWorkerImpl(dereferencer, enricher));
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, modes);

    // Counters of method calls depend on the mode
    final boolean doDereferencing = modes.contains(Mode.DEREFERENCE);
    final boolean doEnrichment = modes.contains(Mode.ENRICHMENT);

    // Check the performed tasks
    verifyDereferencingNullFlow(doDereferencing, dereferencer, inputRdf);
    verifyEnrichmentNullFlow(doEnrichment, enricher, inputRdf);

  }

  // Verify dereference related calls
  private void verifyDereferencingHappyFlow(boolean doDereferencing, Dereferencer dereferencer,
      RDF inputRdf) throws DereferenceException {
    if (doDereferencing) {
      verify(dereferencer, times(1)).dereference(inputRdf);

    } else {
      verify(dereferencer, never()).dereference(any());
    }
  }

  private void verifyDereferencingNullFlow(boolean doDereferencing, Dereferencer dereferencer,
      RDF inputRdf) throws DereferenceException {
    if (doDereferencing) {

      verify(dereferencer, times(1)).dereference(inputRdf);

    } else {
      verify(dereferencer, never()).dereference(any());
    }
  }

  // Verify enrichment related calls
  private void verifyEnrichmentHappyFlow(boolean doEnrichment, Enricher enricher,
      RDF inputRdf) throws EnrichmentException {
    if (doEnrichment) {
      verify(enricher, times(1)).enrichment(inputRdf);

    } else {
      verify(enricher, never()).enrichment(any());
    }
  }

  private void verifyEnrichmentNullFlow(boolean doEnrichment, Enricher worker, RDF inputRdf)
      throws EnrichmentException {
    if (doEnrichment) {
      verify(worker, times(1)).enrichment(inputRdf);

    } else {
      verify(worker, never()).enrichment(any());
    }
  }


  @Test
  void testProcessWrapperMethods()
      throws DereferenceException, EnrichmentException, SerializationException {

    // Create enrichment worker and mock the actual worker method as well as the RDF conversion
    // methods.
    final EnrichmentWorkerImpl worker = spy(new EnrichmentWorkerImpl(null, null));
    final RDF inputRdf = new RDF();
    final String outputString = "OutputString";
    doReturn(inputRdf).when(worker).convertStringToRdf(anyString());
    doReturn(outputString).when(worker).convertRdfToString(inputRdf);

    doReturn(new ProcessedEncrichment<>(inputRdf)).when(worker).process(any(RDF.class), any());

    // Perform the operations and verify the result
    final RDF returnedRdf = worker.process(inputRdf).getEnrichedRecord();
    assertEquals(inputRdf, returnedRdf);
    //add messages
    final String returnedString = worker.process("").getEnrichedRecord();
    assertEquals(outputString, returnedString);
    //add messages

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Validate the method calls to the actual worker method
    verify(worker, times(2)).process(any(RDF.class), any());
    verify(worker, times(2)).process(inputRdf, modeSetWithBoth);

    // Test null string input
    try {
      worker.process((String) null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  @Test
  void testEnrichmentWorkerNullValues() {

    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Test null string input
    try {
      worker.process((String) null, modeSetWithBoth);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    } catch (DereferenceException | SerializationException | EnrichmentException e) {
      e.printStackTrace();
    }

    // Test empty RDF input
    try {
      worker.process(new RDF(), null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException | EnrichmentException | DereferenceException e) {
      // This is expected
    }
  }

  @Test
  void testEnrichment() throws DereferenceException, SerializationException, EnrichmentException {
    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    assertThrows(SerializationException.class, () -> {
          ProcessEnriched<String> enrichedData = worker.process((String) "");
          //    assertEquals(enrichedData.getStatus());
        }
    );
  }
}
