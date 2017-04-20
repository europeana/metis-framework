package eu.europeana.metis.mapping.client;

import eu.europeana.metis.mapping.model.MappingSchema;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ymamakis on 8/2/16.
 */
public class PandoraClientTest {

    public static Map<String,String> generateNamespaces() {
        Map<String,String> namespaces = new HashMap<>();
        namespaces.put("http://www.w3.org/ns/adms#","adms");
        namespaces.put("http://creativecommons.org/ns#","cc");
        namespaces.put("http://purl.org/dc/elements/1.1/","dc");
        namespaces.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
        namespaces.put("http://purl.org/dc/terms/","dcterms");
        namespaces.put("http://www.w3.org/2003/01/geo/wgs84_pos#","wgs84");
        namespaces.put("http://www.w3.org/2004/02/skos/core#","skos");
        namespaces.put("http://www.w3.org/2002/07/owl#","owl");
        namespaces.put("http://rdvocab.info/ElementsGr2/","rdaGr2");
        namespaces.put("http://xmlns.com/foaf/0.1/","foaf");
        namespaces.put("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#","ebucore");
        namespaces.put("http://www.w3.org/2007/05/powder-s#","wdrs");
        namespaces.put("http://rdfs.org/sioc/services#","svcs");
        namespaces.put("http://purl.oclc.org/dsdl/schematron","sch");
        namespaces.put("http://www.w3.org/ns/dcat#","dcat");
        namespaces.put("http://www.europeana.eu/schemas/edm/","edm");
        namespaces.put("http://www.w3.org/2000/01/rdf-schema#","rdfs");
        namespaces.put("http://www.w3.org/ns/odrl/2/","odrl");
        namespaces.put("http://www.openarchives.org/ore/terms/","ore");
        return namespaces;
    }

    public static void main(String[] args){
        PandoraClient client = new PandoraClient();
        try {
            System.out.println(client.uploadTemplateFromFile("EDM-INTERNAL.xsd","EDM-INTERNAL","rdf:RDF",generateNamespaces(),
                    new File("/home/ymamakis/git/metis-framework/metis-pandora/metis-pandora-client/src/main/resources/schema.tar.gz"),new MappingSchema()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
