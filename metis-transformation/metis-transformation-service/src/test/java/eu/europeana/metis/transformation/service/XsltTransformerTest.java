package eu.europeana.metis.transformation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XsltTransformerTest {

  @Test
  public void shouldTransformGivenFile()
      throws TransformationException, IOException, ParserConfigurationException, SAXException {
    URL xsltFile = getClass().getClassLoader().getResource("sample_xslt.xslt");
    byte[] fileToTransform = readFile("xmlForTesting.xml");
    StringWriter wr = new XsltTransformer(xsltFile.toString(), xsltFile.openStream()).transform(
        fileToTransform, null);

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(new InputSource(new StringReader(wr.toString())));
    assertEquals("html", doc.getDocumentElement().getNodeName());
    assertEquals(1, doc.getElementsByTagName("h2").getLength());
    assertEquals(1, doc.getElementsByTagName("table").getLength());
    assertEquals(2, doc.getElementsByTagName("th").getLength());
    assertEquals(26 + 1, doc.getElementsByTagName("tr").getLength());
    assertEquals(2 * 26, doc.getElementsByTagName("td").getLength());
  }

  @Test
  public void shouldTransformGivenFileWithInjection() throws IOException, TransformationException {
    URL xsltFile = getClass().getClassLoader().getResource("inject_node.xslt");
    byte[] fileToTransform = readFile("xmlForTestingParamInjection.xml");
    StringWriter wr = new XsltTransformer(xsltFile.toString(), xsltFile.openStream(), "sample",
        null, null)
        .transform(fileToTransform, null);
    String resultXml = wr.toString();
    assertTrue(resultXml.contains("<injected_node>"));
    assertTrue(resultXml.contains("sample"));
    assertTrue(resultXml.contains("</injected_node>"));
  }

  @Test
  public void shouldFailForMalformedFile() throws IOException {
    URL xsltFile = getClass().getClassLoader().getResource("inject_node.xslt");
    byte[] fileToTransform = readFile("malformedFile.xml");
    assertThrows(TransformationException.class,
        () -> new XsltTransformer(xsltFile.toString(), xsltFile.openStream(), "sample", null, null)
            .transform(fileToTransform, null));
  }

  private byte[] readFile(String fileName) throws IOException {
    String myXml = IOUtils
        .toString(getClass().getClassLoader().getResource(fileName), StandardCharsets.UTF_8.name());
    byte[] bytes = myXml.getBytes(StandardCharsets.UTF_8);
    InputStream contentStream = new ByteArrayInputStream(bytes);
    return IOUtils.toByteArray(contentStream);
  }
}
