package eu.europeana.metis.mediaservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.mediaservice.MediaProcessor.Thumbnail;

/**
 * This class performs thumbnail generation for images and PDF files using ImageMagick.
 */
public class ThumbnailGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailGenerator.class);

  private static final int[] THUMB_SIZE = {200, 400};
  private static final String[] THUMB_SUFFIX = {"-MEDIUM", "-LARGE"};

  private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));

  private static final String COMMAND_RESULT_FORMAT = "%w\n%h\n%[colorspace]\n";
  private static final int COMMAND_RESULT_WIDTH_LINE = 0;
  private static final int COMMAND_RESULT_HEIGHT_LINE = 1;
  private static final int COMMAND_RESULT_COLORSPACE_LINE = 2;
  private static final int COMMAND_RESULT_COLORS_LINE = 3;
  private static final int COMMAND_RESULT_MAX_COLORS = 6;

  private static String magickCmd;

  private static final File colormapFile;

  static {
    File foundColormapFile;
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("colormap.png")) {
      foundColormapFile = File.createTempFile("colormap", ".png");
      foundColormapFile.deleteOnExit();
      try (OutputStream out = Files.newOutputStream(foundColormapFile.toPath())) {
        IOUtils.copy(is, out);
      }
    } catch (IOException e) {
      foundColormapFile = null;
      LOGGER.warn("Could not load color map file: {}. No remapping will take place.",
          "colormap.png", e);
    }
    colormapFile = foundColormapFile;
  }

  private final CommandExecutor commandExecutor;

  protected final ArrayList<Thumbnail> thumbnails = new ArrayList<>();

  ThumbnailGenerator(CommandExecutor commandExecutor) throws MediaException {
    this.commandExecutor = commandExecutor;
    init(commandExecutor);
  }

  private static synchronized void init(CommandExecutor commandExecutor) throws MediaException {

    // If we already found the command, we don't need to do this.
    if (magickCmd != null) {
      return;
    }

    // Try the 'magick' command for ImageMagick 7.
    try {
      final List<String> lines =
          commandExecutor.runCommand(Arrays.asList("magick", "-version"), true);
      if (String.join("", lines).startsWith("Version: ImageMagick 7")) {
        magickCmd = "magick";
        LOGGER.info("Found ImageMagic 7. Command: {}", magickCmd);
        return;
      }
    } catch (IOException e) {
      LOGGER.info("Could not find ImageMagick 7 because of: {}.", e.getMessage());
      LOGGER.debug("Could not find ImageMagick 7 due to following problem.", e);
    }

    // Try the 'convert' command for ImageMagick 6.
    final boolean isWindows =
        System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    List<String> paths;
    try {
      paths =
          commandExecutor.runCommand(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
    } catch (IOException e) {
      LOGGER.warn("Could not find ImageMagick 6 due to following problem.", e);
      paths = Collections.emptyList();
    }
    for (String path : paths) {
      try {
        final List<String> lines =
            commandExecutor.runCommand(Arrays.asList(path, "-version"), true);
        if (String.join("", lines).startsWith("Version: ImageMagick 6")) {
          magickCmd = path;
          LOGGER.info("Found ImageMagic 6. Command: {}", magickCmd);
          return;
        }
      } catch (IOException e) {
        LOGGER.info("Could not find ImageMagick 6 at path {} because of: {}.", path,
            e.getMessage());
        LOGGER.debug("Could not find ImageMagick 6 at path {} due to following problem.", path, e);
      }
    }

    // So no image magick was found.
    LOGGER.error("Could not find ImageMagick 6 or 7. See previous log statements for details.");
    throw new MediaException("Could not find ImageMagick 6 or 7.");
  }

  /**
   * This is the main method of this class. It generates thumbnails for the given content. These
   * thumbnails are added to {@link #thumbnails}.
   * 
   * @param url The URL of the content. Used for determining the name of the output files.
   * @param mimeType The mime type of the content.
   * @param content The content for which to generate thumbnails.
   * @return The metadata of the image as gathered during processing.
   * @throws MediaException In case a problem occurred.
   */
  public ImageMetadata generateThumbnails(String url, String mimeType, File content)
      throws MediaException {
    try {
      return generateThumbnailsInternal(url, mimeType, content);
    } catch (IOException e) {
      throw new MediaException("I/O error during procesing", "IOException " + e.getMessage(), e);
    } catch (NoSuchAlgorithmException | RuntimeException e) {
      throw new MediaException("Unexpected error during procesing", e);
    }
  }

  private ImageMetadata generateThumbnailsInternal(String url, String mimeType, File content)
      throws IOException, MediaException, NoSuchAlgorithmException {

    if (content == null) {
      throw new MediaException("File content is null", "File content cannot be null");
    }

    final List<Thumbnail> thumbs = prepareThumbnailFiles(url);
    int sizes = THUMB_SIZE.length;

    ArrayList<String> command = new ArrayList<>(Arrays.asList(magickCmd, content.getPath() + "[0]",
        "-format", COMMAND_RESULT_FORMAT, "-write", "info:"));
    if ("application/pdf".equals(mimeType)) {
      command.addAll(Arrays.asList("-background", "white", "-alpha", "remove"));
    }
    for (int i = 0; i < sizes; i++) {
      // do not +delete the last one, use it to find dominant colors (smaller=quicker)
      if (i != sizes - 1) {
        command.add("(");
        command.add("+clone");
      }
      command.addAll(Arrays.asList("-thumbnail", THUMB_SIZE[i] + "x", "-write",
          thumbs.get(i).content.getPath()));
      if (i != sizes - 1) {
        command.add("+delete");
        command.add(")");
      }
    }
    command.addAll(Arrays.asList("-colorspace", "sRGB", "-dither", "Riemersma", "-format", "\n%c",
        "histogram:info:"));
    if (colormapFile != null) {
      command.addAll(Arrays.asList("-remap", colormapFile.getPath()));
    }

    List<String> results = commandExecutor.runCommand(command, false);

    // Read the image properties from the command output.
    final ImageMetadata result;
    try {
      final int width = Integer.parseInt(results.get(COMMAND_RESULT_WIDTH_LINE));
      final int height = Integer.parseInt(results.get(COMMAND_RESULT_HEIGHT_LINE));
      final String colorSpace = results.get(COMMAND_RESULT_COLORSPACE_LINE);
      final List<String> dominantColors =
          extractDominantColors(results, COMMAND_RESULT_COLORS_LINE);
      result = new ImageMetadata(width, height, colorSpace, dominantColors);
    } catch (RuntimeException e) {
      LOGGER.info("Could not parse ImageMagick response:\n" + StringUtils.join(results, "\n"), e);
      throw new MediaException("File seems to be corrupted", "IMAGE ERROR", e);
    }

    for (int i = 0; i < sizes; i++) {
      File thumb = thumbs.get(i).content;
      if (thumb.length() == 0) {
        throw new MediaException("Thumbnail file empty: " + thumb, "THUMBNAIL ERROR");
      }
      if (!"application/pdf".equals(mimeType) && result.getWidth() < THUMB_SIZE[i]) {
        FileUtils.copyFile(content, thumb);
      }
    }
    thumbnails.addAll(thumbs);

    return result;
  }

  private List<Thumbnail> prepareThumbnailFiles(String url)
      throws IOException, NoSuchAlgorithmException {
    File thumbsDir = new File(tempDir, "media_thumbnails");
    if (!thumbsDir.isDirectory() && !thumbsDir.mkdir()) {
      throw new IOException("Could not create thumbnails subdirectory: " + thumbsDir);
    }
    String md5 = md5Hex(url);
    List<Thumbnail> thumbs = new ArrayList<>(THUMB_SUFFIX.length);
    for (String thumbnailSuffix : THUMB_SUFFIX) {
      File f = File.createTempFile("thumb", ".tmp", thumbsDir);
      thumbs.add(new Thumbnail(url, md5 + thumbnailSuffix, f));
    }
    return thumbs;
  }

  private static String md5Hex(String s)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    byte[] bytes = s.getBytes("UTF-8");
    byte[] md5bytes = MessageDigest.getInstance("MD5").digest(bytes);
    return String.format("%032x", new BigInteger(1, md5bytes));
  }

  private List<String> extractDominantColors(List<String> results, int skipLines) {
    final Pattern pattern = Pattern.compile("#([0-9A-F]{6})");
    return results.stream().skip(skipLines).sorted(Collections.reverseOrder())
        .limit(COMMAND_RESULT_MAX_COLORS).map(line -> {
          Matcher m = pattern.matcher(line);
          m.find();
          return m.group(1); // throw exception if not found
        }).collect(Collectors.toList());
  }

  static synchronized void setCommand(String magick) {
    magickCmd = magick;
  }

  static File getColormapFile() {
    return colormapFile;
  }
}
