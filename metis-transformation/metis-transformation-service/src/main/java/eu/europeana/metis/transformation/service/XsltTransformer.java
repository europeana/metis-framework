package eu.europeana.metis.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;

/**
 * Created by pwozniak on 3/21/18
 */
public class XsltTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(XsltTransformer.class);

    private static final int CACHE_SIZE = 50;
    private static LRUCache<String, Transformer> cache = new LRUCache<>(CACHE_SIZE);

    /**
     * Transforms given file based on a provided url to xslt file.
     *
     * @param xsltUrl     url to xslt file
     * @param fileContent content of the file
     * @return transformed file
     * @throws TransformationException if a problem occurred during transformation
     */
    public StringWriter transform(String xsltUrl, byte[] fileContent) throws TransformationException {
        return transform(xsltUrl, fileContent, null);
    }

    /**
     * Transforms given file based on a provided url to xslt file.
     *
     * @param xsltUrl        url to xslt file
     * @param fileContent    content of the file
     * @param datasetIdValue value that will be injected to datasetId field
     * @return transformed file
     * @throws TransformationException if a problem occurred during transformation
     */
    public StringWriter transform(String xsltUrl, byte[] fileContent, String datasetIdValue) throws TransformationException {
        try(final InputStream xsltStream = new URL(xsltUrl).openStream()) {
            final Transformer transformer = getTransformer(xsltUrl, xsltStream);
            addParameter(transformer, "datasetId", datasetIdValue);
            try(final InputStream contentStream = new ByteArrayInputStream(fileContent)) {
                return executeTransformation(transformer, contentStream);
            }
        } catch (IOException | TransformerException e) {
            LOGGER.error("Exception during transformation", e);
            throw new TransformationException(e);
        }
    }

    private Transformer getTransformer(String xsltUrl, InputStream xsltStream) throws TransformerConfigurationException {
        if (cache.containsKey(xsltUrl)) {
            return cache.get(xsltUrl);
        } else {
            Source xslDoc = new StreamSource(xsltStream);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(xslDoc);
            cache.put(xsltUrl, transformer);
            return transformer;
        }
    }

    private StringWriter executeTransformation(Transformer transformer, InputStream contentStream) throws TransformerException {
        Source xmlDoc = new StreamSource(contentStream);
        StringWriter writer = new StringWriter();
        transformer.transform(xmlDoc, new StreamResult(writer));
        return writer;
    }

    private void addParameter(Transformer transformer, String parameterName, String parameterValue) {

        if (parameterValue != null && !parameterValue.isEmpty()) {
            transformer.setParameter(parameterName, parameterValue);
        }
    }
}
