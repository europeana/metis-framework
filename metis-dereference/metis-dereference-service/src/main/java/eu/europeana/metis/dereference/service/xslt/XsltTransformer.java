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
package eu.europeana.metis.dereference.service.xslt;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * Apply an XSLT to incoming data and convert to string
 * Created by ymamakis on 2/11/16.
 */
public class XsltTransformer {

    /**
     * Apply the XSLT transformation
     * @param record The incoming unmapped entity
     * @param xslt Applying the XSLT
     * @return The mapped entity to EDM
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public String transform(String record, String xslt) throws TransformerException, ParserConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates template = factory.newTemplates(new StreamSource(new ByteArrayInputStream(xslt.getBytes())));

        Transformer xformer = template.newTransformer();
        Source source = new StreamSource(new ByteArrayInputStream(record.getBytes()));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        DOMResult result = new DOMResult(doc);
        xformer.transform(source, result);
        return getStringFromDocument(doc);
    }

    private String getStringFromDocument(Document doc) throws TransformerException {

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();

    }
}
