/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.cleaning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import eu.europeana.normalization.NormalizeDetails;
import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.ValueNormalization;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;
import eu.europeana.normalization.util.Namespaces;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class TrimAndEmptyValueCleaning implements ValueNormalization {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(TrimAndEmptyValueCleaning.class.getName());

    /**
     * Creates a new instance of this class.
     * 
     */
    public TrimAndEmptyValueCleaning() {
        super();
    }

    public List<String> normalize(String value) {
    	String ret=value.trim();
    	if(ret.length()==0)
    		return Collections.EMPTY_LIST;
    	return new ArrayList<String>(1) {
			private static final long serialVersionUID = 1L;
		{add(ret);}};
    }

    public List<NormalizeDetails> normalizeDetailed(String value) {
    	String ret=value.trim();
    	if(ret.length()==0)
    		return Collections.emptyList();
    	return new ArrayList<NormalizeDetails>(1) {
			private static final long serialVersionUID = 1L;
		{add(new NormalizeDetails(ret,1));}};
    }

	public RecordNormalization toEdmRecordNormalizer() {
		XpathQuery cleanablePropertiesQuery=new XpathQuery(
				new HashMap<String, String>() {
					private static final long serialVersionUID = 1L;
				{
					put("rdf",Namespaces.RDF);
					put("dc", Namespaces.DC);
					put("ore", Namespaces.ORE);
					put("edm", Namespaces.EDM);
					put("skos", Namespaces.SKOS);
				}}, 
				"/rdf:RDF/ore:Proxy/*"
//				"/rdf:RDF/ore:Proxy[edm:europeanaProxy='true']/*"
				+ "| /rdf:RDF/ore:Aggregation/*"
				+ "| /rdf:RDF/edm:WebResource/*"
				+ "| /rdf:RDF/edm:Agent/*"
				+ "| /rdf:RDF/edm:Place/*"
				+ "| /rdf:RDF/edm:Event/*"
				+ "| /rdf:RDF/edm:TimeSpan/*"
				+ "| /rdf:RDF/edm:PhysicalThing/*"
				+ "| /rdf:RDF/skos:Concept/*"
				);
//	}}, "//ore:Proxy[edm:europeanaProxy='true')/dc:language");
		
//		ore:Proxy_europeana/edm:europeanaProxy
		
//		For all properties of ore:proxy where edm:europeanaProxy=true, ore:Aggregation, all edm:WebResource, all contextual classes
		
		
		
    	ValueToRecordNormalizationWrapper dcLanguageNorm=new ValueToRecordNormalizationWrapper(this, false, cleanablePropertiesQuery);
    	return dcLanguageNorm;
	}

}
