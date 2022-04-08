package eu.europeana.metis.dereference;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.exception.BadContentException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IncomingRecordToEdmTransformerTest {

  private static final String copyXmlXsltFileName = "copy_xml.xslt";
  private static final String produceEmptyXsltFileName = "produce_empty.xslt";
  private static final String produceInvalidXmlXsltFileName = "produce_invalid_xml.xslt";
  private static final String ysoP105069FileName = "yso_p105069.xml";
  private static final String invalidXmlFileName = "invalid_xml.xml";

  private static String copyXmlXsltString;
  private static String produceEmptyXsltString;
  private static String produceInvalidXmlXsltString;
  private static String ysoP105069String;
  private static String invalidXmlString;

  @BeforeAll
  static void setUp() throws Exception {
    ClassLoader classLoader = IncomingRecordToEdmTransformerTest.class.getClassLoader();
    Path path = Paths.get(Objects.requireNonNull(classLoader.getResource(copyXmlXsltFileName)).toURI());
    copyXmlXsltString = Files.readString(path, StandardCharsets.UTF_8);

    path = Paths.get(Objects.requireNonNull(classLoader.getResource(produceEmptyXsltFileName)).toURI());
    produceEmptyXsltString = Files.readString(path, StandardCharsets.UTF_8);

    path = Paths.get(Objects.requireNonNull(classLoader.getResource(produceInvalidXmlXsltFileName)).toURI());
    produceInvalidXmlXsltString = Files.readString(path, StandardCharsets.UTF_8);

    path = Paths.get(Objects.requireNonNull(classLoader.getResource(ysoP105069FileName)).toURI());
    ysoP105069String = Files.readString(path, StandardCharsets.UTF_8);

    path = Paths.get(Objects.requireNonNull(classLoader.getResource(invalidXmlFileName)).toURI());
    invalidXmlString = Files.readString(path, StandardCharsets.UTF_8);
  }

  @Test
  void transform() throws Exception {
    IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(copyXmlXsltString);
    final Optional<String> transformedOptional = incomingRecordToEdmTransformer.transform(ysoP105069String,
        "http://www.yso.fi/onto/yso/p105069");
    assertTrue(transformedOptional.isPresent());
  }

  @Test
  void transform_EmptyXslt() throws Exception {
    IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(produceEmptyXsltString);
    final Optional<String> transformedOptional = incomingRecordToEdmTransformer.transform(ysoP105069String,
        "http://www.yso.fi/onto/yso/p105069");
    assertTrue(transformedOptional.isEmpty());
  }

  @Test
  void transform_InvalidSourceXml_BadContentException() throws Exception {
    IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(copyXmlXsltString);
    assertThrows(BadContentException.class, () -> incomingRecordToEdmTransformer.transform(invalidXmlString,
        "http://www.yso.fi/onto/yso/p105069"));
  }

  @Test
  void transform_InvalidXml_BadContentException() throws Exception {
    IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(
        produceInvalidXmlXsltString);
    assertThrows(BadContentException.class, () -> incomingRecordToEdmTransformer.transform(ysoP105069String,
        "http://www.yso.fi/onto/yso/p105069"));
  }
}

