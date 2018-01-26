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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * EDM Validator class
 * Created by gmamakis on 18-12-15.
 */
public class Validator implements Callable<ValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
    private static ConcurrentMap<String, Templates> templatesCache;

    private String schema;
    private String rootFileLocation;
    private String schematronFileLocation;
    private String document;
    private SchemaProvider schemaProvider;
    private ClasspathResourceResolver resolver;

    static {
        templatesCache = new ConcurrentHashMap<>();
    }


    /**
     * Constructor specifying the schema to validate against and the document
     *
     * @param schema                 schema that will be used for validation
     * @param rootFileLocation       location of the schema root file
     * @param schematronFileLocation location of the schematron file
     * @param document               document that will be validated
     * @param schemaProvider
     * @param resolver
     */
    public Validator(String schema, String rootFileLocation, String schematronFileLocation, String document, SchemaProvider schemaProvider, ClasspathResourceResolver resolver) {
        this.schema = schema;
        this.rootFileLocation = rootFileLocation;
        this.schematronFileLocation = schematronFileLocation;
        this.document = document;
        this.schemaProvider = schemaProvider;
        this.resolver = resolver;
    }

    /**
     * Get schema object specified with its name
     *
     * @param schemaName name of the schema
     * @return
     */
    private Schema getSchemaByName(String schemaName) throws SchemaProviderException {
        if (schemaProvider.isPredefined(schemaName)) {
            return schemaProvider.getSchema(schemaName);
        } else {
            if (rootFileLocation != null) {
                return schemaProvider.getSchema(schemaName, rootFileLocation, schematronFileLocation);
            } else {
                throw new SchemaProviderException("Missing root file location for custom schema");
            }
        }
    }

    /**
     * Validate method using JAXP
     *
     * @return The outcome of the Validation
     */
    private ValidationResult validate() {
        LOGGER.info("Validation started");
        InputSource source = new InputSource();
        source.setByteStream(new ByteArrayInputStream(document.getBytes()));
        try {
            Schema savedSchema = getSchemaByName(schema);
            if (savedSchema == null) {
                return constructValidationError(document, "Specified schema does not exist");
            }

            resolver.setPrefix(StringUtils.substringBeforeLast(savedSchema.getPath(), File.separator));

            Document doc = EDMParser.getInstance().getEdmParser().parse(source);
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
        } catch (IOException | SchemaProviderException | SAXException | TransformerException e) {
            return constructValidationError(document, e);
        }
        LOGGER.info("Validation ended");
        return constructOk();
    }

    private Transformer getTransformer(Schema schema)
            throws IOException, TransformerConfigurationException {
        StringReader reader;
        String schematronPath = schema.getSchematronPath();

        if (!templatesCache.containsKey(schematronPath)) {
            reader = new StringReader(IOUtils.toString(new FileInputStream(schematronPath)));
            Templates template = TransformerFactory.newInstance()
                    .newTemplates(new StreamSource(reader));
            templatesCache.put(schematronPath, template);
        }

        return templatesCache.get(schematronPath).newTransformer();
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
final class EDMParser {
    private static EDMParser p;
    private static final ConcurrentMap<String, javax.xml.validation.Schema> cache;
    private static final DocumentBuilderFactory parseFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(EDMParser.class);

    static {
        cache = new ConcurrentHashMap<>();
        DocumentBuilderFactory temp = null;
        try {
            temp = DocumentBuilderFactory.newInstance();
            temp.setNamespaceAware(true);
            temp.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            temp.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Unable to create DocumentBuilderFactory", e);
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
            LOGGER.error("Unable to configure parser", e);
        }
        return null;
    }

    /**
     * Get a JAXP schema validator (singleton)
     *
     * @param path The path location of the schema
     * @param resolver
     * @return
     */
    public javax.xml.validation.Validator getEdmValidator(String path, LSResourceResolver resolver) {
        try {
            javax.xml.validation.Schema schema = getSchema(path, resolver);
            return schema.newValidator();
        } catch (SAXException | IOException e) {
            LOGGER.error("Unable to create validator", e);
        }
        return null;
    }

    private javax.xml.validation.Schema getSchema(String path,
                                                  LSResourceResolver resolver)
            throws SAXException, IOException {

        if (!cache.containsKey(path)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(resolver);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
                    false);
            factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
            javax.xml.validation.Schema schema = factory.newSchema(new StreamSource(new FileInputStream(path)));
            cache.put(path, schema);
        }
        return cache.get(path);
    }

    /**
     * Get a parser instance as a singleton
     *
     * @return
     */
    public static synchronized EDMParser getInstance() {
        if (p == null) {
            p = new EDMParser();
        }
        return p;
    }


}
