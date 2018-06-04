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
        XsltTransformer t = new XsltTransformer();
        StringWriter wr = t.transform(xsltFile.toString(),fileToTransform);
        String resultXml = wr.toString();
        //
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource( new StringReader( wr.toString() ) ));
        Assert.assertTrue(doc.getDocumentElement().getNodeName().equals("html"));
        Assert.assertTrue(doc.getElementsByTagName("h2").getLength() == 1);
        Assert.assertTrue(doc.getElementsByTagName("table").getLength() == 1);
        Assert.assertTrue(doc.getElementsByTagName("th").getLength() == 2);
        Assert.assertTrue(doc.getElementsByTagName("tr").getLength() == 26 + 1);
        Assert.assertTrue(doc.getElementsByTagName("td").getLength() == 2 * 26);

    }

    @Test
    public void shouldTransformGivenFileWithInjection() throws IOException, TransformationException {
        URL xsltFile =  Resources.getResource("inject_node.xslt");
        byte[] fileToTransform = readFile("xmlForTestingParamInjection.xml");
        XsltTransformer t = new XsltTransformer();
        StringWriter wr = t.transform(xsltFile.toString(), fileToTransform, "sample");
        String resultXml = wr.toString();
        Assert.assertTrue(resultXml.contains("<injected_node>"));
        Assert.assertTrue(resultXml.contains("sample"));
        Assert.assertTrue(resultXml.contains("</injected_node>"));
    }

    @Test(expected = TransformationException.class)
    public void shouldFailForMalformedFile() throws IOException, TransformationException {
        URL xsltFile =  Resources.getResource("inject_node.xslt");
        byte[] fileToTransform = readFile("malformedFile.xml");
        XsltTransformer t = new XsltTransformer();
        StringWriter wr = t.transform(xsltFile.toString(), fileToTransform, "sample");
    }

    private byte[] readFile(String fileName) throws IOException {
        String myXml = IOUtils.toString(getClass().getResource(fileName),
                Charsets.UTF_8);
        byte[] bytes = myXml.getBytes("UTF-8");
        InputStream contentStream = new ByteArrayInputStream(bytes);
        return IOUtils.toByteArray(contentStream);

    }
}
