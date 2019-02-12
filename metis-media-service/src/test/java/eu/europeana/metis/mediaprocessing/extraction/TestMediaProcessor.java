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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.RdfSerializer;
import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.UrlType;

public class TestMediaProcessor {
  
    private static final String THUMBNAIL_COMMAND = "thumbnail command";
    private static final String AUDIO_VIDEO_COMMAND = "audio/video command";
    private static final String COLOR_MAP_FILE = "color map file";
	
	private static File tempDir = new File(System.getProperty("java.io.tmpdir"));
	private static CommandExecutor commandExecutor;
    private static ResourceDownloadClient resourceDownloadClient; 
    private static Tika tika;
	private static MediaExtractorImpl testedExtractor;

	private static RdfDeserializer deserializer;
	private static RdfSerializer serializer;

	@BeforeAll
	public static void setUp() throws MediaProcessorException, RdfConverterException {
		deserializer = new RdfConverterFactory().createRdfDeserializer();
		serializer = new RdfConverterFactory().createRdfSerializer();
		commandExecutor = mock(CommandExecutor.class);
		resourceDownloadClient = mock(ResourceDownloadClient.class);
        tika = mock(Tika.class);
        final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(commandExecutor, THUMBNAIL_COMMAND, COLOR_MAP_FILE);
        final AudioVideoProcessor audioVideoProcessor = new AudioVideoProcessor(commandExecutor, AUDIO_VIDEO_COMMAND);
        testedExtractor = new MediaExtractorImpl(resourceDownloadClient, commandExecutor, tika, thumbnailGenerator, audioVideoProcessor);
	}
	
	@AfterEach
	public void resetMocks() {
	  reset(tika, commandExecutor, resourceDownloadClient);
	}
	
