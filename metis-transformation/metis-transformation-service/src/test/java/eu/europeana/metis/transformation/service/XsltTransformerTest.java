package eu.europeana.metis.transformation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

class XsltTransformerTest {

    @Test
    void shouldTransformGivenFile() throws Exception {
        final InputStream xsltFileInputStream = readFileToInputStream("sample_xslt.xslt");
        final String fileToTransform = readFileToString("xmlForTesting.xml");
        final StringWriter stringWriter = new XsltTransformer("sample_xslt.xslt", xsltFileInputStream).transform(
                fileToTransform.getBytes(), null);
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
    void shouldTransformGivenFileWithInjection() throws Exception {
        final InputStream xsltFileInputStream = readFileToInputStream("inject_node.xslt");
        final String fileToTransform = readFileToString("xmlForTestingParamInjection.xml");
        StringWriter stringWriter = new XsltTransformer("inject_node.xslt", xsltFileInputStream, "sample",
                null, null)
                .transform(fileToTransform.getBytes(), null);
        final String resultXml = stringWriter.toString();
        assertTrue(resultXml.contains("<injected_node>"));
        assertTrue(resultXml.contains("sample"));
        assertTrue(resultXml.contains("</injected_node>"));
    }

    @Test
    void shouldFailForMalformedFile() throws Exception {
        final InputStream xsltFileInputStream = readFileToInputStream("inject_node.xslt");
        final String fileToTransform = readFileToString("malformedFile.xml");
        final XsltTransformer xsltTransformer = new XsltTransformer("inject_node.xslt", xsltFileInputStream, "sample", null, null);
        assertThrows(TransformationException.class, () -> xsltTransformer
                .transform(fileToTransform.getBytes(), null));
    }

    @Test
    void reorderEDM() throws Exception {
        final InputStream xsltFileInputStream = readFileToInputStream("edm_sorter.xsl");
        final String fileToTransform = readFileToString("europeana_record_edm_external_unsorted.xml");
        final StringWriter stringWriter = new XsltTransformer("edm_sorter.xsl", xsltFileInputStream)
                .transform(fileToTransform.getBytes(), null);
        final String transformed = stringWriter.toString();
        final String expected = readFileToString("europeana_record_edm_external_sorted.xml");
      System.out.println(transformed);

        Diff diff = DiffBuilder.compare(expected)
                .withTest(transformed)
                .ignoreWhitespace()
                .ignoreComments()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences(), () -> "XMLs differ: " + diff);
    }

    @Test
    void transformToEDMInternal() throws Exception {
        final InputStream xsltFileInputStream = readFileToInputStream("default_transformation.xslt");
        final String fileToTransform = readFileToString("europeana_record_edm_external_sorted.xml");
        final StringWriter stringWriter = new XsltTransformer("default_transformation.xslt", xsltFileInputStream)
                .transform(fileToTransform.getBytes(), null);
        final String transformed = stringWriter.toString();
        final String expected = readFileToString("europeana_record_edm_internal.xml");

        Diff diff = DiffBuilder.compare(expected)
                .withTest(transformed)
                .ignoreWhitespace()
                .ignoreComments()
                .checkForIdentical()
                .build();

        assertFalse(diff.hasDifferences(), () -> "XMLs differ: " + diff);
    }

    public String readFileToString(String file) throws IOException {
        InputStream inputStream = readFileToInputStream(file);
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    private InputStream readFileToInputStream(String file) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(file);
        if (inputStream == null) {
            throw new IOException("Failed reading file " + file);
        }
        return inputStream;
    }
}
