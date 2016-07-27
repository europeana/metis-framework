package eu.europeana.enrichment.harvester.transform;

import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Template that converts the RDF representation of a contextual Class to Europeana compatible POJO
 *
 * @param <S> Implementation Class of a Contextual Entity
 * @author Yorgos.Mamakis@ europeana.eu
 */
public abstract class Template<S extends ContextualClassImpl> {

    private static final Logger log = Logger.getLogger(Template.class
            .getCanonicalName());
    private static final String RDF_RESOURCE = "rdf:resource";
    private static final String XML_LANG = "xml:lang";
    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private void appendValue(S obj, String methodName, Object value) {
        if (value.getClass().isAssignableFrom(String.class)) {
            obj.setAbout((String) value);
        } else {
            Class type = null;
            Method found = null;

            Method getter;
            try {
                getter = obj.getClass().getMethod(
                        StringUtils.replace(methodName, "set", "get"));

                for (Method method : obj.getClass().getMethods()) {
                    if (StringUtils.equals(method.getName(), methodName)) {
                        type = method.getParameterTypes()[0];
                        found = method;
                        break;
                    }
                }
                if (type.isAssignableFrom(String[].class)) {
                    String[] vals = (String[]) getter.invoke(obj);
                    if (vals == null) {
                        vals = new String[1];
                        vals[0] = ((AttributeHolder) value).getAttributeValue();
                        found.invoke(obj, (Object) vals);

                    } else {
                        List<String> strs = new ArrayList<>(Arrays.asList(vals));
                        strs.add(((AttributeHolder) value).getAttributeValue());
                        String[] finalArray = strs.toArray(new String[strs.size()]);
                        found.invoke(obj, (Object) finalArray);
                    }
                }
                if (type.isAssignableFrom(Float.class)) {

                        found.invoke(obj, Float.parseFloat(((AttributeHolder)value).elementValue));

                }
                if (type.isAssignableFrom(Map.class)) {
                    Map<String, List<String>> vals = (Map<String, List<String>>) getter
                            .invoke(obj);
                    AttributeHolder attr = (AttributeHolder) value;
                    if (attr.getAttributeName() != null
                            && attr.getAttributeName().equals(RDF_RESOURCE)) {
                        if (vals == null) {
                            vals = new HashMap<>();
                            List<String> str = new ArrayList<>();
                            str.add(attr.attributeValue);
                            vals.put("def", str);
                        } else {
                            List<String> str = new ArrayList<>();
                            if (vals.containsKey("def")) {
                                str = vals.get("def");
                            }
                            str.add(attr.attributeValue);
                            vals.put("def", str);
                        }
                    } else {
                        if (attr.getAttributeName() == null) {
                            if (vals == null) {
                                vals = new HashMap<>();
                                List<String> str = new ArrayList<>();
                                str.add(attr.elementValue);
                                vals.put("def", str);
                            } else {
                                List<String> str = new ArrayList<>();
                                if (vals.containsKey("def")) {
                                    str = vals.get("def");
                                }
                                str.add(attr.elementValue);
                                vals.put("def", str);
                            }
                        } else {
                            if (vals == null) {
                                vals = new HashMap<>();
                                List<String> str = new ArrayList<>();
                                str.add(attr.elementValue);
                                vals.put(attr.attributeValue, str);
                            } else {
                                List<String> str = new ArrayList<>();
                                if (vals.containsKey(attr.attributeValue)) {
                                    str = vals.get(attr.attributeValue);
                                }
                                str.add(attr.elementValue);
                                vals.put(attr.attributeValue, str);
                            }
                        }
                    }
                    found.invoke(obj, vals);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                    InvocationTargetException e) {
                log.log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that parses the EDM/XML representation of contextual Entity and returns a POJO
     *
     * @param obj           The Europeana Compatible Java class for a contextual entity
     * @param resourceUri   The URI of the contextual class
     * @param xml           The EDM/XML representation of the contextual class
     * @param methodMapping The mapping between the EDM fields and the Java setter
     * @return The populated POJO
     */
    protected S parse(S obj, String resourceUri, String xml,
                      Map<String, String> methodMapping) {
        //System.out.println ("before "+ xml+"\n end before");


        //to be optimized
        xml = xml.replace("&#55296;", "");
        xml = xml.replace("&#55362;", "");
        xml = xml.replace("&#55363;", "");
        xml = xml.replace("&#55364;", "");
        xml = xml.replace("&#55378;", "");
        xml = xml.replace("&#55381;", "");
        xml = xml.replace("&#55384;", "");
        xml = xml.replace("&#55391;", "");
        xml = xml.replace("&#55394;", "");
        xml = xml.replace("&#55629;", "");
        xml = xml.replace("&#56256;", "");
        xml = xml.replace("&#56322;", "");
        xml = xml.replace("&#56351;", "");
        xml = xml.replace("&#56384;", "");
        xml = xml.replace("&#56394;", "");
        xml = xml.replace("&#56513;", "");
        xml = xml.replace("&#56648;", "");
        xml = xml.replace("&#56657;", "");
        xml = xml.replace("&#56786;", "");
        xml = xml.replace("&#56855;", "");
        xml = xml.replace("&#57086;", "");
        xml = xml.replace("&#57113;", "");
        xml = xml.replace("&#57121;", "");
        xml = xml.replace("&#57136;", "");
        xml = xml.replace("&#57137;", "");
        xml = xml.replace("&#57138;", "");
        xml = xml.replace("&#57139;", "");
        xml = xml.replace("&#57140;", "");
        xml = xml.replace("&#57142;", "");
        xml = xml.replace("&#57143;", "");
        xml = xml.replace("&#57144;", "");
        xml = xml.replace("&#57145;", "");
        xml = xml.replace("&#57146;", "");
        xml = xml.replace("&#57147;", "");
        xml = xml.replace("&#57148;", "");
        xml = xml.replace("&#57149;", "");
        xml = xml.replace("&#57150;", "");
        xml = xml.replace("&#57151;", "");
        xml = xml.replace("&#57152;", "");
        xml = xml.replace("&#57154;", "");
        xml = xml.replace("&#57155;", "");
        xml = xml.replace("&#57156;", "");
        xml = xml.replace("&#57157;", "");
        xml = xml.replace("&#57158;", "");
        xml = xml.replace("&#57159;", "");
        xml = xml.replace("&#57161;", "");
        xml = xml.replace("&#57271;", "");
        xml = xml.replace("&#57279;", "");

        //

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        InputStream in = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        //String xml10pattern = "[^"
        //+ "\u0009\r\n"
        //+ "\u0020-\uD7FF"
        //+ "\uE000-\uFFFD"
        //+ "\ud800\udc00-\udbff\udfff"
        //+ "]";
        try {
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            boolean isRoot = true;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    String qElem = startElement.getName().getPrefix() + ":"
                            + startElement.getName().getLocalPart();

                    if (methodMapping.containsKey(qElem)) {
                        if (isRoot) {
                            appendValue(obj, methodMapping.get(qElem),
                                    resourceUri);
                            isRoot = !isRoot;
                        } else {

                            Attribute attr = startElement
                                    .getAttributeByName(new QName(RDF_NAMESPACE,
                                            "resource"));
                            Attribute langAttr = startElement
                                    .getAttributeByName(new QName(XML_NAMESPACE, "lang"));
                            Attribute about = startElement
                                    .getAttributeByName(new QName(RDF_NAMESPACE,
                                            "about"));
                            if (attr != null) {
                                AttributeHolder attribute = new AttributeHolder();
                                attribute.setAttributeName(RDF_RESOURCE);
                                attribute.setAttributeValue(attr.getValue());
                                appendValue(obj, methodMapping.get(qElem),
                                        attribute);
                            } else {
                                if (about == null) {
                                    AttributeHolder attribute = new AttributeHolder();
                                    if (langAttr != null) {
                                        attribute.setAttributeName(XML_LANG);
                                        attribute.setAttributeValue(langAttr
                                                .getValue());
                                    }

                                    event = eventReader.nextEvent();
                                    if (!event.isEndElement()) {
                                        attribute.setElementValue(event
                                                .asCharacters().getData());
                                        appendValue(obj, methodMapping.get(qElem),
                                                attribute);
                                    }

                                }
                            }
                        }
                    }

                }
            }
            return obj;
        } catch (XMLStreamException | FactoryConfigurationError e) {
            log.log(Level.SEVERE, e.getMessage() + " \n" + xml);
        }
        return null;
    }

    /**
     * Contextual class specific method that needs to be invoked to access the parse() method
     *
     * @param xml         The XML that contains the EDM/XML to be converted
     * @param resourceUri The Resource URI of the original Controlled Vocabulary Resource
     * @return
     */
    public abstract S transform(String xml, String resourceUri);

    class AttributeHolder {

        private String attributeName;
        private String attributeValue;
        private String elementValue;

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeValue() {
            return attributeValue;
        }

        public void setAttributeValue(String attributeValue) {
            this.attributeValue = attributeValue;
        }

        public String getElementValue() {
            return elementValue;
        }

        public void setElementValue(String elementValue) {
            this.elementValue = elementValue;
        }

    }
}
