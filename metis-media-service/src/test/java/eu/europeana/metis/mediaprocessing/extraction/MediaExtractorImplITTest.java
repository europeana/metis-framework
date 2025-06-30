package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.MediaProcessorFactory;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MediaExtractorImplITTest {

  private RdfDeserializer rdfDeserializer;
  private MediaExtractor mediaExtractor;

  @Test
  void testIIFExtraction() throws Exception {

    rdfDeserializer = new RdfConverterFactory().createRdfDeserializer();
    mediaExtractor = new MediaProcessorFactory().createMediaExtractor();
    InputStream inputRdf = getClass().getClassLoader().getResourceAsStream("__files/rdf_with_iiif_sample.xml");
    List<RdfResourceEntry> resourceEntryList = rdfDeserializer.getRemainingResourcesForMediaExtraction(inputRdf);
    for (RdfResourceEntry resourceEntry : resourceEntryList) {

      ResourceExtractionResult extractionResult = mediaExtractor.performMediaExtraction(resourceEntry, true);
      assertNotNull(extractionResult.getMetadata());
      //assertNotNull(extractionResult.getThumbnails());
    }
  }

}
