package eu.europeana.enrichment.harvester.transform.edm.concept;

import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.enrichment.harvester.transform.XslTransformer;
import eu.europeana.enrichment.harvester.transform.util.NormalizeUtils;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concept Transformer class. It will transform any Controlled Vocabulary resource to an ConceptImpl by first applying the
 * XSLT specified and then invoke the ConceptTemplate class to generate the actual POJO. By definition, this class can be
 * reused for any transformation between a Controlled Vocabulary Resource that comes in RDF/XML to an ConceptImpl just by
 * modifying the XSLT to apply
 *
 * @author Cesare.Concordia@ europeana.eu
 */
public class ConceptTransformer implements XslTransformer<ConceptImpl> {

    private static final Logger log = Logger.getLogger(XslTransformer.class.getCanonicalName());

    @Override
    public ConceptImpl transform(String xsltPath, String resourceUri, Source doc) {
        StreamSource transformDoc = new StreamSource(new File(xsltPath));

        try {
            Transformer transformer = TransformerFactory
                    .newInstance().newTransformer(transformDoc);
            StreamResult out = new StreamResult(new StringWriter());
            transformer.setParameter("rdf_about", resourceUri);
            transformer.transform(doc, out);
            //System.out.println(out.getWriter().toString());
           // System.out.println("<!-- -->");
            return normalize(ConceptTemplate.getInstance().transform(out.getWriter().toString(), resourceUri));
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            log.log(Level.SEVERE, e.getMessage());

        }

        return null;
    }

    @Override
    public ConceptImpl normalize(ConceptImpl concept) {
    	

    	
       // concept.setAltLabel(NormalizeUtils.normalizeMap(concept.getAltLabel()));
        concept.setNote(NormalizeUtils.normalizeMap(concept.getNote()));
        concept.setExactMatch(NormalizeUtils.normalizeArray(concept.getExactMatch()));
        concept.setPrefLabel(NormalizeUtils.normalizeMap(concept.getPrefLabel()));
        concept.setBroader(NormalizeUtils.normalizeArray(concept.getBroader()));
        concept.setRelated(NormalizeUtils.normalizeArray(concept.getRelated()));
        return concept;
    }

}
