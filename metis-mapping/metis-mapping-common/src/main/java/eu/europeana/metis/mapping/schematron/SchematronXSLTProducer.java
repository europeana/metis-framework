package eu.europeana.metis.mapping.schematron;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author geomark
 *
 */
public class SchematronXSLTProducer {

	private static SchematronXSLTProducer INSTANCE;
	private static TransformerFactory tFactory;
	private static String iso_dsdl_include;
	private static String iso_abstract_expand;
	private static String iso_svrl_for_xslt2;
	private static String iso_schematron_skeleton_for_saxon;
	
	
	/**
	 * Default private constructor (instantiate via factory method)
	 */
	private SchematronXSLTProducer(){
		
	}
	
	
	/**
	 * @return
	 */
	public static SchematronXSLTProducer getInstance(){
		if(INSTANCE == null){
			tFactory = TransformerFactory.newInstance();
			
			iso_dsdl_include = SchematronXSLTProducer.class.getResource("iso_dsdl_include.xsl").getPath();
			iso_abstract_expand = SchematronXSLTProducer.class.getResource("iso_abstract_expand.xsl").getPath();
			iso_svrl_for_xslt2 = SchematronXSLTProducer.class.getResource("iso_svrl_for_xslt2.xsl").getPath();
			iso_schematron_skeleton_for_saxon = SchematronXSLTProducer.class.getResource("iso_schematron_skeleton_for_saxon.xsl").getPath();

			INSTANCE = new SchematronXSLTProducer();
		}

		return INSTANCE;
	}
	
	/**
	 * Generate the schematron XSL from a string that contains a schematron document
	 * @param schematron
	 * @return
	 */
	public String getXSL(String schematron) {
		String step1 = preformTransformation(schematron,iso_dsdl_include);
		String step2 = preformTransformation(step1,iso_abstract_expand);
		String finalstep = preformTransformation(step2,iso_svrl_for_xslt2);
		return finalstep;
	}
	
	/**
	 * Form a complete schematron document from schematron rules by adding root element and XmlSchema's namespace declarations 
	 * @param schematronRules
	 * @param namespaces
	 * @return
	 */
	public String wrapRules(String schematronRules, Map<String, String> namespaces) {
		StringBuffer sb = new StringBuffer();
		sb.append("<schema xmlns=\"http://purl.oclc.org/dsdl/schematron\">");
		
		if(namespaces != null) {
			for(String prefix: namespaces.keySet()) {
				String uri = namespaces.get(prefix);
				sb.append("<ns prefix=\"" + prefix + "\" uri=\"" + uri + "\"/>");
			}
		}
		
	    sb.append(schematronRules);
		sb.append("</schema>");
		
		return sb.toString();
	}
		
	public String wrapRules(String schematronRules){
		return this.wrapRules(schematronRules, null);
	}
	
	/**
	 * @param xml
	 * @param xslt
	 * @return
	 */
	private String preformTransformation(String xml, String xslt){
		try {
		    StringReader reader = new StringReader(xml);
		    StringWriter writer = new StringWriter();

		    Transformer transformer = tFactory.newTransformer(
		            new StreamSource(xslt));

		    transformer.transform(
		            new StreamSource(reader),
		            new StreamResult(writer));

		    String result = writer.toString();
		    return result;
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
	}

	/**
	 * Merge schematron rules with an existing schematron document
	 * 
	 * @param schematron string with contents of the original schematron document
	 * @param schematronRules the rules to merge
	 * @return the merged schematron document
	 * @throws IOException
	 * @throws ParsingException 
	 * @throws ValidityException 
	 */
	public String mergeSchematronRules(String schematron, String schematronRules) throws ValidityException, ParsingException, IOException {
		Builder builder = new Builder();
		Document document = builder.build(new ByteArrayInputStream(schematron.getBytes()));
		return this.mergeSchematron(document, schematronRules);
	}
	
	/**
	 * Merge schematron rules with an existing schematron document
	 * 
	 * @param schematron the original schematron document
	 * @param schematronRules the rules to merge
	 * @return the merged schematron document
	 * @throws IOException
	 * @throws ParsingException 
	 * @throws ValidityException 
	 */
	public String mergeSchematron(Document schematron, String schematronRules) throws ValidityException, ParsingException, IOException {
		Builder builder = new Builder();
		Document rules = builder.build(new ByteArrayInputStream(this.wrapRules(schematronRules).getBytes()));
		for(int i = 0; i < rules.getRootElement().getChildCount(); i++) {
			schematron.getRootElement().appendChild(rules.getRootElement().getChild(i).copy());			
		}
		return schematron.toXML();
	}
}