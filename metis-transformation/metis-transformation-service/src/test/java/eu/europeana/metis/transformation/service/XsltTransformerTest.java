package eu.europeana.metis.transformation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


class XsltTransformerTest {

  @Test
  void shouldTransformGivenFile()
      throws TransformationException, IOException, ParserConfigurationException, SAXException {
    final URL xsltFile = getClass().getClassLoader().getResource("sample_xslt.xslt");
    assertNotNull(xsltFile);
    assertDoesNotThrow(xsltFile::toURI);
    final byte[] fileToTransform = readFile("xmlForTesting.xml");
    final StringWriter stringWriter = new XsltTransformer(xsltFile.toString(), xsltFile.openStream()).transform(
        fileToTransform, null);
    final String transformed = stringWriter.toString();

    final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    final Document doc = dBuilder.parse(new InputSource(new StringReader(transformed)));
    assertEquals("html", doc.getDocumentElement().getNodeName());
    assertEquals(1, doc.getElementsByTagName("h2").getLength());
    assertEquals(1, doc.getElementsByTagName("table").getLength());
    assertEquals(2, doc.getElementsByTagName("th").getLength());
    assertEquals(26 + 1, doc.getElementsByTagName("tr").getLength());
    assertEquals(2 * 26, doc.getElementsByTagName("td").getLength());
  }

  @Test
  void shouldTransformGivenFileWithInjection() throws IOException, TransformationException {
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
  void shouldFailForMalformedFile() throws Exception {
    URL xsltFile = getClass().getClassLoader().getResource("inject_node.xslt");
    byte[] fileToTransform = readFile("malformedFile.xml");
    final XsltTransformer xsltTransformer = new XsltTransformer(xsltFile.toString(), xsltFile.openStream(), "sample", null, null);
    assertThrows(TransformationException.class, () -> xsltTransformer.transform(fileToTransform, null));
  }

  private byte[] readFile(String fileName) throws IOException {
    final String xml = IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(fileName)),
        StandardCharsets.UTF_8);
    final byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    return IOUtils.toByteArray(new ByteArrayInputStream(bytes));
  }
}
