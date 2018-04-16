package eu.europeana.normalization;

import java.util.ArrayList;
import java.util.List;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.normalizers.ChainedNormalizer;
import eu.europeana.normalization.normalizers.RecordNormalizeAction;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.NormalizationException;

/**
 * This class creates instances of {@link Normalizer}. The default steps for a normalization are:
 * <ol>
 * <li>{@link NormalizerStep#CLEAN_SPACE_CHARACTERS}</li>
 * <li>{@link NormalizerStep#CLEAN_MARKUP_TAGS}</li>
 * <li>{@link NormalizerStep#NORMALIZE_LANGUAGE_REFERENCES}</li>
 * <li>{@link NormalizerStep#REMOVE_DUPLICATE_STATEMENTS}</li>
 * </ol>
 * But an alternative order may be provided, and steps may be omitted or performed more than once.
 */
public class NormalizerFactory {

  private final NormalizerSettings settings;

  private static final NormalizerStep[] DEFAULT_NORMALIZER_STEPS =
      {NormalizerStep.CLEAN_SPACE_CHARACTERS, NormalizerStep.CLEAN_MARKUP_TAGS,
          NormalizerStep.NORMALIZE_LANGUAGE_REFERENCES, NormalizerStep.REMOVE_DUPLICATE_STATEMENTS};