	@AfterAll
	public static void cleanUp() throws IOException {
		testedExtractor.close();
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

  private static Resource createResourceForExtraction(String url, String providedMimeType,
      File contents) throws IOException, URISyntaxException {
    final Resource resource = spy(Resource.class);
    when(resource.getResourceUrl()).thenReturn(url);
    when(resource.getUrlTypes()).thenReturn(Collections.singleton(UrlType.IS_SHOWN_BY));
    when(resource.getMimeType()).thenReturn(providedMimeType);
    when(resource.getContentPath()).thenReturn(contents == null ? null : contents.toPath());
    when(resource.hasContent()).thenReturn(contents != null);
    when(resource.getContentSize()).thenReturn(contents == null ? 0 : contents.length());
    when(resource.getActualLocation()).thenReturn(new URI(url));
    return resource;
  }
  
  private static RdfResourceEntry getEntryForResource(Resource resource) {
    return new RdfResourceEntry(resource.getResourceUrl(), new ArrayList<>(resource.getUrlTypes()));
  }

	@Test
	public void processImage()
			throws IOException, MediaExtractionException, RdfDeserializationException, RdfSerializationException, CommandExecutionException, URISyntaxException {
		String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
		String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
		File file = new File(tempDir, "media8313043870723212585.tmp");
		try(RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
		    raf.setLength(83943L);
		}
		
		File[] thumbs = new File[2];
		
		doAnswer(i -> {
			List<String> command = i.getArgument(0);
			thumbs[0] = new File(command.get(11));
			thumbs[1] = new File(command.get(17));
			assertEquals(Arrays.asList(THUMBNAIL_COMMAND, file.getPath() + "[0]",
					"-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
					"(", "+clone", "-thumbnail", "400x", "-write", thumbs[0].getPath(), "+delete", ")",
					"-thumbnail", "200x", "-write", thumbs[1].getPath(),
					"-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
					"-format", "\n%c", "histogram:info:"), command);
			FileUtils.writeByteArrayToFile(thumbs[0], new byte[] { 0 });
			FileUtils.writeByteArrayToFile(thumbs[1], new byte[] { 0 });
			return lines("image1-magick-output1.txt");
		}).when(commandExecutor).execute(any(), eq(false));
		
		doReturn("image/jpeg").when(tika).detect(any(Path.class));

		final ResourceExtractionResult result;
		try {
		    final Resource resource = createResourceForExtraction(url, "image/jpeg", file);
		    doReturn(resource).when(resourceDownloadClient).download(any());
		    result = testedExtractor.performMediaExtraction(getEntryForResource(resource));
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
		assertEquals(md5 + "-LARGE", thumbnails.get(0).getTargetName());
		assertEquals(md5 + "-MEDIUM", thumbnails.get(1).getTargetName());
	}

	@Test
  public void processAudio()
      throws IOException, RdfDeserializationException, MediaExtractionException, RdfSerializationException, CommandExecutionException, URISyntaxException {
		String url = "http://cressound.grenoble.archi.fr/son/rap076/bogota_30_tercer_milenio_parade.mp3";
		
		List<String> command = Arrays.asList(AUDIO_VIDEO_COMMAND, "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		when(commandExecutor.execute(command, false)).thenReturn(lines("audio1-ffprobe-output1.txt"));
		
		when(tika.detect(any(URL.class))).thenReturn("audio/mpeg");

	final Resource resource = createResourceForExtraction(url, "audio/mpeg", null);
	doReturn(resource).when(resourceDownloadClient).download(any());
    final ResourceExtractionResult result = testedExtractor.performMediaExtraction(getEntryForResource(resource));

    final EnrichedRdf rdf = rdf("audio1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("audio1-output.xml"), resultFile);
	}
	
	@Test
	public void processVideo()
      throws IOException, MediaExtractionException, RdfDeserializationException, RdfSerializationException, CommandExecutionException, URISyntaxException {
		String url = "http://maccinema.com/info/filmovi/dae.mp4";
		
		List<String> command = Arrays.asList(AUDIO_VIDEO_COMMAND, "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		when(commandExecutor.execute(command, false)).thenReturn(lines("video1-ffprobe-outptu1.txt"));
		
		when(tika.detect(any(URL.class))).thenReturn("video/mp4");

        final Resource resource = createResourceForExtraction(url, "video/mp4", null);
        doReturn(resource).when(resourceDownloadClient).download(any());
        final ResourceExtractionResult result = testedExtractor.performMediaExtraction(getEntryForResource(resource));

    final EnrichedRdf rdf = rdf("video1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("video1-output.xml"), resultFile);
	}

	@Test
	public void processPdf()
      throws IOException, URISyntaxException, MediaExtractionException, RdfSerializationException, RdfDeserializationException, CommandExecutionException {
		File contents = new File(getClass().getClassLoader().getResource("pdf1.pdf").toURI());

        File[] thumbs = new File[2];
        
	    doAnswer(i -> {
            List<String> command = i.getArgument(0);
            thumbs[0] = new File(command.get(15));
            thumbs[1] = new File(command.get(21));
            assertEquals(Arrays.asList(THUMBNAIL_COMMAND, contents.getPath() + "[0]",
                    "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
                    "-background", "white", "-alpha", "remove",
                    "(", "+clone", "-thumbnail", "400x", "-write", thumbs[0].getPath(), "+delete", ")",
                    "-thumbnail", "200x", "-write", thumbs[1].getPath(),
                    "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
                    "-format", "\n%c", "histogram:info:"), command);
            FileUtils.writeByteArrayToFile(thumbs[0], new byte[] { 0 });
            FileUtils.writeByteArrayToFile(thumbs[1], new byte[] { 0 });
            return lines("pdf1-magick-output1.txt");
        }).when(commandExecutor).execute(any(), eq(false));

		when(tika.detect(any(Path.class))).thenReturn("application/pdf");

        final Resource resource = createResourceForExtraction("http://sample.edu.eu/data/sample1.pdf", "application/pdf", contents);
        doReturn(resource).when(resourceDownloadClient).download(any());
        final ResourceExtractionResult result = testedExtractor.performMediaExtraction(getEntryForResource(resource));

    final EnrichedRdf rdf = rdf("pdf1-input.xml");
    rdf.enrichResource(result.getMetadata());
    final String resultFile = new String(serializer.serialize(rdf), "UTF-8");

		assertEquals(string("pdf1-output.xml"), resultFile);
	}
}
