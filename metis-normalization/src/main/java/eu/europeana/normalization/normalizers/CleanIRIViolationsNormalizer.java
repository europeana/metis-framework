package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XpathQuery;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class CleanIRIViolationsNormalizer implements ValueNormalizeAction{

    private static final Namespace.Element RDF_RESOURCE = Namespace.RDF.getElement("resource");
    private static final Namespace.Element EDM_IS_SHOWN_BY = Namespace.EDM.getElement("isShownBy");
    private static final Namespace.Element ORE_AGGREGATION = Namespace.ORE.getElement("Aggregation");

    private static final XpathQuery RESOURCE_DATA_QUERY = new XpathQuery(
            "/%s/%s/%s//@%s", XpathQuery.RDF_TAG, ORE_AGGREGATION, EDM_IS_SHOWN_BY, RDF_RESOURCE);

    private final IRIFactory iriFactory;

    /**
     * Creates a new instance of this class.
     */
    public CleanIRIViolationsNormalizer() {
        iriFactory = IRIFactory.iriImplementation();
    }

    @Override
    public RecordNormalizeAction getAsRecordNormalizer() {
        return new ValueNormalizeActionWrapper(this, RESOURCE_DATA_QUERY);
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
        }

        return Collections.singletonList(new NormalizedValueWithConfidence(normalizedValue, 1));
    }


}
