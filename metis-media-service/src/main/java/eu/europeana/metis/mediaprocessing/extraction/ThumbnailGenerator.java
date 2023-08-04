package eu.europeana.metis.mediaprocessing.extraction;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import eu.europeana.metis.schema.model.MediaType;
import eu.europeana.metis.utils.TempFileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs thumbnail generation for images and PDF files using ImageMagick.
 */
public class ThumbnailGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailGenerator.class);

  private static final String PNG_MIME_TYPE = "image/png";
  private static final String JPEG_MIME_TYPE = "image/jpeg";

  private static final String COMMAND_RESULT_FORMAT = "\n%w\n%h\n%[colorspace]\n";
  private static final int COMMAND_RESULT_WIDTH_LINE = 0;
  private static final int COMMAND_RESULT_HEIGHT_LINE = 1;
  private static final int COMMAND_RESULT_COLORSPACE_LINE = 2;
  private static final int COMMAND_RESULT_MAX_COLORS = 6;
  public static final String COLORMAP_PNG = "colormap.png";
  public static final int EXPECTED_CONTENT_MARKERS_COUNT = 5;
  public static final String MAGICK_TEMPORARY_PATH = "MAGICK_TEMPORARY_PATH";

  private static String globalMagickCommand;
  private static Path globalColormapFile;

  private final String magickCmd;
  private final String colormapFile;

  private final CommandExecutor commandExecutor;

  /**
   * Constructor. This is a wrapper for {@link ThumbnailGenerator#ThumbnailGenerator(CommandExecutor, String, String)} where the
   * properties are detected. It is advisable to use this constructor for non-testing purposes.
   *
   * @param commandExecutor A command executor. The calling class is responsible for closing this object.
   * @throws MediaProcessorException In case the properties could not be initialized.
   */
  public ThumbnailGenerator(CommandExecutor commandExecutor) throws MediaProcessorException {
    this(commandExecutor, getGlobalImageMagickCommand(commandExecutor), initColorMap().toString());
  }

  /**
   * Constructor.
   *
   * @param commandExecutor A command executor.The calling class is responsible for closing this object
   * @param magickCommand The magick command (how to trigger imageMagick).
   * @param colorMapFile The location of the color map file.
   */
  ThumbnailGenerator(CommandExecutor commandExecutor, String magickCommand, String colorMapFile) {
    this.commandExecutor = commandExecutor;
    this.magickCmd = magickCommand;
    this.colormapFile = colorMapFile;
  }

  private static Path initColorMap() throws MediaProcessorException {
    synchronized (ThumbnailGenerator.class) {

      // If we already found the color map, we don't need to do this
      if (globalColormapFile != null) {
        return globalColormapFile;
      }

      // Copy the color map file to the temp directory for use during this session.
      final Path colormapTempFile;
      try (InputStream colorMapInputStream =
          Thread.currentThread().getContextClassLoader().getResourceAsStream(COLORMAP_PNG)) {
        if (colorMapInputStream == null) {
          throw new MediaProcessorException("Could not load color map file: could not find file.");
        }
        colormapTempFile = TempFileUtils.createSecureTempFileDeleteOnExit("colormap", ".png");
        Files.copy(colorMapInputStream, colormapTempFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new MediaProcessorException(
            String.format("Could not load color map file: %s", COLORMAP_PNG), e);
      }

      // So everything went well. We set this as the new color map file.
      globalColormapFile = colormapTempFile;
      return globalColormapFile;
    }
  }

  private static String getGlobalImageMagickCommand(CommandExecutor commandExecutor)
      throws MediaProcessorException {
    synchronized (ThumbnailGenerator.class) {
      if (globalMagickCommand == null) {
        globalMagickCommand = discoverImageMagickCommand(commandExecutor);
      }
      return globalMagickCommand;
    }
  }

  static String discoverImageMagickCommand(CommandExecutor commandExecutor)
      throws MediaProcessorException {

    // Try the 'magick' command for ImageMagick 7.
    try {
      final String im7Response = commandExecutor.execute(Arrays.asList("magick", "-version"), emptyMap(), true,
          MediaProcessorException::new);
      if (im7Response.startsWith("Version: ImageMagick 7")) {
        final String result = "magick";
        LOGGER.info("Found ImageMagic 7. Command: {}", result);
        return result;
      }
    } catch (MediaProcessorException e) {
      LOGGER.info("Could not find ImageMagick 7 because of: {}.", e.getMessage());
      LOGGER.debug("Could not find ImageMagick 7 due to following problem.", e);
    }

    // Try the 'convert' command for ImageMagick 6: find locations of the executable.
    final boolean isWindows =
        System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    List<String> paths;
    try {
      paths = splitByNewLine(commandExecutor.execute(Arrays.asList(
          isWindows ? "where" : "which", "convert"), emptyMap(), true, MediaProcessorException::new));
    } catch (MediaProcessorException e) {
      LOGGER.warn("Could not find ImageMagick 6 due to following problem.", e);
      paths = Collections.emptyList();
    }

    // Try the 'convert' command for ImageMagick 6: try executables to find the right one.
    for (String path : paths) {
      try {
        final String pathResult = commandExecutor.execute(Arrays.asList(path, "-version"), emptyMap(), true,
            MediaProcessorException::new);
        if (pathResult.startsWith("Version: ImageMagick 6")) {
          LOGGER.info("Found ImageMagic 6. Command: {}", path);
          return path;
        }
      } catch (MediaProcessorException e) {
        LOGGER.info("Could not find ImageMagick 6 at path {} because of: {}.", path,
            e.getMessage());
        LOGGER.debug("Could not find ImageMagick 6 at path {} due to following problem.", path, e);
      }
    }

    // So no image magick was found.
    LOGGER.error("Could not find ImageMagick 6 or 7. See previous log statements for details.");
    throw new MediaProcessorException("Could not find ImageMagick 6 or 7.");
  }

  private static List<String> splitByNewLine(String input) {
    return Stream.of(input.split("\\R")).filter(StringUtils::isNotBlank)
                 .collect(Collectors.toList());
  }

  /**
   * This is the main method of this class. It generates thumbnails for the given content.
   *
   * @param url The URL of the content. Used for determining the name of the output files.
   * @param detectedMimeType The detected mime type of the content.
   * @param content The resource content for which to generate thumbnails.
   * @param removeAlpha Whether any alpha should be removed and replaced with a white background.
   * @return The metadata of the image as gathered during processing, together with the thumbnails. The list can be null or empty,
   * but does not contain null values or thumbnails without content.
   * @throws MediaExtractionException In case a problem occurred.
   */
  public Pair<ImageMetadata, List<Thumbnail>> generateThumbnails(String url, String detectedMimeType,
      File content, boolean removeAlpha) throws MediaExtractionException {

    // Sanity checking
    if (content == null) {
      throw new MediaExtractionException("File content is null");
    }
    if (MediaType.getMediaType(detectedMimeType) != MediaType.IMAGE) {
      throw new MediaExtractionException(
          "Cannot perform thumbnail generation on mime type '" + detectedMimeType + "'.");
    }

    // TODO JV We should change this into a whitelist of supported formats.
    // Exception for DjVu files
    if (detectedMimeType.startsWith("image/vnd.djvu") || detectedMimeType.startsWith("image/x-djvu")
        || detectedMimeType.startsWith("image/x.djvu")) {
      throw new MediaExtractionException("Cannot generate thumbnails for DjVu file.");
    }

    // Obtain the thumbnail files (they are still empty) - create temporary files for them.
    final List<ThumbnailWithSize> thumbnails = prepareThumbnailFiles(url, detectedMimeType);

    // Load the thumbnails: delete the temporary files, and the thumbnails in case of exceptions.
    final ImageMetadata image;
    try {
      image = generateThumbnailsInternal(thumbnails, removeAlpha, content);
    } catch (RuntimeException e) {
      closeAllThumbnailsSilently(thumbnails);
      throw new MediaExtractionException("Unexpected error during processing", e);
    } catch (MediaExtractionException e) {
      closeAllThumbnailsSilently(thumbnails);
      throw e;
    } finally {
      thumbnails.forEach(ThumbnailWithSize::deleteTempFileSilently);
    }

    // Done.
    final List<Thumbnail> resultThumbnails = thumbnails.stream()
                                                       .map(ThumbnailWithSize::getThumbnail).collect(Collectors.toList());
    return new ImmutablePair<>(image, resultThumbnails);
  }

  private static void closeAllThumbnailsSilently(List<ThumbnailWithSize> thumbnails) {
    for (ThumbnailWithSize thumbnail : thumbnails) {
      thumbnail.getThumbnail().close();
    }
  }

  List<String> createThumbnailGenerationCommand(List<ThumbnailWithSize> thumbnails,
      boolean removeAlpha, File content, String contentMarker) {

    // Compile the command
    final String commandResultFormat = contentMarker + COMMAND_RESULT_FORMAT + contentMarker + "\n";

    // To suppress warnings that can come up, we use the flag `-quiet'.
    // This flag needs to be at the beginning of the command to work
    final List<String> command = new ArrayList<>(Arrays.asList(magickCmd, "-quiet",
        content.getPath() + "[0]", "-format", commandResultFormat, "-write", "info:"));
    if (removeAlpha) {
      command.addAll(Arrays.asList("-background", "white", "-alpha", "remove"));
    }
    final int thumbnailCounter = thumbnails.size();
    for (int i = 0; i < thumbnailCounter; i++) {
      // do not +delete the last one, use it to find dominant colors (smaller=quicker)
      if (i != thumbnailCounter - 1) {
        command.add("(");
        command.add("+clone");
      }
      final ThumbnailWithSize thumbnail = thumbnails.get(i);
      command.addAll(Arrays.asList("-thumbnail", thumbnail.getImageSize() + "x", "-write",
          thumbnail.getImageMagickTypePrefix() + thumbnail.getTempFileForThumbnail().toString()));
      if (i != thumbnailCounter - 1) {
        command.add("+delete");
        command.add(")");
      }
    }
    final String colorResultFormat = "\n" + contentMarker + "\n%c\n" + contentMarker;
    command.addAll(Arrays.asList("-colorspace", "sRGB", "-dither", "Riemersma", "-remap",
        colormapFile, "-format", colorResultFormat, "histogram:info:"));

    return command;
  }

  private ImageMetadata generateThumbnailsInternal(List<ThumbnailWithSize> thumbnails,
      boolean removeAlpha, File content) throws MediaExtractionException {

    // Generate the thumbnails and read image properties.
    final String contentMarker = UUID.randomUUID().toString();
    final List<String> command = createThumbnailGenerationCommand(thumbnails, removeAlpha, content, contentMarker);

    final String response = executeImageMagick(command);
    final ImageMetadata result = parseCommandResponse(response, contentMarker);

    // Check the thumbnails.
    for (ThumbnailWithSize thumbnail : thumbnails) {
      try {

        // Check that the thumbnails are not empty.
        final Path tempFileForThumbnail = thumbnail.getTempFileForThumbnail();
        if (getFileSize(tempFileForThumbnail) == 0) {
          throw new MediaExtractionException("Thumbnail file empty: " + tempFileForThumbnail);
        }

        // Copy the thumbnail. In case of images: don't make a thumbnail larger than the original.
        final boolean shouldUseOriginal = result.getWidth() < thumbnail.getImageSize();
        if (shouldUseOriginal) {
          copyFile(content, thumbnail);
        } else {
          copyFile(thumbnail.getTempFileForThumbnail(), thumbnail);
        }

      } catch (IOException e) {
        throw new MediaExtractionException("Could not access thumbnail file", e);
      }
    }

    // Done.
    return result;
  }

  private String executeImageMagick(List<String> command) throws MediaExtractionException {
    Path tempDir = createMagickTempDirectory();
    try {
      return commandExecutor.execute(command, singletonMap(MAGICK_TEMPORARY_PATH, tempDir.toString()), false,
          message -> new MediaExtractionException("Could not analyze content and generate thumbnails: " + message));
    } finally {
      deleteMagickTempDirectory(tempDir);
    }
  }


  private Path createMagickTempDirectory() {
    try {
      return TempFileUtils.createSecureTempDirectory("magick_");
    } catch (IOException e) {
      throw new UncheckedIOException("Could not create temporary directory for media extraction!", e);
    }
  }

  private void deleteMagickTempDirectory(Path tempDir) {
    try {
      try (DirectoryStream<Path> files = Files.newDirectoryStream(tempDir)) {
        for (Path file : files) {
          LOGGER.warn("Image Magick process left temporary file: {}, it would be deleted.", file);
          //It uses deleteIfExists, cause this file could be deleted in meantime by OS.
          Files.deleteIfExists(file);
        }
      }
      Files.delete(tempDir);
    } catch (IOException e) {
      LOGGER.warn("Could not clear temporary Image Magick folder: {} cause of: {}", tempDir, e.getMessage(), e);
    }
  }

  long getFileSize(Path file) throws IOException {
    return Files.size(file);
  }

  void copyFile(Path source, ThumbnailWithSize destination) throws IOException {
    try (final InputStream thumbnailStream = Files.newInputStream(source)) {
      destination.getThumbnail().markAsWithContent(thumbnailStream);
    }
  }

  void copyFile(File source, ThumbnailWithSize destination) throws IOException {
    copyFile(source.toPath(), destination);
  }

  List<ThumbnailWithSize> prepareThumbnailFiles(String url, String detectedMimeType)
      throws MediaExtractionException {

    // Decide on the thumbnail file type
    final String imageMagickThumbnailTypePrefix;
    final String thumbnailMimeType;
    final String thumbnailFileSuffix;
    if (PNG_MIME_TYPE.equals(detectedMimeType)) {
      imageMagickThumbnailTypePrefix = "png:";
      thumbnailMimeType = PNG_MIME_TYPE;
      thumbnailFileSuffix = TempFileUtils.PNG_FILE_EXTENSION;
    } else {
      imageMagickThumbnailTypePrefix = "jpeg:";
      thumbnailMimeType = JPEG_MIME_TYPE;
      thumbnailFileSuffix = TempFileUtils.JPEG_FILE_EXTENSION;
    }

    // Create the thumbnails: one for each kind
    final String md5 = md5Hex(url);
    final List<ThumbnailWithSize> result = new ArrayList<>(ThumbnailKind.values().length);
    try {
      for (ThumbnailKind thumbnailKind : ThumbnailKind.values()) {
        final String targetName = md5 + thumbnailKind.getNameSuffix();
        // False positive - we don't want to close the thumbnail here.
        @SuppressWarnings("squid:S2095") final ThumbnailImpl thumbnail = new ThumbnailImpl(url, thumbnailMimeType, targetName);
        result.add(
            new ThumbnailWithSize(thumbnail, thumbnailKind.getImageSize(), imageMagickThumbnailTypePrefix, thumbnailFileSuffix));
      }
    } catch (RuntimeException | IOException e) {
      closeAllThumbnailsSilently(result);
      throw new MediaExtractionException("Could not create temporary thumbnail files.", e);
    }

    // Done.
    return result;
  }

  public static String md5Hex(String s) throws MediaExtractionException {
    try {
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8.name());
      // Note: we have no choice but to use MD5, this is agreed upon with the API implementation.
      // The data used are not private and are considered safe
      @SuppressWarnings({"findsecbugs:WEAK_MESSAGE_DIGEST_MD5", "java:S4790"})
      byte[] md5bytes = MessageDigest.getInstance("MD5").digest(bytes);
      return String.format("%032x", new BigInteger(1, md5bytes));
    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      throw new MediaExtractionException("Could not compute md5 hash", e);
    }
  }

  ImageMetadata parseCommandResponse(String response, String contentMarker)
      throws MediaExtractionException {
    try {

      // Divide in segments and check their number.
      final String[] segments = response.split(Pattern.quote(contentMarker), 6);
      if (segments.length < EXPECTED_CONTENT_MARKERS_COUNT) {
        throw new MediaExtractionException(String.format(
            "Could not parse ImageMagick response(there are not enough content markers):%s%s",
            System.lineSeparator(), response));
      }
      if (segments.length > EXPECTED_CONTENT_MARKERS_COUNT) {
        throw new MediaExtractionException(String
            .format("Could not parse ImageMagick response(there are too many content markers):%s%s",
                System.lineSeparator(), response));
      }

      // Check that what's returned before, between and after the pairs is empty. I.e. that the even
      // segments are empty. If there is any unexpected content, this could be an error message.
      final String unexpectedContent =
          IntStream.range(0, segments.length).filter(index -> index % 2 == 0)
                   .mapToObj(index -> segments[index])
                   .collect(Collectors.joining(System.lineSeparator()));
      if (StringUtils.isNotBlank(unexpectedContent)) {
        throw new MediaExtractionException(String
            .format("Unexpected content found in ImageMagick response: %s%s",
                System.lineSeparator(), unexpectedContent.trim()));
      }

      // Get the dominant colors - sort them by frequency (' ' comes before any number).
      final Pattern pattern = Pattern.compile("#([0-9A-F]{6})");
      final List<String> colorStrings =
          splitByNewLine(segments[3]).stream().sorted(Collections.reverseOrder())
                                     .limit(COMMAND_RESULT_MAX_COLORS).collect(Collectors.toList());
      final Supplier<Stream<Matcher>> streamMatcherSupplier = () -> colorStrings.stream()
                                                                                .map(pattern::matcher);
      if (!streamMatcherSupplier.get().allMatch(Matcher::find)) {
        throw new IllegalStateException("Invalid color line found.");
      }
      final List<String> dominantColors = streamMatcherSupplier.get().filter(Matcher::find)
                                                               .map(matcher -> matcher.group(1)).collect(Collectors.toList());

      // Get width, height and color space
      final List<String> metadata = splitByNewLine(segments[1]);
      final int width = Integer.parseInt(metadata.get(COMMAND_RESULT_WIDTH_LINE));
      final int height = Integer.parseInt(metadata.get(COMMAND_RESULT_HEIGHT_LINE));
      final String colorSpace = metadata.get(COMMAND_RESULT_COLORSPACE_LINE);

      // Done.
      return new ImageMetadata(width, height, colorSpace, dominantColors);
    } catch (RuntimeException e) {
      throw new MediaExtractionException(String
          .format("Could not parse ImageMagick response:%s%s", System.lineSeparator(), response),
          e);
    }
  }

  static class ThumbnailWithSize {

    private final ThumbnailImpl thumbnail;
    private final int imageSize;
    private final Path tempFileForThumbnail;
    private final String imageMagickTypePrefix;

    ThumbnailWithSize(ThumbnailImpl thumbnail, int imageSize, Path tempFileForThumbnail,
        String imageMagickTypePrefix) {
      this.thumbnail = thumbnail;
      this.imageSize = imageSize;
      this.tempFileForThumbnail = tempFileForThumbnail;
      this.imageMagickTypePrefix = imageMagickTypePrefix;
    }

    ThumbnailWithSize(ThumbnailImpl thumbnail, int imageSize, String imageMagickTypePrefix, String thumbnailFileSuffix)
        throws IOException {
      this(thumbnail, imageSize, TempFileUtils.createSecureTempFile("thumbnail_", thumbnailFileSuffix),
          imageMagickTypePrefix);
    }

    ThumbnailImpl getThumbnail() {
      return thumbnail;
    }

    int getImageSize() {
      return imageSize;
    }

    Path getTempFileForThumbnail() {
      return tempFileForThumbnail;
    }

    String getImageMagickTypePrefix() {
      return imageMagickTypePrefix;
    }

    void deleteTempFileSilently() {
      try {
        Files.deleteIfExists(getTempFileForThumbnail());
      } catch (IOException e) {
        LOGGER.warn("Could not close thumbnail: {}", getTempFileForThumbnail(), e);
      }
    }
  }
}
