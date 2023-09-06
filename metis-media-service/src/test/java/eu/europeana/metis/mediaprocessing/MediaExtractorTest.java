package eu.europeana.metis.mediaprocessing;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.AudioResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MediaExtractorTest {
    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();
    @Test
    void performMediaExtraction() throws MediaProcessorException, MediaExtractionException, IOException {
        // given
        MediaProcessorFactory mediaProcessorFactory = new MediaProcessorFactory();
        MediaExtractor mediaExtractor = mediaProcessorFactory.createMediaExtractor();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("__files/audio_test.mp3")) {
            byte[] audioBytes = inputStream.readAllBytes();
            wireMockExtension.stubFor(get("/imagen_id.do?idImagen=10610909").willReturn(aResponse()
                    .withStatus(200)
                    .withBody(audioBytes)
                    .withHeader("Content-Type", "audio/mpeg")));
        }
        final String url = String.format("http://localhost:%d/imagen_id.do?idImagen=10610909", wireMockExtension.getPort());

        // when
        ResourceExtractionResult resourceExtractionResult =
                mediaExtractor.performMediaExtraction(
                        new RdfResourceEntry(url,
                                List.of(UrlType.OBJECT)), false);
        // then
        assertNotNull(resourceExtractionResult.getMetadata());
    }
}
