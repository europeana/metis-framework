package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import eu.europeana.validation.model.ValidationResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
     * @return the schema object
     * @throws SchemaProviderException 
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
        source.setByteStream(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
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


