import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.xsd.XSDParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ymamakis on 9/9/16.
 */
public class MappingConstructor {

    public static void main(String[] args) throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setName("EDM-INTERNAL");
        schema.setVersion("undefined");
        Mapping mapping = new XSDParser("/home/ymamakis/git/metis-framework/metis-pandora/metis-pandora-utils/src/main/resources/schema/EDM-INTERNAL.xsd")
                .buildTemplate(schema, "crosswalk-EDM-INTERNAL", "RDF", generateNamespaces());

        System.out.println(mapping.getName());

    }

    private static Map<String, String> generateNamespaces() {
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("http://www.w3.org/ns/adms#", "adms");
        namespaces.put("http://creativecommons.org/ns#", "cc");
        namespaces.put("http://purl.org/dc/elements/1.1/", "dc");
        namespaces.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        namespaces.put("http://purl.org/dc/terms/", "dcterms");
        namespaces.put("http://www.w3.org/2003/01/geo/wgs84_pos#", "wgs84");
        namespaces.put("http://www.w3.org/2004/02/skos/core#", "skos");
        namespaces.put("http://www.w3.org/2002/07/owl#", "owl");
        namespaces.put("http://rdvocab.info/ElementsGr2/", "rdaGr2");
        namespaces.put("http://xmlns.com/foaf/0.1/", "foaf");
        namespaces.put("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#", "ebucore");
        namespaces.put("http://www.w3.org/2007/05/powder-s#", "wdrs");
        namespaces.put("http://rdfs.org/sioc/services#", "svcs");
        namespaces.put("http://purl.oclc.org/dsdl/schematron", "sch");
        namespaces.put("http://www.w3.org/ns/dcat#", "dcat");
        namespaces.put("http://www.europeana.eu/schemas/edm/", "edm");
        namespaces.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
        namespaces.put("http://www.w3.org/ns/odrl/2/", "odrl");
        namespaces.put("http://www.openarchives.org/ore/terms/", "ore");
        return namespaces;
    }
}
