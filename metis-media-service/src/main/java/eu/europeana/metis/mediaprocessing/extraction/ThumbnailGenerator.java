package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs thumbnail generation for images and PDF files using ImageMagick.
 */
class ThumbnailGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailGenerator.class);

  private static final int[] THUMB_SIZE = {200, 400};
  private static final String[] THUMB_SUFFIX = {"-MEDIUM", "-LARGE"};

  private static final String COMMAND_RESULT_FORMAT = "%w\n%h\n%[colorspace]\n";
  private static final int COMMAND_RESULT_WIDTH_LINE = 0;
  private static final int COMMAND_RESULT_HEIGHT_LINE = 1;
  private static final int COMMAND_RESULT_COLORSPACE_LINE = 2;
  private static final int COMMAND_RESULT_COLORS_LINE = 3;
  private static final int COMMAND_RESULT_MAX_COLORS = 6;

  private static String globalMagickCmd;
  private static Path globalColormapFile;
  
  private final String magickCmd;
  private final String colormapFile;
  
  private final CommandExecutor commandExecutor;

  /**
   * Constructor. This is a wrapper for
   * {@link ThumbnailGenerator#ThumbnailGenerator(CommandExecutor, String, Path)} where the
   * properties are detected. It is advisable to use this constructor for non-testing purposes.
   * 
   * @param commandExecutor A command executor.
   * @throws MediaProcessorException In case the properties could not be initialized.
   */
  ThumbnailGenerator(CommandExecutor commandExecutor) throws MediaProcessorException {
    this(commandExecutor, initImageMagick(commandExecutor), initColorMap().toString());
  }

  /**
   * Constructor.
   * 
   * @param commandExecutor A command executor.
   * @param magickCommand The magick command (how to trigger imageMagick).
   * @param colorMapFile The location of the color map file.
   */
  ThumbnailGenerator(CommandExecutor commandExecutor, String magickCommand, String colorMapFile) {
    this.commandExecutor = commandExecutor;
    this.magickCmd = magickCommand;
    this.colormapFile = colorMapFile;
  }

  private static synchronized Path initColorMap() throws MediaProcessorException {

    // If we already found the color map, we don't need to do this
    if (globalColormapFile != null) {
      return globalColormapFile;
    }

    // Copy the color map file to the temp directory for use during this session.
    final Path colormapTempFile;
    try (InputStream colorMapInputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("colormap.png")) {
      colormapTempFile = Files.createTempFile("colormap", ".png");
      Files.copy(colorMapInputStream, colormapTempFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      LOGGER.warn("Could not load color map file: {}.", "colormap.png", e);
      throw new MediaProcessorException("Could not load color map file.", e);
    }

    // Make sure that the temporary file is removed when we're done with it.
    colormapTempFile.toFile().deleteOnExit();

    // So everything went well. We set this as the new color map file.
    globalColormapFile = colormapTempFile;
    return globalColormapFile;
  }

  private static synchronized String initImageMagick(CommandExecutor commandExecutor)
      throws MediaProcessorException {

    // If we already found the command, we don't need to do this.
    if (globalMagickCmd != null) {
      return globalMagickCmd;
    }

    // Try the 'magick' command for ImageMagick 7.
    try {
      final List<String> lines =
          commandExecutor.execute(Arrays.asList("magick", "-version"), true);
      if (String.join("", lines).startsWith("Version: ImageMagick 7")) {
        globalMagickCmd = "magick";
        LOGGER.info("Found ImageMagic 7. Command: {}", globalMagickCmd);
        return globalMagickCmd;
      }
    } catch (CommandExecutionException e) {
      LOGGER.info("Could not find ImageMagick 7 because of: {}.", e.getMessage());
      LOGGER.debug("Could not find ImageMagick 7 due to following problem.", e);
    }

    // Try the 'convert' command for ImageMagick 6: find locations of the executable.
    final boolean isWindows =
        System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    List<String> paths;
    try {
      paths =
          commandExecutor.execute(Arrays.asList(isWindows ? "where" : "which", "convert"), true);
    } catch (CommandExecutionException e) {
      LOGGER.warn("Could not find ImageMagick 6 due to following problem.", e);
      paths = Collections.emptyList();
    }
    
    // Try the 'convert' command for ImageMagick 6: try executables to find the right one.
    for (String path : paths) {
      try {
        final List<String> lines =
            commandExecutor.execute(Arrays.asList(path, "-version"), true);
        if (String.join("", lines).startsWith("Version: ImageMagick 6")) {
          globalMagickCmd = path;
          LOGGER.info("Found ImageMagic 6. Command: {}", globalMagickCmd);
          return globalMagickCmd;
        }
      } catch (CommandExecutionException e) {
        LOGGER.info("Could not find ImageMagick 6 at path {} because of: {}.", path,
            e.getMessage());
        LOGGER.debug("Could not find ImageMagick 6 at path {} due to following problem.", path, e);
      }
    }

    // So no image magick was found.
    LOGGER.error("Could not find ImageMagick 6 or 7. See previous log statements for details.");
    throw new MediaProcessorException("Could not find ImageMagick 6 or 7.");
  }

  /**
   * This is the main method of this class. It generates thumbnails for the given content.
   * 
   * @param url The URL of the content. Used for determining the name of the output files.
   * @param resourceType The resource type of the content.
   * @param content The resource content for which to generate thumbnails.
   * @return The metadata of the image as gathered during processing.
   * @throws MediaExtractionException In case a problem occurred.
   */
  public Pair<ImageMetadata, List<Thumbnail>> generateThumbnails(String url,
      ResourceType resourceType, File content) throws MediaExtractionException {
    
    // Sanity checking
    if (content == null) {
      throw new MediaExtractionException("File content is null");
    }
    
    // Obtain the thumbnail files (they are still empty)
    final List<ThumbnailImpl> thumbnails = prepareThumbnailFiles(url);

    // Load the thumbnail files: in case of problems, make sure to delete them.
    final ImageMetadata result;
    try {
      result = generateThumbnailsInternal(thumbnails, resourceType, content);
    } catch (RuntimeException e) {
      closeAllThumbnailsSilently(thumbnails);
      throw new MediaExtractionException("Unexpected error during procesing", e);
    } catch (MediaExtractionException e) {
      closeAllThumbnailsSilently(thumbnails);
      throw e;
    }

    // Done.
    return new ImmutablePair<>(result, new ArrayList<>(thumbnails));
  }
  
  private static void closeAllThumbnailsSilently(List<? extends Thumbnail> thumbnails) {
    thumbnails.forEach(thumbnail -> {
      try {
        thumbnail.close();
      } catch (IOException e) {
        LOGGER.warn("Could not close thumbnail: {}", thumbnail.getResourceUrl(), e);
      }
    });
  }

  private List<String> createThumbnailGenerationCommand(List<ThumbnailImpl> thumbs, ResourceType resourceType, File content){
    final int sizes = THUMB_SIZE.length;
    final List<String> command = new ArrayList<>(Arrays.asList(magickCmd, content.getPath() + "[0]",
        "-format", COMMAND_RESULT_FORMAT, "-write", "info:"));
    if (resourceType == ResourceType.TEXT) {
      // in case of text (i.e. PDFs): specify white background
      command.addAll(Arrays.asList("-background", "white", "-alpha", "remove"));
    }
    for (int i = 0; i < sizes; i++) {
      // do not +delete the last one, use it to find dominant colors (smaller=quicker)
      if (i != sizes - 1) {
        command.add("(");
        command.add("+clone");
      }
      command.addAll(Arrays.asList("-thumbnail", THUMB_SIZE[i] + "x", "-write",
          thumbs.get(i).getContentPath().toString()));
      if (i != sizes - 1) {
        command.add("+delete");
        command.add(")");
      }
    }
    command.addAll(Arrays.asList("-colorspace", "sRGB", "-dither", "Riemersma", "-remap",
        colormapFile, "-format", "\n%c", "histogram:info:"));
    return command;
  }
  
  private ImageMetadata generateThumbnailsInternal(List<ThumbnailImpl> thumbs, ResourceType resourceType,
      File content) throws MediaExtractionException {

    // Generate the thumbnails and read image properties.
    final List<String> results;
    try {
      results = commandExecutor.execute(createThumbnailGenerationCommand(thumbs, resourceType, content), false);
    } catch (CommandExecutionException e) {
      throw new MediaExtractionException("Could not analyze content and generate thumbnails.", e);
    }

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
      throw new MediaExtractionException("File seems to be corrupted", e);
    }

    // Check the thumbnails.
    for (int i = 0; i < THUMB_SIZE.length; i++) {
      try {
        
        // Check that the thumbnails are not empty.
        final Path thumb = thumbs.get(i).getContentPath();
        if (Files.size(thumb) == 0) {
          throw new MediaExtractionException("Thumbnail file empty: " + thumb);
        }
        
        // In case of actual images: don't make a thumbnail larger than the original.
        if (resourceType == ResourceType.IMAGE && result.getWidth() < THUMB_SIZE[i]) {
          Files.copy(content.toPath(), thumb);
        }
      } catch (IOException e) {
        throw new MediaExtractionException("Could not access thumbnail file", e);
      }
    }

    // Done.
    return result;
  }

  private List<ThumbnailImpl> prepareThumbnailFiles(String url) throws MediaExtractionException {
    String md5 = md5Hex(url);
    List<ThumbnailImpl> thumbs = new ArrayList<>(THUMB_SUFFIX.length);
    try {
      for (String thumbnailSuffix : THUMB_SUFFIX) {
        thumbs.add(new ThumbnailImpl(url, md5 + thumbnailSuffix));
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not create thumbnail files.", e);
    }
    return thumbs;
  }

  private static String md5Hex(String s) throws MediaExtractionException {
    try {
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8.name());
      byte[] md5bytes = MessageDigest.getInstance("MD5").digest(bytes);
      return String.format("%032x", new BigInteger(1, md5bytes));
    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      throw new MediaExtractionException("Could not compute md5 hash", e);
    }
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
}
