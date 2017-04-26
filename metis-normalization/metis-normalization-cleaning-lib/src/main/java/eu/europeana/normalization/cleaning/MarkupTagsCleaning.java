/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.cleaning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Document;

import eu.europeana.normalization.NormalizeDetails;
import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.ValueNormalization;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;
import eu.europeana.normalization.util.Namespaces;
import net.htmlparser.jericho.Source;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class MarkupTagsCleaning implements ValueNormalization {
	
	public enum Mode {HTML_ONLY, ALL_MARKUP};
	
	
	public interface MarkupCleaner {
	    public  String clean(String input);
	}
	
	public class HtmlMarkupCleaner implements MarkupCleaner{
	
	    private Pattern pattern;
	
	    private final String [] tagsTab = {"!doctype","a","abbr","acronym","address","applet","area","article","aside","audio","b","base","basefont","bdi","bdo","bgsound","big","blink","blockquote","body","br","button","canvas","caption","center","cite","code","col","colgroup","content","data","datalist","dd","decorator","del","details","dfn","dir","div","dl","dt","element","em","embed","fieldset","figcaption","figure","font","footer","form","frame","frameset","h1","h2","h3","h4","h5","h6","head","header","hgroup","hr","html","i","iframe","img","input","ins","isindex","kbd","keygen","label","legend","li","link","listing","main","map","mark","marquee","menu","menuitem","meta","meter","nav","nobr","noframes","noscript","object","ol","optgroup","option","output","p","param","plaintext","pre","progress","q","rp","rt","ruby","s","samp","script","section","select","shadow","small","source","spacer","span","strike","strong","style","sub","summary","sup","table","tbody","td","template","textarea","tfoot","th","thead","time","title","tr","track","tt","u","ul","var","video","wbr","xmp"};
	    
	    
	    public HtmlMarkupCleaner() {
	        StringBuffer tags = new StringBuffer();
	        for (int i=0;i<tagsTab.length;i++) {
	            tags.append(tagsTab[i].toLowerCase());
	            if (i<tagsTab.length-1) {
	                tags.append('|');
	            }
	        }
	        pattern = Pattern.compile("</?("+tags.toString()+")"+
	        "((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>"
	        		, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
	        
	        
	    }
	
	    public  String clean(String input) {
	        return pattern.matcher(input).replaceAll("");
	    }
	
	
	}
	
	public class AllMarkupCleaner implements MarkupCleaner{
		@Override
		public String clean(String input) {
			Source source = new Source(input);
	    	return source.getTextExtractor().toString();
		}
		
	}
	
	private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(MarkupTagsCleaning.class.getName());

    protected final Mode mode;
    MarkupCleaner cleaner;
    /**
     * Creates a new instance of this class.
     * 
     */
    public MarkupTagsCleaning() {
        this(Mode.ALL_MARKUP);
    }
    /**
     * Creates a new instance of this class.
     * 
     */
    public MarkupTagsCleaning(Mode mode) {
    	super();
    	this.mode=mode;
    	if(mode==Mode.HTML_ONLY)
    		cleaner=new HtmlMarkupCleaner();
    	else
    		cleaner=new AllMarkupCleaner();
    }

    public List<String> normalize(String htmlText) {
    	String ret=cleaner.clean(htmlText);
    	if(ret.length()==0)
    		return Collections.EMPTY_LIST;
    	return new ArrayList<String>(1) {
			private static final long serialVersionUID = 1L;
		{add(ret);}};
    }

    public List<NormalizeDetails> normalizeDetailed(String htmlText) {
    	String ret=cleaner.clean(htmlText);
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
