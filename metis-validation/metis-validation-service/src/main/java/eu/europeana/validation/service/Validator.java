/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import eu.europeana.validation.model.ValidationResult;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.concurrent.Callable;
import org.xml.sax.SAXException;
import sun.rmi.runtime.Log;

/**
 * EDM Validator class
 * Created by gmamakis on 18-12-15.
 */
public class Validator implements Callable<ValidationResult> {

    private static final Logger logger = Logger.getLogger(Validator.class);
    private static ConcurrentMap<String, Templates> templatesCache;

    static {
        templatesCache = new ConcurrentHashMap<>();
    }


    /**
     * Constructor specifying the schema to validate against and the document
     *
     * @param schema
     * @param document
     */
    public Validator(String schema, String document, String version, ValidationManagementService service, AbstractLSResourceResolver resolver) {
        this.schema = schema;
        this.document = document;
        this.service = service;
        this.version = version;
        this.resolver = resolver;
    }

    private String schema;
    private String document;

    @Autowired
    private ValidationManagementService service;
    private String version;
    private AbstractLSResourceResolver resolver;

    /**
     * Validate method using JAXP
     *
     * @return The outcome of the Validation
     */
    private ValidationResult validate() {
        logger.info("Validation started");
        InputSource source = new InputSource();
        source.setByteStream(new ByteArrayInputStream(document.getBytes()));
        try {
            Document doc = EDMParser.getInstance().getEdmParser().parse(source);
            Schema savedSchema = service.getSchemaByName(schema, version);
            resolver.setPrefix(StringUtils.substringBeforeLast(savedSchema.getPath(), "/"));

            EDMParser.getInstance().getEdmValidator(savedSchema.getPath(), resolver).validate(new DOMSource(doc));
            if (StringUtils.isNotEmpty(savedSchema.getSchematronPath())) {
                Transformer transformer = getTransformer(savedSchema);

                DOMResult result = new DOMResult();
                transformer.transform(new DOMSource(doc), result);

                NodeList nresults = result.getNode().getFirstChild().getChildNodes();
                for (int i = 0; i < nresults.getLength(); i++) {
                    Node nresult = nresults.item(i);
                    if ("failed-assert".equals(nresult.getLocalName())) {
                        return constructValidationError(document, "Schematron error: " + nresult.getTextContent());
                    }
                }
            }
        } catch (Exception e) {
          //  e.printStackTrace();
            return constructValidationError(document, e);
        }
        logger.info("Validation ended");
        return constructOk();
    }

    private Transformer getTransformer(Schema schema)
        throws IOException, TransformerConfigurationException {
        StringReader reader;
        String schematronPath = schema.getSchematronPath();

        if (!templatesCache.containsKey(schematronPath)){
            if (resolver.getClass().isAssignableFrom(ObjectStorageResourceResolver.class)) {
                reader = new StringReader(IOUtils.toString(resolver.getObjectStorageClient().
                    get(schematronPath).get().getPayload().openStream()));
            } else {
                reader = new StringReader(IOUtils.toString(new FileInputStream(schematronPath)));
            }
            Templates template = TransformerFactory.newInstance()
                .newTemplates(new StreamSource(reader));
            templatesCache.put(schematronPath,template);
        }

        return templatesCache.get(schematronPath).newTransformer() ;
    }

    private ValidationResult constructValidationError(String document, Exception e) {
        ValidationResult res = new ValidationResult();
        res.setMessage(e.getMessage());
        res.setRecordId(StringUtils.substringBetween(document, "ProvidedCHO", ">"));
        if (StringUtils.isEmpty(res.getRecordId())) {
            res.setRecordId("Missing record identifier for EDM record");
        }

        res.setSuccess(false);
        return res;
    }

    private ValidationResult constructValidationError(String document, String message) {
        ValidationResult res = new ValidationResult();
        res.setMessage(message);
        res.setRecordId(StringUtils.substringBetween(document, "ProvidedCHO", ">"));
        if (StringUtils.isEmpty(res.getRecordId())) {
            res.setRecordId("Missing record identifier for EDM record");
        }

        res.setSuccess(false);
        return res;
    }

    private ValidationResult constructOk() {
        ValidationResult res = new ValidationResult();

        res.setSuccess(true);
        return res;
    }

    @Override
    public ValidationResult call() {
        return validate();
    }

}


/**
 * Helper class for EDM service exposing two validator and a DOMParser
 */
class EDMParser {
    private static EDMParser p;
    private static final ConcurrentMap<String, javax.xml.validation.Schema> cache;
    private static final DocumentBuilderFactory parseFactory;
    private static final Logger logger = Logger.getLogger(EDMParser.class);

    static {
        cache = new ConcurrentHashMap<>();
        DocumentBuilderFactory temp = null;
        try {
            temp = DocumentBuilderFactory.newInstance();
            temp.setNamespaceAware(true);
            temp.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            temp.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
        } catch (ParserConfigurationException e) {
            logger.error("Unable to create DocumentBuilderFactory", e);
            e.printStackTrace();
        }
        parseFactory = temp;
    }

    private EDMParser() {
    }

    /**
     * Get an EDM Parser using DOM
     *
     * @return
     */
    public DocumentBuilder getEdmParser() {
        try {
            return parseFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a JAXP schema validator (singleton)
     *
     * @param path The path location of the schema
     * @return
     */
    public javax.xml.validation.Validator getEdmValidator(String path, AbstractLSResourceResolver resolver) {
        try {
            javax.xml.validation.Schema schema = getSchema(path, resolver);
            return schema.newValidator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private javax.xml.validation.Schema getSchema(String path,
        AbstractLSResourceResolver resolver)
        throws SAXException, IOException {

        if(!cache.containsKey(path)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //ClasspathResourceResolver resolver = new ClasspathResourceResolver();
            //Set the prefix as schema since this is the folder where the schemas exist in the classpath
            factory.setResourceResolver(resolver);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
                false);
            factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
            javax.xml.validation.Schema schema;
            if (resolver.getClass().isAssignableFrom(ObjectStorageResourceResolver.class)) {
                schema = factory.newSchema(new StreamSource(resolver.getObjectStorageClient().
                    get(path).get().getPayload().openStream()));
            } else {
                schema = factory.newSchema(new StreamSource(new FileInputStream(path)));
            }
            cache.put(path, schema);
        }
        return cache.get(path);
    }

    /**
     * Get a parser instance as a singleton
     *
     * @return
     */
    public static EDMParser getInstance() {
        if (p == null) {
            p = new EDMParser();
        }
        return p;
    }


}
