package eu.europeana.normalization.common.cleaning;

import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;
import eu.europeana.normalization.util.Namespaces;
import java.util.HashMap;

public abstract class EdmRecordNormalizerBase implements ValueNormalization {

  public EdmRecordNormalizerBase() {
    super();
  }

  @Override
  public RecordNormalization toEdmRecordNormalizer() {
    XpathQuery cleanablePropertiesQuery = new XpathQuery(
        new HashMap<String, String>() {
          private static final long serialVersionUID = 1L;

          {
            put("rdf", Namespaces.RDF);
            put("dc", Namespaces.DC);
            put("ore", Namespaces.ORE);
            put("edm", Namespaces.EDM);
            put("skos", Namespaces.SKOS);
          }
        },
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

    return new ValueToRecordNormalizationWrapper(this,
        false, cleanablePropertiesQuery);
  }


}
