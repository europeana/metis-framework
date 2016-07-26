package eu.europeana.enrichment.harvester.transform.edm.agent;

import eu.europeana.corelib.solr.entity.AgentImpl;
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
 * Agent Transformer class. It will transform any Controlled Vocabulary resource to an AgentImpl by first applying the
 * XSLT specified and then invoke the AgentTemplate class to generate the actual POJO. By definition, this class can be
 * reused for any transformation between a Controlled Vocabulary Resource that comes in RDF/XML to an AgentImpl just by
 * modifying the XSLT to apply
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AgentTransformer implements XslTransformer<AgentImpl> {

    private static final Logger log = Logger.getLogger(XslTransformer.class.getCanonicalName());

    @Override
    public AgentImpl transform(String xsltPath, String resourceUri, Source doc) {
        StreamSource transformDoc = new StreamSource(new File(xsltPath));

        try {
            Transformer transformer = TransformerFactory
                    .newInstance().newTransformer(transformDoc);
            StreamResult out = new StreamResult(new StringWriter());
            transformer.transform(doc, out);
            //System.out.println(out.getWriter().toString());
            return normalize(AgentTemplate.getInstance().transform(out.getWriter().toString(), resourceUri));
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            log.log(Level.SEVERE, e.getMessage());

        }

        return null;
    }

    @Override
    public AgentImpl normalize(AgentImpl agent) {

        agent.setAltLabel(NormalizeUtils.normalizeMap(agent.getAltLabel()));
        agent.setBegin(NormalizeUtils.normalizeMap(agent.getBegin()));
        agent.setDcDate(NormalizeUtils.normalizeMap(agent.getDcDate()));
        agent.setDcIdentifier(NormalizeUtils.normalizeMap(agent.getDcIdentifier()));
        agent.setEdmHasMet(NormalizeUtils.normalizeMap(agent.getEdmHasMet()));
        agent.setEdmIsRelatedTo(NormalizeUtils.normalizeMap(agent.getEdmIsRelatedTo()));
        agent.setEnd(NormalizeUtils.normalizeMap(agent.getEnd()));
        agent.setFoafName(NormalizeUtils.normalizeMap(agent.getFoafName()));
        agent.setNote(NormalizeUtils.normalizeMap(agent.getNote()));
        agent.setOwlSameAs(NormalizeUtils.normalizeArray(agent.getOwlSameAs()));
        agent.setPrefLabel(NormalizeUtils.normalizeMap(agent.getPrefLabel()));
        agent.setRdaGr2BiographicalInformation(NormalizeUtils.normalizeMap(agent.getRdaGr2BiographicalInformation()));
        agent.setRdaGr2DateOfBirth(NormalizeUtils.normalizeMap(agent.getRdaGr2DateOfBirth()));
        agent.setRdaGr2DateOfDeath(NormalizeUtils.normalizeMap(agent.getRdaGr2DateOfDeath()));
        agent.setRdaGr2PlaceOfBirth(NormalizeUtils.normalizeMap(agent.getRdaGr2PlaceOfBirth()));
        agent.setRdaGr2PlaceOfDeath(NormalizeUtils.normalizeMap(agent.getRdaGr2PlaceOfDeath()));
        agent.setRdaGr2ProfessionOrOccupation(NormalizeUtils.normalizeMap(agent.getRdaGr2ProfessionOrOccupation()));
        return agent;
    }

}