  /**
   * Constructor for default settings.
   */
  public NormalizerFactory() {
    this(new NormalizerSettings());
  }

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to this normalization.
   */
  public NormalizerFactory(NormalizerSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates a normalizer with the default steps and order.
   * 
   * @return A normalizer.
   * @throws NormalizationConfigurationException In case the normalizer could not be set up.
   */
  public Normalizer getNormalizer() throws NormalizationConfigurationException {
    return getNormalizer(DEFAULT_NORMALIZER_STEPS);
  }

  /**
   * This method creates a normalizer.
   * 
   * @param normalizerSteps The steps to be performed and the order in which to perform them.
   * @return A normalizer.
   * @throws NormalizationConfigurationException In case the normalizer could not be set up.
   */
  public Normalizer getNormalizer(NormalizerStep... normalizerSteps)
      throws NormalizationConfigurationException {

    // Sanity checks
    if (normalizerSteps == null || normalizerSteps.length == 0) {
      throw new NormalizationConfigurationException("The list of steps cannot be empty.", null);
    }

    // Create actions for the steps.
    final List<RecordNormalizeAction> actions = new ArrayList<>(normalizerSteps.length);
    for (NormalizerStep step : normalizerSteps) {
      actions.add(step.createAction(settings).getAsRecordNormalizer());
    }

    // Create chain normalizer and use it to set up the normalizer.
    return new NormalizerImpl(new ChainedNormalizer(actions.toArray(new RecordNormalizeAction[0])));
  }
  
  public static void main(String[] args) throws NormalizationException, NormalizationConfigurationException {
    final String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<rdf:RDF xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:odrl=\"http://www.w3.org/ns/odrl/2/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:adms=\"http://www.w3.org/ns/adms#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">\n" + 
        "<edm:ProvidedCHO rdf:about=\"http://data.dm2e.eu/data/item/onb/codices/%2BZ103983303\"/>\n" + 
        "<edm:WebResource rdf:about=\"http://archiv.onb.ac.at:1801/webclient/DeliveryManager?pid=2937559&amp;custom_att_2=simple_viewer\">\n" + 
        "<dc:format>text/html</dc:format>\n" + 
        "<edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/>\n" + 
        "</edm:WebResource>\n" + 
        "<edm:WebResource rdf:about=\"http://digital.onb.ac.at/content/v1/codices/jpg/Z103983302/thumbnail\">\n" + 
        "<dc:format>image/jpeg</dc:format>\n" + 
        "<edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/>\n" + 
        "</edm:WebResource>\n" + 
        "<edm:Agent rdf:about=\"http://data.dm2e.eu/data/agent/onb/codices/%2BZ103983302/Verschiedene\">\n" + 
        "<skos:prefLabel>Verschiedene</skos:prefLabel>\n" + 
        "</edm:Agent>\n" + 
        "<edm:TimeSpan rdf:about=\"http://data.dm2e.eu/data/timespan/onb/codices/0975-01-01T000000UG_1299-12-31T235959UG\">\n" + 
        "<skos:prefLabel>0975-1299</skos:prefLabel>\n" + 
        "<edm:begin>0975-01-01</edm:begin>\n" + 
        "<edm:end>1299-12-31</edm:end>\n" + 
        "</edm:TimeSpan>\n" + 
        "<skos:Concept rdf:about=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript\">\n" + 
        "<skos:prefLabel xml:lang=\"en\">Manuscript</skos:prefLabel>\n" + 
        "<skos:note>ProvidedCHO of type manuscript, e.g. Wittgensteins brown book. Not equivalent to bibo:Manuscript.</skos:note>\n" + 
        "</skos:Concept>\n" + 
        "<ore:Aggregation rdf:about=\"http://data.dm2e.eu/data/aggregation/onb/codices/%2BZ103983302\">\n" + 
        "<edm:aggregatedCHO rdf:resource=\"http://data.dm2e.eu/data/item/onb/codices/%2BZ103983302\"/>\n" + 
        "<edm:dataProvider>Österreichische Nationalbibliothek - Austrian National Library</edm:dataProvider>\n" + 
        "<edm:isShownAt rdf:resource=\"http://archiv.onb.ac.at:1801/webclient/DeliveryManager?pid=2937559&amp;custom_att_2=simple_viewer\"/>\n" + 
        "<edm:object rdf:resource=\"http://digital.onb.ac.at/content/v1/codices/jpg/Z103983302/thumbnail\"/>\n" + 
        "<edm:provider>DM2E</edm:provider>\n" + 
        "<edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/>\n" + 
        "</ore:Aggregation>\n" + 
        "<ore:Proxy rdf:about=\"http://data.dm2e.eu/data/item/onb/codices/%2BZ103983302\">\n" + 
        "<dc:creator rdf:resource=\"http://data.dm2e.eu/data/agent/onb/codices/%2BZ103983302/Verschiedene\"/>\n" + 
        "<dc:description>Cod. 510 HAN </dc:description>\n" + 
        "<dc:description>Pergament</dc:description>\n" + 
        "<dc:description>Rotes Schafleder mit Streicheisenlinien und Blindstempeln über Holzdeckeln. Österreich, Göttweig, 15. Jhdt.</dc:description>\n" + 
        "<dc:identifier>+Z103983302</dc:identifier>\n" + 
        "<dc:language>la</dc:language>\n" + 
        "<dc:source rdf:resource=\"http://data.dm2e.eu/data/item/onb/codices/%2BZ103983302\"/>\n" + 
        "<dc:title>Historische Sammelhandschrift</dc:title>\n" + 
        "<dc:title xml:lang=\"de\">Historische Sammelhandschrift</dc:title>\n" + 
        "<dc:title xml:lang=\"de\">Historische Sammelhandschrift</dc:title>\n" + 
        "<dc:title xml:lang=\"de\">Sammelhandschrift</dc:title>\n" + 
        "<dc:title xml:lang=\"en\">Historic handwritten document</dc:title>\n" + 
        "<dc:type rdf:resource=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript\"/>\n" + 
        "<dc:type rdf:resource=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript\"/>\n" + 
        "<dc:type rdf:resource=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript1\"></dc:type>\n" + 
        "<dc:type rdf:resource=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript1\">TEXT</dc:type>\n" + 
        "<dc:type rdf:resource=\"http://onto.dm2e.eu/schemas/dm2e/Manuscript1\">Text</dc:type>\n" + 
        "<dcterms:extent>245 x 185 mm</dcterms:extent>\n" + 
        "<dcterms:extent>Handschrift; 141 Bll.</dcterms:extent>\n" + 
        "<dcterms:issued rdf:resource=\"http://data.dm2e.eu/data/timespan/onb/codices/0975-01-01T000000UG_1299-12-31T235959UG\"/>\n" + 
        "<dcterms:issued>13. Jhdt.; letztes Viertel 10. Jhdt.; 12. Jhdt.</dcterms:issued>\n" + 
        "<edm:hasMet rdf:resource=\"http://d-nb.info/gnd/118726870\"/>\n" + 
        "<edm:hasMet rdf:resource=\"http://d-nb.info/gnd/2103432-1\"/>\n" + 
        "<edm:hasType>Manuscript</edm:hasType>\n" + 
        "<edm:type>TEXT</edm:type>\n" + 
        "</ore:Proxy>\n" + 
        "<edm:EuropeanaAggregation rdf:about=\"http://data.dm2e.eu/data/aggregation/onb/codices/%2BZ103983302\">\n" + 
        "<edm:aggregatedCHO rdf:resource=\"http://data.dm2e.eu/data/item/onb/codices/%2BZ103983302\"/>\n" + 
        "<edm:country>Austria</edm:country>\n" + 
        "<edm:language>de</edm:language>\n" + 
        "</edm:EuropeanaAggregation>\n" + 
        "</rdf:RDF>\n";
    
    final NormalizationResult result = new NormalizerFactory().getNormalizer().normalize(testXml);
    System.out.println(result.getNormalizedRecordInEdmXml());
    
  }
}
