package eu.europeana.validation.edm.validation;

import eu.europeana.validation.edm.model.ValidationResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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

/**
 * EDM Validator class
 * Created by gmamakis on 18-12-15.
 */
public class Validator implements Callable<ValidationResult> {

    private static final Logger logger = Logger.getRootLogger();

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
            eu.europeana.validation.edm.model.Schema savedSchema = service.getSchemaByName(schema, version);
            EDMParser.getInstance().getEdmValidator(savedSchema.getPath(), StringUtils.substringBeforeLast(savedSchema.getPath(), "/"), resolver).validate(new DOMSource(doc));
            if (StringUtils.isNotEmpty(savedSchema.getSchematronPath())) {

                StringReader reader =null;
                if(resolver.getClass().isAssignableFrom(OpenstackResourceResolver.class)){
                    reader = new StringReader(IOUtils.toString(resolver.getSwiftProvider().getObjectApi()
                            .get(savedSchema.getSchematronPath()).getPayload().openStream()));
                } else {
                    reader = new StringReader(IOUtils.toString(new FileInputStream(savedSchema.getSchematronPath())));
                }
                DOMResult result = new DOMResult();
                Transformer transformer = TransformerFactory.newInstance().newTemplates(new StreamSource(reader)).newTransformer();
                transformer.transform(new DOMSource(doc), result);

                NodeList nresults = result.getNode().getFirstChild().getChildNodes();
                for (int i = 0; i < nresults.getLength(); i++) {
                    Node nresult = nresults.item(i);
                    if ("failed-assert".equals(nresult.getLocalName())) {
                        System.out.println(nresult.getTextContent());
                        return constructValidationError(document, "Schematron error: " + nresult.getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return constructValidationError(document, e);
        }
        return constructOk();
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
 * Helper class for EDM validation exposing two validator and a DOMParser
 */
class EDMParser {
    private static EDMParser p;

    private EDMParser() {


    }

    /**
     * Get an EDM Parser using DOM
     *
     * @return
     */
    public DocumentBuilder getEdmParser() {
        try {
            DocumentBuilderFactory parseFactory = DocumentBuilderFactory.newInstance();

            parseFactory.setNamespaceAware(true);
            parseFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            parseFactory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);

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
    public javax.xml.validation.Validator getEdmValidator(String path, String rootPath, AbstractLSResourceResolver resolver) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            //ClasspathResourceResolver resolver = new ClasspathResourceResolver();
            //Set the prefix as schema since this is the folder where the schemas exist in the classpath
            resolver.setPrefix(rootPath);
            factory.setResourceResolver(resolver);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
            if (resolver.getClass().isAssignableFrom(OpenstackResourceResolver.class)) {
                return factory.newSchema(new StreamSource(resolver.getSwiftProvider().getObjectApi().get(path).getPayload().openStream())).newValidator();
            }
            return factory.newSchema(new StreamSource(new FileInputStream(path))).newValidator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
