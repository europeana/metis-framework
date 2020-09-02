package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XpathQuery;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * This normalizer cleans, or 'escapes', media link values, replacing all IRI Violation characters
 * with their respective encoding character.
 *
 * @author Joana Sousa
 */

public class CleanIRIViolationsNormalizer implements ValueNormalizeAction{

    private static final Namespace.Element RDF_RESOURCE = Namespace.RDF.getElement("resource");
    private static final Namespace.Element RDF_ABOUT = Namespace.RDF.getElement("about");
    private static final Namespace.Element ORE_AGGREGATION = Namespace.ORE.getElement("Aggregation");
    private static final Namespace.Element EDM_IS_SHOWN_BY = Namespace.EDM.getElement("isShownBy");
    private static final Namespace.Element EDM_IS_SHOWN_AT = Namespace.EDM.getElement("isShownAt");
    private static final Namespace.Element EDM_OBJECT = Namespace.EDM.getElement("object");
    private static final Namespace.Element EDM_HAS_VIEW = Namespace.EDM.getElement("hasView");
    private static final Namespace.Element EDM_WEB_RESOURCE = Namespace.EDM.getElement("WebResource");

    private static final XpathQuery RESOURCE_IS_SHOWN_BY_QUERY = new XpathQuery(
            "/%s/%s/%s/@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, EDM_IS_SHOWN_BY, RDF_RESOURCE);

    private static final XpathQuery RESOURCE_HAS_VIEW_QUERY = new XpathQuery(
            "/%s/%s/%s/@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, EDM_HAS_VIEW,  RDF_RESOURCE);

    private static final XpathQuery RESOURCE_IS_SHOWN_AT_QUERY = new XpathQuery(
            "/%s/%s/%s/@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, EDM_IS_SHOWN_AT,  RDF_RESOURCE);

    private static final XpathQuery RESOURCE_OBJECT_QUERY = new XpathQuery(
            "/%s/%s/%s/@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, EDM_OBJECT,  RDF_RESOURCE);

    private static final XpathQuery WEB_RESOURCE_ABOUT_QUERY = new XpathQuery(
            "/%s/%s/@%s", XpathQuery.RDF_TAG, EDM_WEB_RESOURCE, RDF_ABOUT);

    private final IRIFactory iriFactory;

    /**
     * Creates a new instance of this class.
     */
    public CleanIRIViolationsNormalizer() {
        iriFactory = IRIFactory.iriImplementation();
    }

    @Override
    public RecordNormalizeAction getAsRecordNormalizer() {
        return new ValueNormalizeActionWrapper(this, RESOURCE_IS_SHOWN_BY_QUERY, RESOURCE_HAS_VIEW_QUERY,
                RESOURCE_IS_SHOWN_AT_QUERY, RESOURCE_OBJECT_QUERY, WEB_RESOURCE_ABOUT_QUERY);
    }

    @Override
    public List<NormalizedValueWithConfidence> normalizeValue(String value) {
        if(value == null || value.isEmpty()){
            return Collections.emptyList();
        }

        IRI iri = iriFactory.create(value);
        String normalizedValue = "";

        try {
            normalizedValue = iri.toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            //TODO: Add LOG (debug)
            //TODO: Return empty list
        }

        return Collections.singletonList(new NormalizedValueWithConfidence(normalizedValue, 1));
    }


}
