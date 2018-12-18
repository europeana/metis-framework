package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.RdfSerializer;
import eu.europeana.metis.mediaprocessing.extraction.AudioVideoProcessor;
import eu.europeana.metis.mediaprocessing.extraction.CommandExecutor;
import eu.europeana.metis.mediaprocessing.extraction.MediaProcessor;
import eu.europeana.metis.mediaprocessing.extraction.ThumbnailGenerator;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestMediaProcessor {
	
	private static File tempDir = new File(System.getProperty("java.io.tmpdir"));
	private static Tika tika;
	private static CommandExecutor commandExecutor;
	private static MediaProcessor testedProcessor;

	private static RdfDeserializer deserializer;
	private static RdfSerializer serializer;

	@BeforeAll
	public static void setUp() throws MediaProcessorException, RdfConverterException {
		deserializer = new RdfConverterFactory().createRdfDeserializer();
		serializer = new RdfConverterFactory().createRdfSerializer();
		AudioVideoProcessor.setCommand("ffprobe");
		ThumbnailGenerator.setCommand("magick");
		tika = mock(Tika.class);
		MediaProcessor.setTika(tika);
		commandExecutor = mock(CommandExecutor.class);
		testedProcessor = new MediaProcessor(commandExecutor);
	}
	
	@AfterEach
	public void resetMocks() {
	  reset(tika, commandExecutor);
	}
	
	@AfterAll
	public static void cleanUp() {
		testedProcessor.close();
	}
	
	private List<String> lines(String resource) throws IOException {
		return IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(resource), "UTF-8");
	}
	
	private String string(String resource) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource), "UTF-8");
	}
	
	private EnrichedRdf rdf(String resource) throws RdfDeserializationException {
		return deserializer.getRdfForResourceEnriching(getClass().getClassLoader().getResourceAsStream(resource));
	}

	@Test
	public void processImage()
      throws IOException, MediaExtractionException, RdfDeserializationException, RdfSerializationException {
		String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
		String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
		File file = spy(new File(tempDir, "media8313043870723212585.tmp"));
		when(file.length()).thenReturn(83943L);
		
		File[] thumbs = new File[2];
		
		doAnswer(i -> {
			List<String> command = i.getArgument(0);
			thumbs[0] = new File(command.get(11));
			thumbs[1] = new File(command.get(17));
			assertEquals(Arrays.asList("magick", file.getPath() + "[0]",
					"-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
					"(", "+clone", "-thumbnail", "200x", "-write", thumbs[0].getPath(), "+delete", ")",
					"-thumbnail", "400x", "-write", thumbs[1].getPath(),
					"-colorspace", "sRGB", "-dither", "Riemersma", "-remap",
					ThumbnailGenerator.getColormapFile().toString(),
					"-format", "\n%c", "histogram:info:"), command);
			FileUtils.writeByteArrayToFile(thumbs[0], new byte[] { 0 });
			FileUtils.writeByteArrayToFile(thumbs[1], new byte[] { 0 });
			return lines("image1-magick-output1.txt");
		}).when(commandExecutor).runCommand(any(), eq(false));
		
		doReturn("image/jpeg").when(tika).detect(file);

		final ResourceExtractionResult result;
		try {
			result = testedProcessor.processResource(url, Collections.singleton(UrlType.IS_SHOWN_BY), "image/jpeg", file);
		} finally {
			assertTrue(thumbs[0].delete());
			assertTrue(thumbs[1].delete());
		}

    final EnrichedRdf rdf = rdf("image1-input.xml");
		rdf.enrichResource(result.getMetadata());
		final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("image1-output.xml"), resultFile);
		
		List<Thumbnail> thumbnails = result.getThumbnails();
		assertEquals(2, thumbnails.size());
		assertEquals(url, thumbnails.get(0).getResourceUrl());
		assertEquals(url, thumbnails.get(1).getResourceUrl());
		assertEquals(md5 + "-MEDIUM", thumbnails.get(0).getTargetName());
		assertEquals(md5 + "-LARGE", thumbnails.get(1).getTargetName());
	}

	@Test
  public void processAudio()
      throws IOException, RdfDeserializationException, MediaExtractionException, RdfSerializationException {
		String url = "http://cressound.grenoble.archi.fr/son/rap076/bogota_30_tercer_milenio_parade.mp3";
		
		List<String> command = Arrays.asList("ffprobe", "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		when(commandExecutor.runCommand(command, false)).thenReturn(lines("audio1-ffprobe-output1.txt"));
		
		when(tika.detect(any(URL.class))).thenReturn("audio/mpeg");

    final ResourceExtractionResult result = testedProcessor
        .processResource(url, Collections.singleton(UrlType.IS_SHOWN_BY), "audio/mpeg", null);

    final EnrichedRdf rdf = rdf("audio1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("audio1-output.xml"), resultFile);
	}

	@Test
	public void processVideo()
      throws IOException, MediaExtractionException, RdfDeserializationException, RdfSerializationException {
		String url = "http://maccinema.com/info/filmovi/dae.mp4";
		
		List<String> command = Arrays.asList("ffprobe", "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		when(commandExecutor.runCommand(command, false)).thenReturn(lines("video1-ffprobe-outptu1.txt"));
		
		when(tika.detect(any(URL.class))).thenReturn("video/mp4");

    final ResourceExtractionResult result = testedProcessor
        .processResource(url, Collections.singleton(UrlType.IS_SHOWN_BY), "audio/mpeg", null);

    final EnrichedRdf rdf = rdf("video1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("video1-output.xml"), resultFile);
	}

	@Test
	public void processPdf()
      throws IOException, URISyntaxException, MediaExtractionException, RdfSerializationException, RdfDeserializationException {
		File contents = new File(getClass().getClassLoader().getResource("pdf1.pdf").toURI());

        File[] thumbs = new File[2];
        
	    doAnswer(i -> {
            List<String> command = i.getArgument(0);
            thumbs[0] = new File(command.get(15));
            thumbs[1] = new File(command.get(21));
            assertEquals(Arrays.asList("magick", contents.getPath() + "[0]",
                    "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
                    "-background", "white", "-alpha", "remove",
                    "(", "+clone", "-thumbnail", "200x", "-write", thumbs[0].getPath(), "+delete", ")",
                    "-thumbnail", "400x", "-write", thumbs[1].getPath(),
                    "-colorspace", "sRGB", "-dither", "Riemersma", "-remap",
										ThumbnailGenerator.getColormapFile().toString(),
                    "-format", "\n%c", "histogram:info:"), command);
            FileUtils.writeByteArrayToFile(thumbs[0], new byte[] { 0 });
            FileUtils.writeByteArrayToFile(thumbs[1], new byte[] { 0 });
            return lines("pdf1-magick-output1.txt");
        }).when(commandExecutor).runCommand(any(), eq(false));

		when(tika.detect(contents)).thenReturn("application/pdf");

    final ResourceExtractionResult result = testedProcessor
        .processResource("http://sample.edu.eu/data/sample1.pdf",
            Collections.singleton(UrlType.IS_SHOWN_BY), "application/pdf", contents);

    final EnrichedRdf rdf = rdf("pdf1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("pdf1-output.xml"), resultFile);
	}
}
