package eu.europeana.metis.mediaprocessing.extraction.oembed;

import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getOEmbedModelFromJson;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getOEmbedModelFromXml;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.hasValidHeightSizeThumbnail;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.hasValidHeightSizeUrl;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.hasValidWidthSizeThumbnail;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.hasValidWidthSizeUrl;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.isValidOEmbedPhotoOrVideo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class OEmbedModelTest {

  @Test
  void getOEmbedModelFromJsonTest() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.json");

    OEmbedModel oEmbedModel = getOEmbedModelFromJson(inputStream.readAllBytes());

    assertNotNull(oEmbedModel);
    assertTrue(isValidOEmbedPhotoOrVideo(oEmbedModel));
  }

  @Test
  void getOEmbedModelFromXmlTest() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.xml");

    OEmbedModel oEmbedModel = getOEmbedModelFromXml(inputStream.readAllBytes());

    assertNotNull(oEmbedModel);
    assertTrue(isValidOEmbedPhotoOrVideo(oEmbedModel));
  }

  @Test
  void checkValidWidthAndHeightDimensions() throws IOException {
    String url = "https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fcdn.pixabay.com%2Fvideo%2F2023%2F10%2F22%2F186070-876973719_small.mp4&maxheight=300&maxwidth=500";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.json");

    OEmbedModel oEmbedModel = getOEmbedModelFromJson(inputStream.readAllBytes());

    assertTrue(hasValidHeightSizeUrl(oEmbedModel, url));
    assertTrue(hasValidWidthSizeUrl(oEmbedModel, url));
    assertTrue(hasValidHeightSizeThumbnail(oEmbedModel, url));
    assertTrue(hasValidWidthSizeThumbnail(oEmbedModel, url));
  }

  @Test
  void checkValidWidthAndHeightDimensions_InvalidUrl() throws IOException {
    String url = "my url test";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/oembed.json");

    OEmbedModel oEmbedModel = getOEmbedModelFromJson(inputStream.readAllBytes());

    assertFalse(hasValidHeightSizeUrl(oEmbedModel, url));
    assertFalse(hasValidWidthSizeUrl(oEmbedModel, url));
    assertFalse(hasValidHeightSizeThumbnail(oEmbedModel, url));
    assertFalse(hasValidWidthSizeThumbnail(oEmbedModel, url));
  }
}
