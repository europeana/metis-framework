/**
 * This file is <b>REQUIRED</b> for JAXB qualifying elements.
 * <p>It provides a mapping of a namespace uri and it's prefix value. Classes that contain xml
 * elements and define a namespace will be mapped to the prefix that is declated here.</p>
 *
 * <p>If the this file is not present the prefixes will be automatically provided(of the format
 * nsX where X is an incrementing number) and an example of an xml file would be generated as such:
 * <pre>
 *    {@code
 *      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *      <ns11:results xmlns:ns2="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:ns3="http://www.w3.org/2004/02/skos/core#"
 *      xmlns:ns4="http://xmlns.com/foaf/0.1/" xmlns:ns5="http://www.europeana.eu/schemas/edm/"
 *      xmlns:ns6="http://purl.org/dc/elements/1.1/" xmlns:ns7="http://rdvocab.info/ElementsGr2/"
 *      xmlns:ns8="http://www.w3.org/2002/07/owl#" xmlns:ns9="http://purl.org/dc/terms/"
 *      xmlns:ns10="http://www.w3.org/2003/01/geo/wgs84_pos#"
 *      xmlns:ns11="http://www.europeana.eu/schemas/metis">
 *        <ns11:result>
 *          <ns3:Concept ns2:about="http://example.com/concept">
 *            <ns3:prefLabel xml:lang="en">examplePrefLabel</ns3:prefLabel>
 *            <ns3:note xml:lang="en">exampleNote</ns3:note>
 *            <ns3:exactMatch ns2:resource="http://example.com/exactMatch"/>
 *            <ns3:related ns2:resource="http://example.com/related"/>
 *          </ns3:Concept>
 *        </ns11:result>
 *      </ns11:results>}
 *  </pre>
 * </p>
 *
 * <p>
 * <pre>
 *    With this file present the xml example would look like this:
 *    {@code
 *    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 *    <metis:results xmlns:dcterms="http://purl.org/dc/terms/" xmlns:foaf="http://xmlns.com/foaf/0.1/"
 *    xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:edm="http://www.europeana.eu/schemas/edm/"
 *    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema" xmlns:cc="http://creativecommons.org/ns"
 *    xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
 *    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
 *    xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
 *    xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:metis="http://www.europeana.eu/schemas/metis">
 *      <metis:result>
 *        <skos:Concept rdf:about="http://example.com/concept">
 *          <skos:prefLabel xml:lang="en">examplePrefLabel</skos:prefLabel>
 *          <skos:note xml:lang="en">exampleNote</skos:note>
 *          <skos:exactMatch rdf:resource="http://example.com/exactMatch"/>
 *          <skos:related rdf:resource="http://example.com/related"/>
 *        </skos:Concept>
 *      </metis:result>
 *    </metis:results>}
 *  </pre>
 * </p>
 */
//@formatter:off
@XmlSchema(xmlns = {
    @XmlNs(prefix = "metis", namespaceURI = "http://www.europeana.eu/schemas/metis"),
    @XmlNs(prefix = "edm", namespaceURI = "http://www.europeana.eu/schemas/edm/"),
    @XmlNs(prefix = "skos", namespaceURI = "http://www.w3.org/2004/02/skos/core#"),
    @XmlNs(prefix = "dcterms", namespaceURI = "http://purl.org/dc/terms/"),
    @XmlNs(prefix = "rdf", namespaceURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    @XmlNs(prefix = "rdfs", namespaceURI = "http://www.w3.org/2000/01/rdf-schema"),
    @XmlNs(prefix = "cc", namespaceURI = "http://creativecommons.org/ns"),
    @XmlNs(prefix = "foaf", namespaceURI = "http://xmlns.com/foaf/0.1/"),
    @XmlNs(prefix = "wgs84_pos", namespaceURI = "http://www.w3.org/2003/01/geo/wgs84_pos#"),
    @XmlNs(prefix = "owl", namespaceURI = "http://www.w3.org/2002/07/owl#"),
    @XmlNs(prefix = "xml", namespaceURI = "http://www.w3.org/XML/1998/namespace"),
    @XmlNs(prefix = "dc", namespaceURI = "http://purl.org/dc/elements/1.1/"),
    @XmlNs(prefix = "rdaGr2", namespaceURI = "http://rdvocab.info/ElementsGr2/")},
    namespace = "http://www.europeana.eu/schemas/metis", elementFormDefault = XmlNsForm.QUALIFIED)
//@formatter:on
package eu.europeana.enrichment.api.external.model;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
