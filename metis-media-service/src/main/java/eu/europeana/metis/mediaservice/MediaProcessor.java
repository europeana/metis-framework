package eu.europeana.metis.mediaservice;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import eu.europeana.metis.mediaservice.WebResource.Orientation;

public class MediaProcessor implements Closeable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessor.class);
	
	private static final int[] THUMB_SIZE = { 200, 400 };
	private static final String[] THUMB_SUFFIX = { "-MEDIUM", "-LARGE" };
	
	private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));
	static final File colormapFile;
	static Tika tika = new Tika();
	static String magickCmd;
	static String ffprobeCmd;
	
	static {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("colormap.png")) {
			colormapFile = File.createTempFile("colormap", ".png");
			colormapFile.deleteOnExit();
			try (FileOutputStream out = new FileOutputStream(colormapFile)) {
				IOUtils.copy(is, out);
			}
		} catch (IOException e) {
			throw new RuntimeException("colormap.png can't be loaded", e);
		}
	}
	
	private ExecutorService commandIOThreadPool = Executors.newFixedThreadPool(2);
	
	private EdmObject edm;
	private Map<String, List<UrlType>> urlTypes;
	private Map<String, String> thumbnails = new HashMap<>();
	
	public MediaProcessor() {
		if (magickCmd == null) {
			synchronized (MediaProcessor.class) {
				if (magickCmd == null)
					staticInit();
			}
		}
	}
	
	/**
	 * @param edm future calls to {@link #processResource(String, String, File)}
	 *        will store extracted metadata in given EDM.
	 */
	public void setEdm(EdmObject edm) {
		this.edm = edm;
		urlTypes = edm.getResourceUrls(Arrays.asList(UrlType.values()));
		thumbnails.clear();
	}
	
	public EdmObject getEdm() {
		return edm;
	}
	
	/**
	 * @param contents downloaded file, can be {@code null} for mime types accepted
	 *        by {@link #supportsLinkProcessing(String)}
	 */
	public void processResource(String url, String providedMimeType, File contents) throws MediaException {
		String mimeType;
		try {
			mimeType = contents != null ? tika.detect(contents) : tika.detect(URI.create(url).toURL());
		} catch (IOException e) {
			throw new MediaException("Mime type checking error", "IOException " + e.getMessage(), e, contents == null);
		}
		
		if (!mimeType.equals(providedMimeType))
			LOGGER.info("Invalid mime type provided (should be {}, was {}): {}", mimeType, providedMimeType, url);
		if (contents == null && !supportsLinkProcessing(mimeType))
			throw new IllegalArgumentException("Contents file is required for mime type " + mimeType);
		
		try {
			if (isImage(mimeType))
				processImage(url, mimeType, contents);
			if (isAudioVideo(mimeType))
				processAudioVideo(url, mimeType, contents);
			if (isText(mimeType))
				processText(url, mimeType, contents);
		} catch (IOException e) {
			throw new MediaException("I/O error during procesing", "IOException " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return thumbnails for all the image resources processed since the last call
	 *         to {@link #setEdm(EdmObject)}. The map's key is the thumbnail file
	 *         absolute path, with the name it should be stored under. The value is
	 *         the resource's original url. Remember to remove the files when they
	 *         are no longer needed.
	 */
	public Map<String, String> getThumbnails() {
		return new HashMap<>(thumbnails);
	}
	
	@Override
	public void close() {
		commandIOThreadPool.shutdown();
	}
	
	private void processImage(String url, String mimeType, File content) throws MediaException, IOException {
		List<File> thumbs = prepareThumbnailFiles(url, mimeType);
		int sizes = THUMB_SIZE.length;
		
		final String FORMAT = "%w\n%h\n%[colorspace]\n";
		final int WIDTH_LINE = 0;
		final int HEIGHT_LINE = 1;
		final int COLORSPACE_LINE = 2;
		final int COLORS_LINE = 3;
		ArrayList<String> command = new ArrayList<>(Arrays.asList(
				magickCmd, content.getPath() + "[0]", "-format", FORMAT, "-write", "info:"));
		for (int i = 0; i < sizes - 1; i++) {
			command.addAll(Arrays.asList(
					"(", "+clone", "-thumbnail", THUMB_SIZE[i] + "x", "-write", thumbs.get(i).getPath(), "+delete",
					")"));
		} // do not +delete the last one, use it to find dominant colors (smaller=quicker)
		command.addAll(Arrays.asList(
				"-thumbnail", THUMB_SIZE[sizes - 1] + "x", "-write", thumbs.get(sizes - 1).getPath()));
		command.addAll(Arrays.asList(
				"-colorspace", "sRGB", "-dither", "Riemersma", "-remap", colormapFile.getPath(),
				"-format", "\n%c", "histogram:info:"));
		
		List<String> results = runCommand(command, false);
		
		int width;
		try {
			width = Integer.parseInt(results.get(WIDTH_LINE));
			int height = Integer.parseInt(results.get(HEIGHT_LINE));
			if (shouldExtractMetadata(urlTypes.get(url))) {
				WebResource resource = edm.getWebResource(url);
				resource.setMimeType(mimeType);
				resource.setFileSize(content.length());
				resource.setWidth(width);
				resource.setHeight(height);
				resource.setOrientation(width > height ? Orientation.LANDSCAPE : Orientation.PORTRAIT);
				resource.setColorspace(results.get(COLORSPACE_LINE));
				resource.setDominantColors(extractDominantColors(results, COLORS_LINE));
			}
		} catch (Exception e) {
			LOGGER.info("Could not parse ImageMagick response:\n" + StringUtils.join(results, "\n"), e);
			throw new MediaException("File seems to be corrupted", "IMAGE ERROR");
		}
		
		for (int i = 0; i < sizes; i++) {
			File thumb = thumbs.get(i);
			if (thumb.length() == 0)
				throw new MediaException("Thumbnail file empty: " + thumb, "THUMBNAIL ERROR");
			if (width < THUMB_SIZE[i]) {
				FileUtils.copyFile(content, thumb);
			}
			thumbnails.put(thumb.getAbsolutePath(), url);
		}
	}
	
	/**
	 * Thumbnails are put directly in temp dir with names that should be used in the
	 * target storage. It's a problem when multiple EDMs share the same resource -
	 * we can't use the same thumbnail files because system won't know when they can
	 * be removed. So duplicate thumbnails are stored in subdirectories with
	 * increasing numbers.
	 */
	private List<File> prepareThumbnailFiles(String url, String mimeType) throws IOException {
		final int MAX_DIRS = 1000;
		List<File> thumbs = new ArrayList<>();
		String md5 = DigestUtils.md5Hex(url);
		String ext = "image/png".equals(mimeType) ? ".png" : ".jpeg";
		for (int i = 0; i < THUMB_SUFFIX.length; i++) {
			File temp = File.createTempFile("thumb", null);
			for (int j = 0; j < MAX_DIRS; j++) {
				File dir = new File(tempDir, "media_thumbnails_" + j);
				if (!dir.isDirectory() && !dir.mkdir())
					throw new IOException("Could not create thumbnails subdirectory: " + dir);
				try {
					File f = new File(dir, md5 + THUMB_SUFFIX[i] + ext);
					Files.move(temp.toPath(), f.toPath());
					thumbs.add(f);
					break;
				} catch (FileAlreadyExistsException e) {
					LOGGER.trace(j + " duplicates of " + url, e);
					// try next dir
				}
			}
			if (thumbs.size() < i + 1) {
				Files.delete(temp.toPath());
				throw new IOException("Too many duplicates of url " + url);
			}
		}
		return thumbs;
	}
	
	private List<String> extractDominantColors(List<String> results, int skipLines) {
		final int MAX_COLORS = 6;
		final Pattern pattern = Pattern.compile("#([0-9A-F]{6})");
		return results.stream()
				.skip(skipLines)
				.sorted(Collections.reverseOrder())
				.limit(MAX_COLORS)
				.map(line -> {
					Matcher m = pattern.matcher(line);
					m.find();
					return m.group(1); // throw exception if not found
				})
				.collect(Collectors.toList());
	}
	
	private void processAudioVideo(String url, String mimeType, File contents) throws MediaException, IOException {
		if (!shouldExtractMetadata(urlTypes.get(url)))
			return;
		
		List<String> command = Arrays.asList(ffprobeCmd, "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", contents != null ? contents.getPath() : url);
		List<String> resultLines = runCommand(command, false);
		
		try {
			JSONObject result = new JSONObject(new JSONTokener(String.join("", resultLines)));
			if (result.length() == 0 && contents == null) {
				throw new MediaException("Probably downlaod failed", "", null, true);
			}
			WebResource resource = edm.getWebResource(url);
			resource.setMimeType(mimeType);
			resource.setFileSize(result.getJSONObject("format").getLong("size"));
			
			JSONObject videoStream = findStream(result, "video");
			JSONObject audioStream = findStream(result, "audio");
			if (videoStream != null) {
				resource.setDuration(videoStream.getDouble("duration"));
				resource.setBitrate(videoStream.getInt("bit_rate"));
				resource.setWidth(videoStream.getInt("width"));
				resource.setHeight(videoStream.getInt("height"));
				resource.setCodecName(videoStream.getString("codec_name"));
				
				String[] frameRateParts = videoStream.getString("avg_frame_rate").split("/");
				double frameRate = Double.parseDouble(frameRateParts[0]) / Double.parseDouble(frameRateParts[1]);
				resource.setFrameRete(frameRate);
			} else if (audioStream != null) {
				resource.setDuration(audioStream.getDouble("duration"));
				resource.setBitrate(audioStream.getInt("bit_rate"));
				resource.setCahhnels(audioStream.getInt("channels"));
				resource.setSampleRate(audioStream.getInt("sample_rate"));
				resource.setSampleSize(audioStream.getInt("bits_per_sample"));
			} else {
				throw new MediaException("No media streams", "AUDIOVIDEO ERROR");
			}
		} catch (MediaException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.info("Could not parse ffprobe response:\n" + StringUtils.join(resultLines, "\n"), e);
			throw new MediaException("File seems to be corrupted", "AUDIOVIDEO ERROR");
		}
	}
	
	private JSONObject findStream(JSONObject data, String codecType) {
		for (Object streamObject : data.getJSONArray("streams")) {
			JSONObject stream = (JSONObject) streamObject;
			if (codecType.equals(stream.getString("codec_type")))
				return stream;
		}
		return null;
	}
	
	private void processText(String url, String mimeType, File contents) throws IOException {
		if (!shouldExtractMetadata(urlTypes.get(url)))
			return;
		
		boolean containsText = mimeType.startsWith("text/");
		Integer resolution = null;
		
		if ("application/pdf".equals(mimeType)) {
			PdfReader reader = new PdfReader(contents.getAbsolutePath());
			try {
				PdfReaderContentParser parser = new PdfReaderContentParser(reader);
				PdfListener pdfListener = new PdfListener();
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					parser.processContent(i, pdfListener);
					resolution = pdfListener.dpi;
					containsText = !StringUtils.isBlank(pdfListener.getResultantText());
					if (resolution != null && containsText)
						break;
				}
			} finally {
				reader.close();
			}
		}
		
		WebResource resource = edm.getWebResource(url);
		resource.setMimeType(mimeType);
		resource.setFileSize(contents.length());
		resource.setContainsText(containsText);
		resource.setResolution(resolution);
	}
	
	List<String> runCommand(List<String> command, boolean mergeError) throws IOException {
		return runCommand(command, mergeError, null);
	}
	
	List<String> runCommand(List<String> command, boolean mergeError, byte[] inputBytes) throws IOException {
		Process process = new ProcessBuilder(command).redirectErrorStream(mergeError).start();
		if (!mergeError) {
			commandIOThreadPool.execute(() -> {
				try (InputStream errorStream = process.getErrorStream()) {
					String output = IOUtils.toString(errorStream, Charset.defaultCharset());
					if (!StringUtils.isBlank(output))
						LOGGER.warn("Command: {}\nerror output:\n{}", command, output);
				} catch (IOException e) {
					LOGGER.error("Error stream reading faild for command " + command, e);
				}
			});
		}
		if (inputBytes != null) {
			commandIOThreadPool.execute(() -> {
				try (OutputStream processInput = process.getOutputStream()) {
					processInput.write(inputBytes);
				} catch (IOException e) {
					LOGGER.error("Pushing data to process input stream failed for command " + command, e);
				}
			});
		}
		try (InputStream in = process.getInputStream()) {
			return IOUtils.readLines(in, Charset.defaultCharset());
		}
	}
	
	/**
	 * This is a non-static method because it needs access to
	 * {@link #runCommand(List, boolean)}.
	 */
	private void staticInit() {
		// find ffprobe
		try {
			List<String> lines = runCommand(Arrays.asList("ffprobe"), true);
			if (!String.join("", lines).startsWith("ffprobe version 2"))
				throw new RuntimeException("ffprobe 2.x not found");
			ffprobeCmd = "ffprobe";
		} catch (IOException e) {
			throw new RuntimeException("Error while looking for ffprobe tools", e);
		}
		
		// find image magick
		try {
			List<String> lines = runCommand(Arrays.asList("magick", "-version"), true);
			if (String.join("", lines).startsWith("Version: ImageMagick 7")) {
				magickCmd = "magick";
			} else { // try convert, but careful about conflict with a windows tool
				boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
				List<String> paths = runCommand(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
				for (String path : paths) {
					lines = runCommand(Arrays.asList(path, "-version"), true);
					if (String.join("", lines).startsWith("Version: ImageMagick 6")) {
						magickCmd = path;
						break;
					}
				}
			}
			if (magickCmd == null)
				throw new RuntimeException("ImageMagick version 6/7 not found");
		} catch (IOException e) {
			throw new RuntimeException("Error while looking for ImageMagick tools", e);
		}
	}
	
	private static boolean shouldExtractMetadata(Collection<UrlType> resourceTypes) {
		return resourceTypes.stream().anyMatch(t -> t == UrlType.HAS_VIEW || t == UrlType.IS_SHOWN_BY);
	}
	
	/**
	 * @return if true, resources of given type don't need to be downloaded before
	 *         processing.
	 */
	public static boolean supportsLinkProcessing(String mimeType) {
		return isAudioVideo(mimeType);
	}
	
	public static boolean isImage(String mimeType) {
		return mimeType.startsWith("image/");
	}
	
	public static boolean isAudioVideo(String mimeType) {
		return mimeType.startsWith("audio/") || mimeType.startsWith("video/");
	}
	
	public static boolean isText(String mimeType) {
		switch (mimeType) {
		case "application/xml":
		case "application/rtf":
		case "application/epub":
		case "application/pdf":
			return true;
		default:
			return mimeType.startsWith("text/");
		}
	}
	
	private static class PdfListener extends SimpleTextExtractionStrategy {
		Integer dpi;
		
		@Override
		public void renderImage(ImageRenderInfo iri) {
			try {
				if (dpi != null)
					return;
				BufferedImage image = iri.getImage().getBufferedImage();
				int wPx = image.getWidth();
				int hPx = image.getHeight();
				
				Matrix m = iri.getImageCTM();
				final int displayDpi = 72;
				double wInch = (double) m.get(Matrix.I11) / displayDpi;
				double hInch = (double) m.get(Matrix.I22) / displayDpi;
				
				long xdpi = Math.abs(Math.round(wPx / wInch));
				long ydpi = Math.abs(Math.round(hPx / hInch));
				dpi = (int) Math.min(xdpi, ydpi);
			} catch (IOException e) {
				LOGGER.info("Could not extract PDF image", e);
			}
		}
	}
}
