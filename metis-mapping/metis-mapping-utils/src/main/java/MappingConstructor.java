import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.mapping.model.Mappings;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.model.XPathHolder;
import eu.europeana.metis.mapping.utils.XSLTGenerator;
import eu.europeana.metis.xsd.XSDParser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * Created by ymamakis on 9/9/16.
 */
public class MappingConstructor {

    public static void main(String[] args) throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setName("EDM-INTERNAL");
        schema.setVersion("undefined");
        XPathHolder holder = new XPathHolder();
        holder.setXpath("/repox:exportedRecords/repox:record/repox:metadata/record");
        holder.setName("repoxWrap");
        schema.setRootPath(holder);
        Mapping mapping = new XSDParser("/home/ymamakis/git/metis-framework/metis-pandora/metis-pandora-utils/src/main/resources/schema/EDM-INTERNAL.xsd")
                .buildTemplate(schema, "crosswalk-EDM-INTERNAL", "RDF", generateNamespaces());

        Mappings mappings= mapping.getMappings();

        String root = mappings.getRootElement();
        if(mappings.getAttributes()!=null){
            List<Attribute> attributesCopy = new ArrayList<>();
            List<Attribute> attributes = mappings.getAttributes();
            for(Attribute attribute:attributes){
                attributesCopy.add(processAttribute(attribute));
            }
            mappings.setAttributes(attributes);
        }
        if(mappings.getElements()!=null){
            List<Element> elementsCopy = new ArrayList<>();
            List<Element> elements = mappings.getElements();
            for(Element element:elements){
                Element processed = processElement(element,root);
                if(processed!=null) {
                    elementsCopy.add(processed);
                    if(StringUtils.equals(processed.getName(),"Proxy")){
                        Element europeanaProxy = new Element();
                        BeanUtils.copyProperties(processed,europeanaProxy);
                        elementsCopy.add(fixEuropeanaProxy(europeanaProxy));
                    }
                } else {
                    elementsCopy.add(element);
                }

            }
            mappings.setElements(elementsCopy);
        }



        mappings.setHasMappings(true);

        mapping.setMappings(mappings);

        String mappingStr = new ObjectMapper().writeValueAsString(mapping);

        IOUtils.write(mappingStr,new FileOutputStream("/home/ymamakis/Desktop/test_mapping.json"));

        XSLTGenerator gen = new XSLTGenerator();
        IOUtils.write(gen.generateFromMappings(mapping),new FileOutputStream("/home/ymamakis/Desktop/test_mapping.xsl"));

    }

    private static Element fixEuropeanaProxy(Element europeanaProxy) {
        List<Element> elements  = europeanaProxy.getElements();
        List<Element> elementsCopy = new ArrayList<>();
        for(Element elem: elements){
            if(StringUtils.equals(elem.getName(),"europeanaProxy")){
                SimpleMapping mapping = new SimpleMapping();
                mapping.setConstant("true");
                List<SimpleMapping> mappings = new ArrayList<>();
                mappings.add(mapping);
                elem.setMappings(mappings);

            }
            elementsCopy.add(elem);
        }
        europeanaProxy.setElements(elementsCopy);
        return europeanaProxy;
    }

    private static Element processElement(Element element, String root) {
        //System.out.println(element.getxPathFromRoot());
        if(element.getAttributes()!=null){
            List<Attribute> attributesCopy = new ArrayList<>();
            List<Attribute> attributes = element.getAttributes();
            for(Attribute attribute:attributes){
                Attribute attr = processAttribute(attribute);
                if(attr!=null) {
                    attributesCopy.add(attr);
                } else {
                    attributesCopy.add(attribute);
                }
            }
            element.setAttributes(attributes);
            element.setHasMapping(true);
        }
        if(element.getElements()!=null){
            List<Element> elementsCopy = new ArrayList<>();
            List<Element> elements = element.getElements();
            for(Element elem:elements){
                Element processed = processElement(elem,root);
                if(processed!=null) {
                    elementsCopy.add(processed);
                } else {
                    elementsCopy.add(elem);
                }
            }
            element.setElements(elementsCopy);
            element.setHasMapping(true);
        }

        ElementEnumeration elem = ElementEnumeration.find(StringUtils.substringAfter(element.getxPathFromRoot(),"rdf:RDF/"));
        if(elem!=null) {
            return ElementContructor.constructElement(element, elem.getSourceFields(), elem.getRules());
        }
        return null;
    }

    private static Attribute processAttribute(Attribute attribute) {

        ElementEnumeration elem = ElementEnumeration.find(StringUtils.substringAfter(attribute.getxPathFromRoot(),"rdf:RDF/"));
        if(elem!=null) {
            return ElementContructor.constructElement(attribute, elem.getSourceFields(), elem.getRules());
        }
        return null;
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
        namespaces.put("http://repox.ist.utl.pt","repox");
        return namespaces;
    }
}
