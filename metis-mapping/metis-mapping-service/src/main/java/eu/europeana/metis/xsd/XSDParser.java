package eu.europeana.metis.xsd;


import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.mapping.model.Mappings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An XSD Parser to Mapping template
 */
public class XSDParser {
    public static final Logger log = LoggerFactory.getLogger(XSDParser.class);

    private XSOMParser parser = new XSOMParser();
    private XSSchemaSet schemaSet = null;
    private Map<String, String> namespaces = new HashMap<>();

    private Map<String, String> documentation = null;
    private Set<String> schematronRules = null;

    public XSDParser(String xsd) {
        this.initXSSchema(xsd);
    }

    public XSDParser(InputStream is) {
        this.initXSSchema(is);
    }

    public XSDParser(Reader reader) {
        this.initXSSchema(reader);
    }

    public Map<String, String> getNamespaces() {
        return this.namespaces;
    }

    public void setNamespaces(HashMap<String, String> map) {
        this.namespaces = map;
    }

    private void initParser() {
        this.parser.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String arg0, String arg1)
                    throws SAXException, IOException {
                log.debug("Resolving: " + arg0 + " => " + arg1);
                return null;
            }

        });

        this.parser.setAnnotationParser(new DomAnnotationParserFactory());


        if (this.parser == null) {
            log.error("schema parser is null!");
        } else {
            ErrorHandler errorHandler = new ErrorHandler() {

                public void error(SAXParseException arg0) throws SAXException {
                    log.error("error: " + arg0.getMessage());

                }

                public void fatalError(SAXParseException arg0)
                        throws SAXException {
                    log.error("fatal: " + arg0.getMessage());
                }

                public void warning(SAXParseException arg0) throws SAXException {
                    log.error("warning: " + arg0.getMessage());
                }
            };
            this.parser.setErrorHandler(errorHandler);
        }
    }

    private void initXSSchema(String schemaFileName) {
        try {
            File file = new File(schemaFileName);
            this.initParser();
            this.parser.parse(file);

            this.schemaSet = this.parser.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initXSSchema(InputStream is) {
        try {
            this.initParser();
            this.parser.parse(is);
            this.schemaSet = this.parser.getResult();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initXSSchema(Reader reader) {
        try {
            this.initParser();
            this.parser.parse(reader);
            this.schemaSet = this.parser.getResult();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getPrefixForNamespace(String namespace) {
        String prefix;
        if (this.namespaces.containsKey(namespace)) {
            prefix = this.namespaces.get(namespace);
        } else {
            if (namespace.equals("http://www.w3.org/2001/XMLSchema")
                    || namespace.equals("http://www.w3.org/XML/1998/namespace")) {
                prefix = "xml";
            } else {
                prefix = "pr" + this.namespaces.keySet().size();
            }

            this.namespaces.put(namespace, prefix);
        }

        return prefix;
    }

    private ArrayList<String> visitedElements = new ArrayList<String>();

    private String elementLabel(XSElementDecl e) {
        String type = e.getType().toString();

        return e.getTargetNamespace() + ":" + e.getName() + "[" + type + "]";
    }

    private Element getElementDescription(XSElementDecl edecl, String xPath) {
        Element result = new Element();
        if (edecl != null) {

            String namespace = edecl.getTargetNamespace();

            XSComplexType complexType = edecl.getType().asComplexType();
            if (complexType != null) {
                XSContentType contentType = complexType.getContentType();
                XSSimpleType simpleType = contentType.asSimpleType();
                XSParticle particle = contentType.asParticle();
                String name = edecl.getName();
                XSType xstype = edecl.getType().getBaseType();
                while (xstype.getBaseType() != null) {
                    if (xstype.getName().equals(xstype.getBaseType().getName()))
                        break;
                    if (xstype.getName().equalsIgnoreCase("string"))
                        break;
                    xstype = xstype.getBaseType();
                }
                String type = xstype.getName();
                result.setName(name);
                result.setxPathFromRoot(xPath + "/" + this.namespaces.get(xstype.getTargetNamespace()) + ":" + name);
                result.setType(type);


                if (namespace.length() > 0) {
                    result.setPrefix(this
                            .getPrefixForNamespace(namespace));
                }

                // process attributes
                List<Attribute> attributes = this
                        .processElementAttributes(complexType, result.getxPathFromRoot());
                result.setAttributes(attributes);

                // process enumerations
                if (simpleType != null) {
                    List<String> enumerations = this
                            .processElementEnumerations(simpleType);
                    if (enumerations.size() > 0) {
                        result.setEnumerations(enumerations);
                    }
                }

                // process children
                if (particle != null) {
                    visitedElements.add(this.elementLabel(edecl));
                    List<Element> elementChildren = this.getParticleMappingChildren(particle, result.getxPathFromRoot());

                    result.setElements(elementChildren);
                    visitedElements.remove(this.elementLabel(edecl));
                }
            } else {
                XSSimpleType simpleType = edecl.getType().asSimpleType();

                // process enumerations
                if (simpleType != null) {
                    List<String> enumerations = this
                            .processElementEnumerations(simpleType);
                    if (enumerations.size() > 0) {
                        result.setEnumerations(enumerations);
                    }
                }

                result.setName(edecl.getName());


                if (namespace.length() > 0) {
                    result.setPrefix(this
                            .getPrefixForNamespace(namespace));
                }
            }
        } else {
            log.error(edecl + " is null!...");
        }


        if (result.getElements() == null
                || result.getElements().size() == 0) {
            if (result.getType() == null || result.getType().equals("anyType")) {
                result.setType("string");
            }
        }

        return result;
    }

    private Element processChildParticle(XSParticle p, String xPath) {

        Element child = this.getElementDescription(p.getTerm()
                .asElementDecl(), xPath);
        BigInteger maxOccurs = p.getMaxOccurs();
        BigInteger minOccurs = p.getMinOccurs();
        child.setMaxOccurs(maxOccurs.intValue());
        child.setMinOccurs(minOccurs.intValue());
        if (child.getMinOccurs()==1) {
            child.setMandatory(true);

            Set<String> man = child.getMandatoryXpath();
            if (man == null) {
                man = new HashSet<>();
            }
            man.add(child.getxPathFromRoot());
        }

        log.debug("process child particle: " + child);

        return child;
    }

    public Mapping buildTemplate(MappingSchema schema, String name, String root, Map<String,String> namespaces) {
        this.namespaces = namespaces;
        Iterator<XSSchema> i = this.schemaSet.iterateSchema();
        XSElementDecl rootElementDecl;
        while (i.hasNext()) {
            XSSchema s = i.next();
            rootElementDecl = s.getElementDecl(root);
            if (rootElementDecl != null) {
                return buildTemplate(schema, name, rootElementDecl);
            }
        }

        return new Mapping();
    }

    private Mapping buildTemplate(MappingSchema schema, String name,
                                 XSElementDecl rootElementDecl) {
        Mapping result = new Mapping();
        result.setObjId(new ObjectId());
        String root = rootElementDecl.getName();

        Mappings mappings = new Mappings();
        mappings.setId(new ObjectId());
        result.setName("template_" + root);
        mappings.setNamespaces(namespaces);
        result.setTargetSchema(schema);

        XSType xstype = rootElementDecl.getType().getBaseType();
        while (xstype.getBaseType() != null) {
            if (xstype.getName().equals(xstype.getBaseType().getName()))
                break;
            if (xstype.getName().equalsIgnoreCase("string"))
                break;
            xstype = xstype.getBaseType();
        }

        visitedElements.add(this.elementLabel(rootElementDecl));


        XSComplexType complexType = rootElementDecl.getType().asComplexType();

        if (complexType != null) {
            XSContentType contentType = complexType.getContentType();
            XSParticle particle = contentType.asParticle();
            List<Attribute> attributes = this.processElementAttributes(complexType,
                    namespaces.get(rootElementDecl.getTargetNamespace()) + ":" + rootElementDecl.getName());
            mappings.setAttributes(attributes);

            if (particle != null) {
                List<Element> elementChildren = new ArrayList<Element>();

                ArrayList<XSParticle> array = this.getParticleChildren(particle);
                for (XSParticle p : array) {
                    if (p.getTerm().isElementDecl()) {
                        Element child;
                        if (!visitedElements.contains(this.elementLabel(p.getTerm().asElementDecl()))) {
                            child = this.buildTemplateForElement(p, namespaces.get(rootElementDecl.getTargetNamespace()) + ":" + rootElementDecl.getName());
                            elementChildren.add(child);
                        }
                    } else if (p.getTerm().isModelGroupDecl() || p.getTerm().isModelGroup()) {
                        boolean isChoice = false;
                        if (p.getTerm().isModelGroup()) {
                            isChoice = (p.getTerm().asModelGroup().getCompositor() == XSModelGroup.CHOICE);
                        } else if (p.getTerm().isModelGroupDecl()) {
                            isChoice = (p.getTerm().asModelGroupDecl().getModelGroup().getCompositor() == XSModelGroup.CHOICE);
                        }

                        log.debug("TEMPLATE MODEL " + rootElementDecl.getName());

                        XSModelGroup group = p.getTerm().asModelGroupDecl()
                                .getModelGroup();
                        XSParticle[] groupChildren = group.getChildren();
                        for (XSParticle gp : groupChildren) {
                            //String name = gp.getTerm().asElementDecl().getName();
                            Element child;
                            if (!visitedElements.contains(this.elementLabel(gp.getTerm().asElementDecl()))) {
                                child = this.buildTemplateForElement(gp, namespaces.get(rootElementDecl.getTargetNamespace()) + ":" + rootElementDecl.getName());
                                if (isChoice) child.setMinOccurs(0);
                                elementChildren.add(child);
                            }
                        }
                    }
                }

                mappings.setRootElement(namespaces.get(rootElementDecl.getTargetNamespace()) + ":" + rootElementDecl.getName());
                mappings.setElements(elementChildren);
            }
        }

        visitedElements.remove(this.elementLabel(rootElementDecl));
        parseAnnotations();
        appendDocumentation(mappings);
        MappingSchema schema1 = result.getTargetSchema();
        result.setTargetSchema(schema1);
        result.setMappings(mappings);
        result.setName(name);
        result.setCreationDate(new Date());

        result.setSchematronRules(schematronRules);

        return result;
    }

    private void appendDocumentation(Mappings mappings) {
        List<Element> rootsToAppend = new ArrayList<>();
        for(Element element:mappings.getElements()){

            if(element.getElements()!=null && element.getElements().size()>0){
                List<Element> newChildren = new ArrayList<>();
                for(Element child:element.getElements()){
                    newChildren.add(appendDocumentation(child));
                }
                element.setElements(newChildren);
            }
            rootsToAppend.add(appendDocumentation(element));
        }
        mappings.setElements(rootsToAppend);
    }

    private Element appendDocumentation(Element element) {
        if(documentation!=null&&documentation.size()>0) {
            for (Map.Entry<String, String> entry : documentation.entrySet()) {
                if(StringUtils.equals(entry.getKey(),element.getPrefix()+":"+element.getName())){
                    element.setDocumentation(entry.getValue());
                }
            }
        }
        return element;
    }


    private Element buildTemplateForElement(XSParticle rootElementDecl, String prevXPath) {
        Element result = new Element();
        String root = rootElementDecl.getTerm().asElementDecl().getName();
        String namespace = rootElementDecl.getTerm().asElementDecl().getTargetNamespace();
        String prefix = namespaces.get(namespace);
        result.setName(root);
        result.setPrefix(prefix);
        result.setNamespace(namespace);
        result.setxPathFromRoot(prevXPath + "/" + prefix + ":" + root);
        result.setMinOccurs(rootElementDecl.getMinOccurs().intValue());
        result.setMaxOccurs(rootElementDecl.getMaxOccurs().intValue());
        Set<String> mandatory = new HashSet<String>();
        if (result.getMinOccurs()==1) {
            result.setMandatory(true);
            mandatory.add(result.getxPathFromRoot());
        }
        visitedElements.add(this.elementLabel(rootElementDecl.getTerm().asElementDecl()));

        XSComplexType complexType = rootElementDecl.getTerm().asElementDecl().getType().asComplexType();
        XSSimpleType simpleType = rootElementDecl.getTerm().asElementDecl().getType().asSimpleType();

        if (complexType != null) {
            XSContentType contentType = complexType.getContentType();
            XSParticle particle = contentType.asParticle();
            List<Attribute> attributes = this.processElementAttributes(complexType, result.getxPathFromRoot());
            result.setAttributes(attributes);
            if (attributes != null) {
                for (Attribute attr : attributes) {
                    if (attr.isMandatory()) {

                        mandatory.add(attr.getxPathFromRoot());
                    }
                }
            }
            if (particle != null) {
                List<Element> elementChildren = new ArrayList<Element>();

                ArrayList<XSParticle> array = this.getParticleChildren(particle);
                for (XSParticle p : array) {
                    if (p.getTerm().isElementDecl()) {
                        Element child;
                        if (!visitedElements.contains(this.elementLabel(p.getTerm().asElementDecl()))) {
                            child = this.buildTemplateForElement(p, result.getxPathFromRoot());
                            elementChildren.add(child);
                        }
                    } else if (p.getTerm().isModelGroupDecl() || p.getTerm().isModelGroup()) {
                        boolean isChoice = false;
                        if (p.getTerm().isModelGroup()) {
                            isChoice = (p.getTerm().asModelGroup().getCompositor() == XSModelGroup.CHOICE);
                        } else if (p.getTerm().isModelGroupDecl()) {
                            isChoice = (p.getTerm().asModelGroupDecl().getModelGroup().getCompositor() == XSModelGroup.CHOICE);
                        }

                        log.debug("TEMPLATE MODEL " + rootElementDecl.getTerm().asElementDecl().getName());

                        XSModelGroup group = p.getTerm().asModelGroupDecl()
                                .getModelGroup();
                        XSParticle[] groupChildren = group.getChildren();
                        for (XSParticle gp : groupChildren) {
                            Element child;
                            if (!visitedElements.contains(this.elementLabel(gp.getTerm().asElementDecl()))) {
                                child = this.buildTemplateForElement(gp, result.getxPathFromRoot());
                                if (isChoice) child.setMinOccurs(0);
                                elementChildren.add(child);
                            }
                        }
                    }
                }

                result.setElements(elementChildren);
            }
        }

        visitedElements.remove(this.elementLabel(rootElementDecl.getTerm().asElementDecl()));

        // process enumerations
        if (simpleType != null) {
            List<String> enumerations = this
                    .processElementEnumerations(simpleType);
            if (enumerations.size() > 0) {
                result.setEnumerations(enumerations);
            }

            result.setType("string");
        }
        result.setMandatoryXpath(mandatory);
        return result;
    }


    private List<Attribute> processElementAttributes(XSComplexType complexType, String xPath) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        Iterator<? extends XSAttributeUse> aitr = complexType
                .iterateAttributeUses();
        while (aitr.hasNext()) {
            XSAttributeUse attributeUse = aitr.next();
            XSAttributeDecl attributeDecl = attributeUse.getDecl();
            String namespace = attributeDecl.getTargetNamespace();
            Attribute attribute = new Attribute();
            attribute.setName(attributeDecl.getName());
            attribute.setNamespace(namespace);

            if (namespace.length() > 0) {
                attribute.setPrefix("@" + this.getPrefixForNamespace(namespace));
            }
            attribute.setxPathFromRoot(xPath + "/" +attribute.getPrefix() + ":" + attribute.getName());

            if (attributeUse.isRequired()) {
                attribute.setMinOccurs(1);
                attribute.setMandatory(true);
            }

            // check for enumerations in attributes
            XSSimpleType simpleType = attributeDecl.getType();
            List<String> enumerations = this
                    .processElementEnumerations(simpleType);
            if (enumerations.size() > 0) {
                attribute.setEnumerations(enumerations);
            }

            boolean alreadyIn = false;
            for (Attribute at : attributes) {
                if (at.getName().equals(attribute.getName())) {
                    if (at.getPrefix().equals(attribute.getPrefix())) {
                        alreadyIn = true;
                    }
                }
            }

            if (!alreadyIn) attributes.add(attribute);
        }

        return attributes;
    }

    private List<String> processElementEnumerations(XSSimpleType simpleType) {
        List<String> enumerations = new ArrayList<String>();

        XSRestrictionSimpleType restriction = simpleType.asRestriction();
        if (restriction != null) {
            Iterator<? extends XSFacet> i = restriction.getDeclaredFacets()
                    .iterator();
            while (i.hasNext()) {
                XSFacet facet = i.next();
                if (facet.getName().equals(XSFacet.FACET_ENUMERATION)) {

                    enumerations.add(facet.getValue().value);
                }
            }
        }

        return enumerations;
    }


    private List<Node> processElementAnnotation(XSElementDecl edecl) {
        List<Node> annotations = new ArrayList<Node>();

        if (edecl.getType().getAnnotation() != null
                && edecl.getType().getAnnotation().getAnnotation() != null) {
            annotations.add((Node) edecl.getType().getAnnotation().getAnnotation());
        }

        if (edecl.getAnnotation() != null
                && edecl.getAnnotation().getAnnotation() != null) {
            annotations.add((Node) edecl.getAnnotation().getAnnotation());
        }

        return annotations;
    }

    private ArrayList<XSParticle> getParticleChildren(XSParticle particle) {
        ArrayList<XSParticle> children = new ArrayList<XSParticle>();

        // process children
        if (particle != null) {
            XSTerm term = particle.getTerm();
            XSModelGroup group = null;

            if (term.isModelGroup()) {
                group = term.asModelGroup();
            } else if (term.isModelGroupDecl()) {
                group = term.asModelGroupDecl().getModelGroup();
            }

            if (group != null) {
                XSParticle[] particles = group.getChildren();
                for (XSParticle p : particles) {
                    if (p.getTerm().isElementDecl()) {
                        children.add(p);
                    } else {
                        ArrayList<XSParticle> particleChildren = this.getParticleChildren(p);
                        children.addAll(particleChildren);
                    }
                }
            }
        }


        return children;
    }

    private List<Element> getParticleMappingChildren(XSParticle particle, String xPath) {
        List<Element> children = new ArrayList<Element>();

        boolean isChoice = false;
        boolean isSequence = false;
        if (particle.getTerm().isModelGroup()) {
            isChoice = (particle.getTerm().asModelGroup().getCompositor() == XSModelGroup.CHOICE);
            isSequence = (particle.getTerm().asModelGroup().getCompositor() == XSModelGroup.SEQUENCE);
            log.debug("model group: " + particle.getTerm().asModelGroup().getCompositor() + " " + particle.getMinOccurs() + "/" + particle.getMaxOccurs());
        } else if (particle.getTerm().isModelGroupDecl()) {
            isChoice = (particle.getTerm().asModelGroupDecl().getModelGroup().getCompositor() == XSModelGroup.CHOICE);
            isSequence = (particle.getTerm().asModelGroupDecl().getModelGroup().getCompositor() == XSModelGroup.SEQUENCE);
            log.debug("model group decl: " + particle.getTerm().asModelGroupDecl().getModelGroup().getCompositor() + " " + particle.getMinOccurs() + "/" + particle.getMaxOccurs());
        }
        BigInteger compositorMaxOccurs = particle.getMaxOccurs();

        // process children
        XSTerm term = particle.getTerm();
        XSModelGroup group = null;

        if (term.isModelGroup()) {
            group = term.asModelGroup();
        } else if (term.isModelGroupDecl()) {
            group = term.asModelGroupDecl().getModelGroup();
        }

        if (group != null) {
            XSParticle[] particles = group.getChildren();
            for (XSParticle p : particles) {
                if (p.getTerm().isElementDecl()) {
                    log.debug("particle: " + p.getTerm().asElementDecl().getName() + " " + particle.getTerm());
                    if (!visitedElements.contains(this.elementLabel(p.getTerm().asElementDecl()))) {
                        Element child = this.processChildParticle(p, xPath);
                        if (isChoice) {
                            child.setMinOccurs(0);
                        }
                        children.add(child);
                        log.debug("child: " + child);
                    }
                } else {
                    List<Element> particleChildren = this.getParticleMappingChildren(p, xPath);
                    if (isSequence) {
                        if ((p.getTerm().isModelGroup() && p.getTerm().asModelGroup().getCompositor() == XSModelGroup.CHOICE) ||
                                (p.getTerm().isModelGroupDecl() &&
                                        ((p.getTerm().asModelGroupDecl().asModelGroup() != null && p.getTerm().asModelGroup().getCompositor() == XSModelGroup.CHOICE)))) {
                            for (Element o : particleChildren) {
                                o.setMaxOccurs(compositorMaxOccurs.intValue());
                            }
                        }
                    }
                    children.addAll(particleChildren);
                }
            }
        }


        return children;
    }

    private void parseAnnotations() {
        documentation = new HashMap<>();
        schematronRules = new HashSet<>();

        Iterator<XSElementDecl> i = this.schemaSet.iterateElementDecls();
        while (i.hasNext()) {
            XSElementDecl e = i.next();
            this.parseAnnotations(e);
        }
    }

    public Map<String, String> getDocumentation() {
        if (this.documentation == null) this.parseAnnotations();
        return this.documentation;
    }

    private void parseAnnotations(XSElementDecl edecl) {

        List<Node> annotations = this.processElementAnnotation(edecl);
        log.debug(edecl.getName() + " annotations: " + annotations);
        String documentationText = "";
        String schematronText = "";

        for (Node node : annotations) {
            NodeList childNodes = node.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {

                Node subnode = childNodes.item(i);
                if (subnode.getNodeName().equals("documentation")) {
                    documentationText += subnode.getTextContent();
                }

                if (subnode.getNodeName().equals("appinfo")) {
                    NodeList schematronNodes = subnode.getChildNodes();

                    for (int j = 0; j < schematronNodes.getLength(); j++) {
                        Node schematronNode = schematronNodes.item(j);
                        if (schematronNode.getNamespaceURI() != null && schematronNode.getNamespaceURI().equals("http://purl.oclc.org/dsdl/schematron")) {
                            try {
                                schematronText += fromDOM(schematronNode, true);
                            } catch (TransformerException e) {
                                log.error(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }

        String name = edecl.getName();
        String namespace = edecl.getTargetNamespace();

        String tag = name;
        if (namespace.length() != 0) {
            String prefix = this.getPrefixForNamespace(namespace);
            tag = prefix + ":" + name;
        }

        if (documentation.containsKey(name)) {
            if (documentationText.equals(documentation.get(name))) {
            }
        }

        if (documentationText.length() > 0) {
            documentation.put(tag, documentationText);
        }

        if (schematronText.length() > 0) {
            schematronRules.add(schematronText);
        }

        XSComplexType complexType = edecl.getType().asComplexType();
        if (complexType != null) {
            // proccess children
            XSContentType contentType = complexType.getContentType();
            XSParticle particle = contentType.asParticle();
            XSType xstype = edecl.getType().getBaseType();

            while (xstype.getBaseType() != null) {
                if (xstype.getName().equals(xstype.getBaseType().getName()))
                    break;
                if (xstype.getName().equalsIgnoreCase("string"))
                    break;
                xstype = xstype.getBaseType();
            }

            if (particle != null) {
                visitedElements.add(this.elementLabel(edecl));

                ArrayList<XSParticle> array = this
                        .getParticleChildren(particle);
                for (XSParticle p : array) {
                    if (p.getTerm().isElementDecl()) {
                        if (!visitedElements.contains(this.elementLabel(p.getTerm()
                                .asElementDecl()))) {
                            XSElementDecl child = p.getTerm().asElementDecl();
                            this.parseAnnotations(child);
                        }
                    } else if (p.getTerm().isModelGroupDecl()) {
                        XSModelGroup group = p.getTerm().asModelGroupDecl()
                                .getModelGroup();
                        XSParticle[] groupChildren = group.getChildren();
                        for (XSParticle gp : groupChildren) {
                            if (!visitedElements.contains(this.elementLabel(gp.getTerm()
                                    .asElementDecl()))) {
                                XSElementDecl child = gp.getTerm().asElementDecl();
                                this.parseAnnotations(child);
                            }
                        }
                    }
                }

                visitedElements.remove(this.elementLabel(edecl));
            }


        }
    }


    private String fromDOM(Node node, boolean omitXmlDeclaration) throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        if (omitXmlDeclaration) transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node),
                new StreamResult(buffer));

        return buffer.toString();
    }


}
