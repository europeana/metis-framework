package eu.europeana.metis.transformation.service;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Created by pwozniak on 3/21/18
 */
public class XsltTransformerTest {

    @Test
    public void shouldTransformGivenFile()
        throws TransformationException, IOException, ParserConfigurationException, SAXException {
        URL xsltFile =  Resources.getResource("sample_xslt.xslt");
        byte[] fileToTransform = readFile("xmlForTesting.xml");
        StringWriter wr = new XsltTransformer(xsltFile.toString()).transform(fileToTransform, null);

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
        StringWriter wr = new XsltTransformer(xsltFile.toString(), "sample", null, null).transform(fileToTransform, null);
        String resultXml = wr.toString();
        Assert.assertTrue(resultXml.contains("<injected_node>"));
        Assert.assertTrue(resultXml.contains("sample"));
        Assert.assertTrue(resultXml.contains("</injected_node>"));
    }

    @Test(expected = TransformationException.class)
    public void shouldFailForMalformedFile() throws IOException, TransformationException {
        URL xsltFile =  Resources.getResource("inject_node.xslt");
        byte[] fileToTransform = readFile("malformedFile.xml");
        new XsltTransformer(xsltFile.toString(), "sample", null, null).transform(fileToTransform, null);
    }

    private byte[] readFile(String fileName) throws IOException {
        String myXml = IOUtils.toString(getClass().getClassLoader().getResource(fileName), Charsets.UTF_8);
        byte[] bytes = myXml.getBytes("UTF-8");
        InputStream contentStream = new ByteArrayInputStream(bytes);
        return IOUtils.toByteArray(contentStream);
    }
}
