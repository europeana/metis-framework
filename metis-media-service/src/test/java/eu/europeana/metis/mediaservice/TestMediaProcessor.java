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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestMediaProcessor {
	
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
	
	private byte[] bytes(String resource) throws IOException {
		return IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(resource));
	}
	
	private EdmObject edm(String resource) throws MediaException {
		return parser.parseXml(getClass().getClassLoader().getResourceAsStream(resource));
	}
	
	@Test
	public void processImage() throws IOException, MediaException {
		String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
		String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
		File file = spy(new File("/tmp/media8313043870723212585.tmp"));
		when(file.length()).thenReturn(83943L);
		
		File thumb1 = spy(new File("/tmp/media_thumbnails_0/" + md5 + "-MEDIUM.jpeg"));
		File thumb2 = spy(new File("/tmp/media_thumbnails_0/" + md5 + "-LARGE.jpeg"));
		
		MediaProcessor processor = spy(originalProcessor);
		List<String> command1 = Arrays.asList("magick", file.getPath() + "[0]",
				"-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
				"(", "+clone", "-thumbnail", "200x", "-write", thumb1.getPath(), "+delete", ")",
				"-thumbnail", "400x", "-write", thumb2.getPath(),
				"-colorspace", "sRGB", "-dither", "Riemersma", "-remap", MediaProcessor.colormapFile.getPath(),
				"-format", "\n%c", "histogram:info:");
		when(processor.runCommand(command1, false)).thenAnswer(i -> {
			FileUtils.writeByteArrayToFile(thumb1, new byte[] { 0 });
			FileUtils.writeByteArrayToFile(thumb2, new byte[] { 0 });
			return lines("image1-magick-output1.txt");
		});
		
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
		
		assertArrayEquals(bytes("image1-output.xml"), writer.toXmlBytes(edm));
		
		Map<String, String> thumbs = processor.getThumbnails();
		assertEquals(2, thumbs.size());
		assertEquals(url, thumbs.get(thumb1.getAbsolutePath()));
		assertEquals(url, thumbs.get(thumb2.getAbsolutePath()));
	}
}
