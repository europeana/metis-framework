package eu.europeana.metis.transformation.service;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import eu.europeana.metis.transformation.service.TransformationException;
import eu.europeana.metis.transformation.service.XsltTransformer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;

/**
 * Created by pwozniak on 3/21/18
 */
public class XsltTransformerTest {

    @Test
    public void shouldTransformGivenFile() throws TransformationException, IOException, ParserConfigurationException, SAXException {
        URL xsltFile =  Resources.getResource("sample_xslt.xslt");
        byte[] fileToTransform = readFile("xmlForTesting.xml");
        StringWriter wr = new XsltTransformer(xsltFile.toString()).transform(fileToTransform);
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(wr.toString())));
        Assert.assertEquals("html", doc.getDocumentElement().getNodeName());
        Assert.assertEquals(1, doc.getElementsByTagName("h2").getLength());
        Assert.assertEquals(1, doc.getElementsByTagName("table").getLength());
        Assert.assertEquals(2, doc.getElementsByTagName("th").getLength());
        Assert.assertEquals(26 + 1, doc.getElementsByTagName("tr").getLength());
        Assert.assertEquals(2 * 26, doc.getElementsByTagName("td").getLength());
    }

    @Test
    public void shouldTransformGivenFileWithInjection() throws IOException, TransformationException {
        URL xsltFile =  Resources.getResource("inject_node.xslt");
        byte[] fileToTransform = readFile("xmlForTestingParamInjection.xml");
        StringWriter wr = new XsltTransformer(xsltFile.toString(), "sample").transform(fileToTransform);
        String resultXml = wr.toString();
        Assert.assertTrue(resultXml.contains("<injected_node>"));
        Assert.assertTrue(resultXml.contains("sample"));
        Assert.assertTrue(resultXml.contains("</injected_node>"));
    }

    @Test(expected = TransformationException.class)
    public void shouldFailForMalformedFile() throws IOException, TransformationException {
        URL xsltFile =  Resources.getResource("inject_node.xslt");
        byte[] fileToTransform = readFile("malformedFile.xml");
        new XsltTransformer(xsltFile.toString(), "sample").transform(fileToTransform);
    }

    private byte[] readFile(String fileName) throws IOException {
        String myXml = IOUtils.toString(getClass().getClassLoader().getResource(fileName), Charsets.UTF_8);
        byte[] bytes = myXml.getBytes("UTF-8");
        InputStream contentStream = new ByteArrayInputStream(bytes);
        return IOUtils.toByteArray(contentStream);
    }
}
