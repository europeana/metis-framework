package eu.europeana.metis.mediaservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.metis.mediaservice.WebResource.Orientation;

class ImageProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessor.class);
	
	private static final int[] THUMB_SIZE = { 200, 400 };
	private static final String[] THUMB_SUFFIX = { "-MEDIUM", "-LARGE" };
	
	private static final File colormapFile;
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
	
	private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));
	
	private static String magickCmd;
	
	private CommandExecutor ce;
	
	protected Map<String, String> thumbnails = new HashMap<>();
	
	ImageProcessor(CommandExecutor ce) {
		this.ce = ce;
		init(ce);
	}
	
	private static synchronized void init(CommandExecutor ce) {
		if (magickCmd != null)
			return;
		try {
			List<String> lines = ce.runCommand(Arrays.asList("magick", "-version"), true);
			if (String.join("", lines).startsWith("Version: ImageMagick 7")) {
				magickCmd = "magick";
			} else { // try convert, but careful about conflict with a windows tool
				boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
				List<String> paths = ce.runCommand(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
				for (String path : paths) {
					lines = ce.runCommand(Arrays.asList(path, "-version"), true);
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
	
	static boolean isImage(String mimeType) {
		return mimeType.startsWith("image/");
	}
	
	void processImage(String url, Collection<UrlType> urlTypes, String mimeType, File content, EdmObject edm)
			throws MediaException, IOException {
		if (content == null)
			throw new IllegalArgumentException("content cannot be null");
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
		
		List<String> results = ce.runCommand(command, false);
		
		int width;
		try {
			width = Integer.parseInt(results.get(WIDTH_LINE));
			int height = Integer.parseInt(results.get(HEIGHT_LINE));
			if (MediaProcessor.shouldExtractMetadata(urlTypes)) {
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
	
	static void setCommand(String magick) {
		ImageProcessor.magickCmd = magick;
	}
	
	static File getColormapFile() {
		return colormapFile;
	}
}
