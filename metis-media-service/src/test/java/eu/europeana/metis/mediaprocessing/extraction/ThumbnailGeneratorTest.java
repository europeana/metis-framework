package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.extraction.ThumbnailGenerator.ThumbnailWithSize;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThumbnailGeneratorTest {

  private static final String IMAGE_MAGICK = "Image Magick";
  private static final String COLOR_MAP_FILE = "color map file";

  private static final String PDF_MIME_TYPE = "application/pdf";
  private static final String PNG_MIME_TYPE = "image/png";
  private static final String JPG_MIME_TYPE = "image/jpeg";

  private static CommandExecutor commandExecutor;
  private static ThumbnailGenerator thumbnailGenerator;

  @BeforeAll
  static void createMocks() {
    commandExecutor = mock(CommandExecutor.class);
    thumbnailGenerator = spy(new ThumbnailGenerator(commandExecutor, IMAGE_MAGICK, COLOR_MAP_FILE));
  }

  @BeforeEach
  void resetMocks() {
    reset(commandExecutor, thumbnailGenerator);
  }

  @Test
  void testDiscoverImageMagickCommand() throws CommandExecutionException, MediaProcessorException {

    // magick commands
    final String magick7Command = "magick";
    final String magick6Command = "convert";
    final String versionDirective = "-version";
    final String whichCommand = "which";
    final String whereCommand = "where";

    // Test I.M. 7
    final List<String> versionCommand = Arrays.asList(magick7Command, versionDirective);
    doReturn(Collections.singletonList(
        "Version: ImageMagick 7.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org"))
        .when(commandExecutor).execute(eq(versionCommand), eq(true));
    assertEquals(magick7Command, ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));
    doReturn(Collections.singletonList("Command unknown")).when(commandExecutor)
        .execute(eq(versionCommand), eq(true));

    // Test I.M. 6: detect three locations, the last of which is the correct one.
    final List<String> convertLocations = Arrays.asList("convert 1", "convert 2", "convert 3");
    doReturn(convertLocations).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true));
    doReturn(convertLocations).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true));
    final List<String> versionCommand0 = Arrays.asList(convertLocations.get(0), versionDirective);
    doReturn(Collections.singletonList("Command unknown")).when(commandExecutor)
        .execute(eq(versionCommand0), eq(true));
    final List<String> versionCommand1 = Arrays.asList(convertLocations.get(1), versionDirective);
    doThrow(CommandExecutionException.class).when(commandExecutor)
        .execute(eq(versionCommand1), eq(true));
    final List<String> versionCommand2 = Arrays.asList(convertLocations.get(2), versionDirective);
    doReturn(Collections.singletonList(
        "Version: ImageMagick 6.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org"))
        .when(commandExecutor).execute(eq(versionCommand2), eq(true));
    assertEquals(convertLocations.get(2),
        ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Change previous test by throwing exception for I.M 7 - should still detect I.M. 6.
    doThrow(CommandExecutionException.class).when(commandExecutor)
        .execute(eq(versionCommand), eq(true));
    assertEquals(convertLocations.get(2),
        ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Change previous test by throwing exception when doing where/which. Should now fail.
    doThrow(CommandExecutionException.class).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true));
    doThrow(CommandExecutionException.class).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true));
    assertThrows(MediaProcessorException.class,
        () -> ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Test other version of I.M. (make sure that where/which works again).
    doReturn(convertLocations).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true));
    doReturn(convertLocations).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true));
    doReturn(Collections.singletonList(
        "Version: ImageMagick 5.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org"))
        .when(commandExecutor).execute(eq(versionCommand2), eq(true));
    assertThrows(MediaProcessorException.class,
        () -> ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));
  }

  @Test
  void testThumbnailGeneration()
      throws MediaExtractionException, CommandExecutionException, IOException {

    // Define first thumbnail
    final int size1 = 123;
    final ThumbnailWithSize thumbnail1 = spy(new ThumbnailWithSize(mock(ThumbnailImpl.class), size1,
        Paths.get("File 1"), "prefix 1"));
    doNothing().when(thumbnail1).deleteTempFileSilently();

    // Define second thumbnail
    final int size2 = 321;
    final ThumbnailWithSize thumbnail2 = spy(new ThumbnailWithSize(mock(ThumbnailImpl.class), size2,
        Paths.get("File 2"), "prefix 2"));
    doNothing().when(thumbnail2).deleteTempFileSilently();

    // Define other method input
    final List<ThumbnailWithSize> thumbnails = Arrays.asList(thumbnail1, thumbnail2);
    final String url = "testUrl";
    final File content = new File("content file");
    final List<String> command = Arrays.asList("command1", "command2");
    final List<String> commandResponse = Arrays.asList("response1", "response2");
    final ImageMetadata imageMetadata = new ImageMetadata(200, 200, "sRGB",
        Arrays.asList("WHITE", "BLACK"));

    // Mock the thumbnail generator
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnailFiles(eq(url), anyString());
    doReturn(command).when(thumbnailGenerator)
        .createThumbnailGenerationCommand(same(thumbnails), notNull(), same(content));
    doReturn(commandResponse).when(commandExecutor).execute(command, false);
    doReturn(imageMetadata).when(thumbnailGenerator).parseCommandResponse(commandResponse);
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    doNothing().when(thumbnailGenerator).copyFile(any(Path.class), any());
    doNothing().when(thumbnailGenerator).copyFile(any(File.class), any());

    // Call the method and verify the result.
    final Pair<ImageMetadata, List<Thumbnail>> result = thumbnailGenerator
        .generateThumbnails(url, JPG_MIME_TYPE, content);
    assertSame(imageMetadata, result.getLeft());
    assertEquals(Arrays.asList(thumbnail1.getThumbnail(), thumbnail2.getThumbnail()),
        result.getRight());
    verify(thumbnail1.getThumbnail(), never()).close();
    verify(thumbnail2.getThumbnail(), never()).close();
    verify(thumbnail1, times(1)).deleteTempFileSilently();
    verify(thumbnail2, times(1)).deleteTempFileSilently();


    // Check that the generated thumbnail was used for thumbnail 1.
    verify(thumbnailGenerator, times(1)).copyFile(any(Path.class), any());
    verify(thumbnailGenerator, times(1)).copyFile(thumbnail1.getTempFileForThumbnail(), thumbnail1);

    // Check that the content was used for thumbnail 2.
    verify(thumbnailGenerator, times(1)).copyFile(any(File.class), any());
    verify(thumbnailGenerator, times(1)).copyFile(content, thumbnail2);

    // Check that the copy method was called twice more for text content.
    thumbnailGenerator.generateThumbnails(url, PDF_MIME_TYPE, content);
    verify(thumbnailGenerator, times(1)).copyFile(any(File.class), any());
    verify(thumbnailGenerator, times(3)).copyFile(any(Path.class), any());

    // Check null content
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, PDF_MIME_TYPE, null));

    // Check exception during command execution - thumbnails should be closed.
    doThrow(new CommandExecutionException("TEST", null)).when(commandExecutor)
        .execute(command, false);
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content));
    doReturn(commandResponse).when(commandExecutor).execute(command, false);
    verify(thumbnail1.getThumbnail(), times(1)).close();
    verify(thumbnail2.getThumbnail(), times(1)).close();

    // Check empty thumbnail - thumbnails should be closed.
    doReturn(0L).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content));
    doThrow(new IOException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content));
    doThrow(new RuntimeException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content));
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    verify(thumbnail1.getThumbnail(), times(4)).close();
    verify(thumbnail2.getThumbnail(), times(4)).close();

    // Check that all is well again.
    thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content);
  }

  @Test
  void testParseCommandResponse() throws MediaExtractionException {

    // Perform the call
    final List<String> input = Arrays
        .asList("589", "768", "sRGB", "      2995: ( 47, 79, 79,255) #2F4F4F DarkSlateGray",
            "        24: ( 72, 61,139,255) #483D8B DarkSlateBlue",
            "      6711: ( 85,107, 47,255) #556B2F DarkOliveGreen");
    final ImageMetadata result = thumbnailGenerator.parseCommandResponse(input);

    // Check result
    assertEquals(589, result.getWidth());
    assertEquals(768, result.getHeight());
    assertEquals("sRGB", result.getColorSpace());

    // Check dominant colors
    final List<String> colors = result.getDominantColors();
    assertEquals(3, colors.size());
    final Set<String> colorSet = new HashSet<>(colors);
    assertEquals(3, colorSet.size());
    assertTrue(colorSet.contains("2F4F4F"));
    assertTrue(colorSet.contains("483D8B"));
    assertTrue(colorSet.contains("556B2F"));

    // Check unexpected input
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(null));
    assertThrows(MediaExtractionException.class, () -> thumbnailGenerator.parseCommandResponse(
        Collections.emptyList()));
    input.set(0, null);
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(input));
    input.set(0, "");
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(input));
    input.set(0, "A");
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(input));
    input.set(0, "589");
    thumbnailGenerator.parseCommandResponse(input);

    // Check missing value
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(input.subList(0, 1)));

    // Check empty color list
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(input.subList(0, 2)));

    // Check bad color value
    final List<String> inputWithBadColor = Stream.concat(input.stream(), Stream.of("BAD COLOR"))
        .collect(Collectors.toList());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(inputWithBadColor));
  }

  @Test
  void testCreateThumbnailGenerationCommand() {

    // Define first thumbnail
    final int size1 = 123;
    final String prefix1 = "prefix1";
    final ThumbnailWithSize thumbnail1 = new ThumbnailWithSize(mock(ThumbnailImpl.class), size1,
        Paths.get("File 1"), prefix1);

    // Define second thumbnail
    final int size2 = 321;
    final String prefix2 = "prefix2";
    final ThumbnailWithSize thumbnail2 = new ThumbnailWithSize(mock(ThumbnailImpl.class), size2,
        Paths.get("File 2"), prefix2);

    // Define other method input
    final List<ThumbnailWithSize> input = Arrays.asList(thumbnail1, thumbnail2);
    final File file = new File("content file");

    // Make call for image
    final List<String> commandImage = thumbnailGenerator
        .createThumbnailGenerationCommand(input, JPG_MIME_TYPE, file);

    // Verify image
    final List<String> expectedImage = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", prefix1 + thumbnail1.getTempFileForThumbnail().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", prefix2 + thumbnail2.getTempFileForThumbnail().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n%c", "histogram:info:");
    assertEquals(expectedImage, commandImage);

    // Make call for PDF
    final List<String> commandText = thumbnailGenerator
        .createThumbnailGenerationCommand(input, PDF_MIME_TYPE, file);

    // Verify text
    final List<String> expectedText = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
        "-background", "white", "-alpha", "remove", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", prefix1 + thumbnail1.getTempFileForThumbnail().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", prefix2 + thumbnail2.getTempFileForThumbnail().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n%c", "histogram:info:");
    assertEquals(expectedText, commandText);
  }

  @Test
  void testPrepareThumbnailFiles() throws MediaExtractionException {
    testPrepareThumbnailFiles(PNG_MIME_TYPE, PNG_MIME_TYPE, "png:");
    testPrepareThumbnailFiles(PDF_MIME_TYPE, PNG_MIME_TYPE, "png:");
    testPrepareThumbnailFiles(JPG_MIME_TYPE, JPG_MIME_TYPE, "jpeg:");
    testPrepareThumbnailFiles("other", JPG_MIME_TYPE, "jpeg:");
  }

  private void testPrepareThumbnailFiles(String detectedMimeType, String expectedMimeType,
      String expectedImageMagickPrefix) throws MediaExtractionException {

    // Make the call
    final String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
    final String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
    final List<ThumbnailWithSize> thumbnails = thumbnailGenerator.prepareThumbnailFiles(url, detectedMimeType);

    // Parse and verify result: there are two thumbnails with unique sizes.
    assertEquals(2, thumbnails.size());
    final Map<Integer, ThumbnailWithSize> thumbnailMap = thumbnails.stream().collect(
        Collectors.toMap(ThumbnailWithSize::getImageSize, Function.identity()));
    assertEquals(2, thumbnailMap.size());

    // Inspect the thumbnails and then close them.
    final ThumbnailWithSize thumbnailWithSize1 = thumbnailMap.get(200);
    final ThumbnailWithSize thumbnailWithSize2 = thumbnailMap.get(400);
    try (ThumbnailImpl thumbnail1 = thumbnailWithSize1.getThumbnail();
        ThumbnailImpl thumbnail2 = thumbnailWithSize2.getThumbnail()) {

      // Check the medium thumbnail including its size and name.
      assertEquals(expectedImageMagickPrefix, thumbnailWithSize1.getImageMagickTypePrefix());
      assertNotNull(thumbnail1);
      assertEquals(md5 + "-MEDIUM", thumbnail1.getTargetName());
      assertFalse(thumbnail1.hasContent());
      assertEquals(0, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());
      assertEquals(expectedMimeType, thumbnail1.getMimeType());

      // Check the large thumbnail including its size and name.
      assertEquals(expectedImageMagickPrefix, thumbnailWithSize2.getImageMagickTypePrefix());
      assertNotNull(thumbnail2);
      assertEquals(md5 + "-LARGE", thumbnail2.getTargetName());
      assertFalse(thumbnail1.hasContent());
      assertEquals(0, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());
      assertEquals(expectedMimeType, thumbnail1.getMimeType());

    }
  }
}
