package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  void testDiscoverImageMagickCommand() throws MediaProcessorException {

    // magick commands
    final String magick7Command = "magick";
    final String magick6Command = "convert";
    final String versionDirective = "-version";
    final String whichCommand = "which";
    final String whereCommand = "where";

    // Test I.M. 7
    final List<String> versionCommand = Arrays.asList(magick7Command, versionDirective);
    doReturn("Version: ImageMagick 7.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org")
        .when(commandExecutor).execute(eq(versionCommand), eq(true), any());
    assertEquals(magick7Command, ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));
    doReturn("Command unknown").when(commandExecutor).execute(eq(versionCommand), eq(true), any());

    // Test I.M. 6: detect three locations, the last of which is the correct one.
    final List<String> convertLocations = Arrays.asList("convert 1", "convert 2", "convert 3");
    final String convertLocationsConcat = String.join("\n", convertLocations);
    doReturn(convertLocationsConcat).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true), any());
    doReturn(convertLocationsConcat).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true), any());
    final List<String> versionCommand0 = Arrays.asList(convertLocations.get(0), versionDirective);
    doReturn("Command unknown").when(commandExecutor).execute(eq(versionCommand0), eq(true), any());
    final List<String> versionCommand1 = Arrays.asList(convertLocations.get(1), versionDirective);
    doThrow(MediaProcessorException.class).when(commandExecutor)
        .execute(eq(versionCommand1), eq(true), any());
    final List<String> versionCommand2 = Arrays.asList(convertLocations.get(2), versionDirective);
    doReturn("Version: ImageMagick 6.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org")
        .when(commandExecutor).execute(eq(versionCommand2), eq(true), any());
    assertEquals(convertLocations.get(2),
        ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Change previous test by throwing exception for I.M 7 - should still detect I.M. 6.
    doThrow(MediaProcessorException.class).when(commandExecutor)
        .execute(eq(versionCommand), eq(true), any());
    assertEquals(convertLocations.get(2),
        ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Change previous test by throwing exception when doing where/which. Should now fail.
    doThrow(MediaProcessorException.class).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true), any());
    doThrow(MediaProcessorException.class).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true), any());
    assertThrows(MediaProcessorException.class,
        () -> ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));

    // Test other version of I.M. (make sure that where/which works again).
    doReturn(convertLocationsConcat).when(commandExecutor)
        .execute(eq(Arrays.asList(whichCommand, magick6Command)), eq(true), any());
    doReturn(convertLocationsConcat).when(commandExecutor)
        .execute(eq(Arrays.asList(whereCommand, magick6Command)), eq(true), any());
    doReturn("Version: ImageMagick 5.9.7-4 Q16 x86_64 20170114 http://www.imagemagick.org")
        .when(commandExecutor).execute(eq(versionCommand2), eq(true), any());
    assertThrows(MediaProcessorException.class,
        () -> ThumbnailGenerator.discoverImageMagickCommand(commandExecutor));
  }

  @Test
  void testThumbnailGeneration() throws MediaExtractionException, IOException {

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
    final String commandResponse = "response1\nresponse2";
    final ImageMetadata imageMetadata = new ImageMetadata(200, 200, "sRGB",
        Arrays.asList("WHITE", "BLACK"));

    // Mock the thumbnail generator
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnailFiles(eq(url), anyString());
    doReturn(command).when(thumbnailGenerator)
        .createThumbnailGenerationCommand(same(thumbnails), anyBoolean(), same(content), any());
    doReturn(commandResponse).when(commandExecutor).execute(eq(command), eq(false), any());
    doReturn(imageMetadata).when(thumbnailGenerator).parseCommandResponse(eq(commandResponse), any());
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    doNothing().when(thumbnailGenerator).copyFile(any(Path.class), any());
    doNothing().when(thumbnailGenerator).copyFile(any(File.class), any());

    // Call the method and verify the result.
    final Pair<ImageMetadata, List<Thumbnail>> result = thumbnailGenerator
        .generateThumbnails(url, JPG_MIME_TYPE, content, false);
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

    // Check non-image content
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.generateThumbnails(url, PDF_MIME_TYPE, content, false));

    // Check null content
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, null, true));

    // Check exception during command execution - thumbnails should be closed.
    doThrow(new MediaExtractionException("TEST", null)).when(commandExecutor)
        .execute(eq(command), eq(false), any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content, false));
    doReturn(commandResponse).when(commandExecutor).execute(eq(command), eq(false), any());
    verify(thumbnail1.getThumbnail(), times(1)).close();
    verify(thumbnail2.getThumbnail(), times(1)).close();

    // Check empty thumbnail - thumbnails should be closed.
    doReturn(0L).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content, false));
    doThrow(new IOException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content, false));
    doThrow(new RuntimeException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content, false));
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    verify(thumbnail1.getThumbnail(), times(4)).close();
    verify(thumbnail2.getThumbnail(), times(4)).close();

    // Check that all is well again.
    thumbnailGenerator.generateThumbnails(url, JPG_MIME_TYPE, content, false);
  }

  private static String concat(List<String> input) {
    return String.join("\n", input);
  }

  @Test
  void testParseCommandResponse() throws MediaExtractionException {

    // Perform the call
    final String contentMarker = "1234567890";
    final List<String> input = Arrays
            .asList("", contentMarker, "589", "768", "sRGB", contentMarker, "", contentMarker,
                    "      2995: ( 47, 79, 79,255) #2F4F4F DarkSlateGray",
                    "        24: ( 72, 61,139,255) #483D8B DarkSlateBlue",
                    "      6711: ( 85,107, 47,255) #556B2F DarkOliveGreen", contentMarker, "");
    assertEquals(13, input.size()); // If false, recalculate indices below.
    final ImageMetadata result = thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

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

    // Check missing content marker at the beginning
    input.set(1, "");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(1, contentMarker);
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check missing content marker in the middle
    input.set(5, "");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(5, contentMarker);
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check missing content marker at the end
    input.set(11, "");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(11, contentMarker);
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check additional content marker at the end
    input.set(0, contentMarker);
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(0, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check additional content marker in the middle
    input.set(6, contentMarker);
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(6, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check additional content marker at the end
    input.set(12, contentMarker);
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(12, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check unexpected content at the beginning
    input.set(0, "UNEXPECTED CONTENT");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(0, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check unexpected content in the middle
    input.set(6, "UNEXPECTED CONTENT");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(6, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check unexpected content at the end
    input.set(12, "UNEXPECTED CONTENT");
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(12, "");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check unexpected input
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(null, contentMarker));
    assertThrows(MediaExtractionException.class,
            () -> thumbnailGenerator.parseCommandResponse("", contentMarker));
    input.set(2, null);
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(2, "");
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(2, "A");
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));
    input.set(2, "589");
    thumbnailGenerator.parseCommandResponse(concat(input), contentMarker);

    // Check bad color value
    input.set(8, "BAD COLOR");
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.parseCommandResponse(concat(input), contentMarker));

    // Check empty color list
    input.set(8, "");
    input.set(9, "");
    input.set(10, "");
    assertTrue(thumbnailGenerator.parseCommandResponse(concat(input), contentMarker)
            .getDominantColors().isEmpty());
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
    final String contentMarker = "1234567890";

    // Make call without removing alpha
    final List<String> commandImage = thumbnailGenerator
        .createThumbnailGenerationCommand(input, false, file, contentMarker);

    // Verify
    final List<String> expectedImage = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", contentMarker + "\n%w\n%h\n%[colorspace]\n" + contentMarker + "\n", "-write", "info:", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", prefix1 + thumbnail1.getTempFileForThumbnail().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", prefix2 + thumbnail2.getTempFileForThumbnail().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n" + contentMarker + "\n%c\n" + contentMarker, "histogram:info:");
    assertEquals(expectedImage, commandImage);

    // Make call with removing alpha
    final List<String> commandText = thumbnailGenerator
        .createThumbnailGenerationCommand(input, true, file, contentMarker);

    // Verify
    final List<String> expectedText = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", contentMarker + "\n%w\n%h\n%[colorspace]\n" + contentMarker + "\n", "-write", "info:",
        "-background", "white", "-alpha", "remove", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", prefix1 + thumbnail1.getTempFileForThumbnail().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", prefix2 + thumbnail2.getTempFileForThumbnail().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n" + contentMarker + "\n%c\n" + contentMarker, "histogram:info:");
    assertEquals(expectedText, commandText);
  }

  @Test
  void testPrepareThumbnailFiles() throws MediaExtractionException {
    testPrepareThumbnailFiles(PNG_MIME_TYPE, PNG_MIME_TYPE, "png:");
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
      final Long contentSize = 0L;
      assertEquals(contentSize, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());
      assertEquals(expectedMimeType, thumbnail1.getMimeType());

      // Check the large thumbnail including its size and name.
      assertEquals(expectedImageMagickPrefix, thumbnailWithSize2.getImageMagickTypePrefix());
      assertNotNull(thumbnail2);
      assertEquals(md5 + "-LARGE", thumbnail2.getTargetName());
      assertFalse(thumbnail1.hasContent());
      assertEquals(contentSize, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());
      assertEquals(expectedMimeType, thumbnail1.getMimeType());

    }
  }
}
