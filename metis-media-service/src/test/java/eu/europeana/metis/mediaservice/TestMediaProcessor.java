package eu.europeana.metis.mediaservice;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestMediaProcessor {
	
	static File tempDir = new File(System.getProperty("java.io.tmpdir"));
	static MediaProcessor originalProcessor;
	
	EdmObject.Parser parser = new EdmObject.Parser();
	EdmObject.Writer writer = new EdmObject.Writer();
	
	@BeforeClass
	public static void setUp() {
		MediaProcessor.ffprobeCmd = "ffprobe";
		MediaProcessor.magickCmd = "magick";
		MediaProcessor.tika = spy(MediaProcessor.tika);
		originalProcessor = new MediaProcessor();
	}
	
	@AfterClass
	public static void cleanUp() {
		originalProcessor.close();
	}
	
	private List<String> lines(String resource) throws IOException {
		return IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(resource), "UTF-8");
	}
	
	private String string(String resource) throws IOException {
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource), "UTF-8");
	}
	
	private EdmObject edm(String resource) throws MediaException {
		return parser.parseXml(getClass().getClassLoader().getResourceAsStream(resource));
	}
	
	@Test
	public void processImage() throws IOException, MediaException {
		String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
		String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
		File file = spy(new File(tempDir, "media8313043870723212585.tmp"));
		when(file.length()).thenReturn(83943L);
		
		File thumb1 = new File(tempDir, "media_thumbnails_0/" + md5 + "-MEDIUM.jpeg");
		File thumb2 = new File(tempDir, "media_thumbnails_0/" + md5 + "-LARGE.jpeg");
		
		MediaProcessor processor = spy(originalProcessor);
		List<String> command1 = Arrays.asList("magick", file.getPath() + "[0]",
				"-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
				"(", "+clone", "-thumbnail", "200x", "-write", thumb1.getPath(), "+delete", ")",
				"-thumbnail", "400x", "-write", thumb2.getPath(),
				"-colorspace", "sRGB", "-dither", "Riemersma", "-remap", MediaProcessor.colormapFile.getPath(),
				"-format", "\n%c", "histogram:info:");
		doAnswer(i -> {
			FileUtils.writeByteArrayToFile(thumb1, new byte[] { 0 });
			FileUtils.writeByteArrayToFile(thumb2, new byte[] { 0 });
			return lines("image1-magick-output1.txt");
		}).when(processor).runCommand(command1, false);
		
		doReturn("image/jpeg").when(MediaProcessor.tika).detect(file);
		
		EdmObject edm;
		try {
			edm = edm("image1-input.xml");
			processor.setEdm(edm);
			processor.processResource(url, "image/jpeg", file);
		} finally {
			assertTrue(thumb1.delete());
			assertTrue(thumb2.delete());
		}
		
		assertEquals(string("image1-output.xml"), new String(writer.toXmlBytes(edm), "UTF-8"));
		
		Map<String, String> thumbs = processor.getThumbnails();
		assertEquals(2, thumbs.size());
		assertEquals(url, thumbs.get(thumb1.getAbsolutePath()));
		assertEquals(url, thumbs.get(thumb2.getAbsolutePath()));
	}
	
	@Test
	public void processAudio() throws IOException, MediaException {
		String url = "http://cressound.grenoble.archi.fr/son/rap076/bogota_30_tercer_milenio_parade.mp3";
		
		MediaProcessor processor = spy(originalProcessor);
		List<String> command = Arrays.asList("ffprobe", "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		doReturn(lines("audio1-ffprobe-output1.txt")).when(processor).runCommand(command, false);
		
		doReturn("audio/mpeg").when(MediaProcessor.tika).detect(any(URL.class));
		
		EdmObject edm = edm("audio1-input.xml");
		processor.setEdm(edm);
		processor.processResource(url, "audio/mpeg", null);
		
		assertEquals(string("audio1-output.xml"), new String(writer.toXmlBytes(edm), "UTF-8"));
	}
	
	@Test
	public void processVideo() throws IOException, MediaException {
		String url = "http://maccinema.com/info/filmovi/dae.mp4";
		
		MediaProcessor processor = spy(originalProcessor);
		List<String> command = Arrays.asList("ffprobe", "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", url);
		doReturn(lines("video1-ffprobe-outptu1.txt")).when(processor).runCommand(command, false);
		
		doReturn("video/mp4").when(MediaProcessor.tika).detect(any(URL.class));
		
		EdmObject edm = edm("video1-input.xml");
		processor.setEdm(edm);
		processor.processResource(url, "audio/mpeg", null);
		
		assertEquals(string("video1-output.xml"), new String(writer.toXmlBytes(edm), "UTF-8"));
	}
	
	@Test
	public void processPdf() throws MediaException, IOException, URISyntaxException {
		MediaProcessor processor = spy(originalProcessor);
		
		EdmObject edm = edm("pdf1-input.xml");
		processor.setEdm(edm);
		processor.processResource("http://sample.edu.eu/data/sample1.pdf", "application/pdf",
				new File(getClass().getClassLoader().getResource("pdf1.pdf").toURI()));
		
		assertEquals(string("pdf1-output.xml"), new String(writer.toXmlBytes(edm), "UTF-8"));
	}
}
