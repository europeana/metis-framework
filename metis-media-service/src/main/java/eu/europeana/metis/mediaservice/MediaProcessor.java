package eu.europeana.metis.mediaservice;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaProcessor implements Closeable {
	
	private static final Logger log = LoggerFactory.getLogger(MediaProcessor.class);
	
	private static final int[] THUMB_SIZE = { 200, 400 };
	private static final String[] THUMB_SUFFIX = { "-MEDIUM", "-LARGE" };
	
	private static File tempDir;
	private static File colormapFile;
	private static String magickCmd;
	private static String ffprobeCmd;
	
	private ExecutorService commandIOThreadPool = Executors.newFixedThreadPool(2);
	
	private EdmObject edm;
	private Map<String, List<UrlType>> urlTypes;
	private Map<File, String> thumbnails = new HashMap<>();
	
	public MediaProcessor() {
		staticInit();
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
	public void processResource(String url, String mimeType, File contents) throws MediaException {
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
	 *         to {@link #setEdm(EdmObject)}. The map's key is the thumbnail file,
	 *         with the name it should be stored under. The value is the resource's
	 *         original url. Remember to remove the files when they are no longer
	 *         needed.
	 */
	public Map<File, String> getThumbnails() {
		return new HashMap<>(thumbnails);
	}
	
	@Override
	public void close() {
		commandIOThreadPool.shutdown();
	}
	
	private void processImage(String url, String mimeType, File content) throws MediaException, IOException {
		List<File> thumbs = new ArrayList<>();
		int sizes = THUMB_SIZE.length;
		String md5 = DigestUtils.md5Hex(url);
		String ext = Arrays.asList("application/pdf", "image/png").contains(mimeType) ? ".png" : ".jpeg";
		for (int i = 0; i < sizes; i++) {
			File f = new File(tempDir, md5 + THUMB_SUFFIX[i] + ext);
			if (!f.isFile() && !f.createNewFile())
				throw new MediaException("Could not create thumbnail file " + f, "THUMBNAIL ERROR");
			thumbs.add(f);
		}
		
		ArrayList<String> command = new ArrayList<>(Arrays.asList(
				magickCmd, content.getPath() + "[0]", "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:"));
		if ("application/pdf".equals(mimeType))
			command.addAll(Arrays.asList("-background", "white", "-alpha", "remove"));
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
			width = Integer.parseInt(results.get(0));
			int height = Integer.parseInt(results.get(1));
			if (shouldExtractMetadata(urlTypes.get(url))) {
				WebResource resource = edm.getWebResource(url);
				resource.setMimeType(mimeType);
				resource.setFileSize(content.length());
				resource.setWidth(width);
				resource.setHeight(height);
				resource.setOrientation(width > height);
				resource.setColorspace(results.get(2));
				resource.setDominantColors(extractDominantColors(results));
			}
		} catch (Exception e) {
			log.info("Could not parse ImageMagick response:\n" + StringUtils.join(results, "\n"), e);
			throw new MediaException("File seems to be corrupted: " + url, "IMAGE ERROR");
		}
		
		for (int i = 0; i < sizes; i++) {
			File thumb = thumbs.get(i);
			if (thumb.length() == 0)
				throw new MediaException("Thumbnail file empty: " + thumb, "THUMBNAIL ERROR");
			if (width < THUMB_SIZE[i]) {
				FileUtils.copyFile(content, thumb);
			}
			thumbnails.put(thumb, url);
		}
	}
	
	private List<String> extractDominantColors(List<String> results) {
		final Pattern pattern = Pattern.compile("#([0-9A-F]{6})");
		return results.stream()
				.skip(3)
				.sorted(Collections.reverseOrder())
				.limit(6)
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
			WebResource resource = edm.getWebResource(url);
			resource.setMimeType(mimeType);
			resource.setFileSize(result.getJSONObject("format").getLong("size"));
			
			JSONObject videoStream = null;
			JSONObject audioStream = null;
			for (Object streamObject : result.getJSONArray("streams")) {
				JSONObject stream = (JSONObject) streamObject;
				if (videoStream == null && "video".equals(stream.getString("codec_type")))
					videoStream = stream;
				if (audioStream == null && "audio".equals(stream.getString("codec_type")))
					audioStream = stream;
			}
			if (videoStream != null) {
				resource.setDuration(videoStream.getDouble("duration") * 1000);
				resource.setBitrate(videoStream.getInt("bit_rate"));
				resource.setWidth(videoStream.getInt("width"));
				resource.setHeight(videoStream.getInt("height"));
				resource.setCodecName(videoStream.getString("codec_name"));
				
				String[] frameRateParts = videoStream.getString("avg_frame_rate").split("/");
				double frameRate = Double.parseDouble(frameRateParts[0]) / Double.parseDouble(frameRateParts[1]);
				resource.setFrameRete(frameRate);
			} else if (audioStream != null) {
				resource.setDuration(audioStream.getDouble("duration") * 1000);
				resource.setBitrate(audioStream.getInt("bit_rate"));
				resource.setCahhnels(audioStream.getInt("channels"));
				resource.setSampleRate(audioStream.getInt("sample_rate"));
				resource.setSampleSize(audioStream.getInt("bits_per_sample"));
			} else {
				throw new Exception("No media streams");
			}
		} catch (Exception e) {
			log.info("Could not parse ffprobe response:\n" + StringUtils.join(resultLines, "\n"), e);
			throw new MediaException("File seems to be corrupted: " + url, "AUDIOVIDEO ERROR");
		}
	}
	
	private void processText(String url, String mimeType, File contents) throws IOException {
		if (!shouldExtractMetadata(urlTypes.get(url)))
			return;
		
		boolean containsText = mimeType.startsWith("text/");
		Integer resolution = null;
		
		if (mimeType.equals("application/pdf")) {
			try (PDDocument document = PDDocument.load(contents)) {
				PDFTextStripper pdfStripper = new PDFTextStripper();
				String text = pdfStripper.getText(document).replaceAll("\\s", "");
				containsText = !text.isEmpty();
				
				firstImageSearch:
				for (int i = 0; i < document.getNumberOfPages(); i++) {
					PDResources res = document.getPage(i).getResources();
					Iterator<COSName> it = res.getXObjectNames().iterator();
					while (it.hasNext()) {
						PDXObject xObject = res.getXObject(it.next());
						if (!(xObject instanceof PDImageXObject))
							continue;
						PDMetadata pdMetadata = ((PDImageXObject) xObject).getMetadata();
						if (pdMetadata == null)
							break firstImageSearch;
						String metadata = IOUtils.toString(pdMetadata.createInputStream(), "UTF-8");
						Matcher resolutionMatcher = Pattern
								.compile("<exif:XResolution>\\s*([0-9]+)\\s*</exif:XResolution>").matcher(metadata);
						if (resolutionMatcher.find())
							resolution = Integer.valueOf(resolutionMatcher.group(1));
						break firstImageSearch;
					}
				}
			}
		}
		
		WebResource resource = edm.getWebResource(url);
		resource.setContainsText(containsText);
		resource.setResolution(resolution);
	}
	
	private List<String> runCommand(List<String> command, boolean mergeError) throws IOException {
		return runCommand(command, mergeError, null);
	}
	
	private List<String> runCommand(List<String> command, boolean mergeError, byte[] inputBytes) throws IOException {
		Process process = new ProcessBuilder(command).redirectErrorStream(mergeError).start();
		if (!mergeError) {
			commandIOThreadPool.execute(() -> {
				try (InputStream errorStream = process.getErrorStream()) {
					String output = IOUtils.toString(errorStream);
					if (!StringUtils.isBlank(output))
						log.warn("Command: {}\nerror output:\n{}", command, output);
				} catch (IOException e) {
					log.error("Error stream reading faild for command " + command, e);
				}
			});
		}
		if (inputBytes != null) {
			commandIOThreadPool.execute(() -> {
				try (OutputStream processInput = process.getOutputStream()) {
					processInput.write(inputBytes);
				} catch (IOException e) {
					log.error("Pushing data to process input stream failed for command " + command, e);
				}
			});
		}
		try (InputStream in = process.getInputStream()) {
			return IOUtils.readLines(in);
		}
	}
	
	private void staticInit() {
		if (tempDir != null)
			return;
		synchronized (MediaProcessor.class) {
			if (tempDir != null)
				return;
			// get temp dir
			tempDir = new File(System.getProperty("java.io.tmpdir"));
			
			// load colormap
			try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("colormap.png")) {
				colormapFile = File.createTempFile("colormap", "png");
				colormapFile.deleteOnExit();
				try (FileOutputStream out = new FileOutputStream(colormapFile)) {
					IOUtils.copy(is, out);
				}
			} catch (IOException e) {
				throw new RuntimeException("colormap.png can't be loaded", e);
			}
			
			// find ffprobe
			try {
				List<String> lines = runCommand(Arrays.asList("ffprobe"), true);
				if (lines.isEmpty() || !lines.get(0).startsWith("ffprobe version 2"))
					throw new RuntimeException("ffprobe 2.x not found");
				ffprobeCmd = "ffprobe";
			} catch (IOException e) {
				throw new RuntimeException("Error while looking for ffprobe tools", e);
			}
			
			// find image magick
			try {
				List<String> lines = runCommand(Arrays.asList("magick", "-version"), true);
				if (!lines.isEmpty() && lines.get(0).startsWith("Version: ImageMagick 7")) {
					magickCmd = "magick";
				} else { // try convert, but careful about conflict with a windows tool
					boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
					List<String> paths = runCommand(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
					for (String path : paths) {
						lines = runCommand(Arrays.asList(path, "-version"), true);
						if (!lines.isEmpty() && lines.get(0).startsWith("Version: ImageMagick 6")) {
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
		// TODO pdf is not an image? https://europeana.atlassian.net/browse/MMS-34
		return mimeType.equals("application/pdf") || mimeType.startsWith("image/");
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
}
