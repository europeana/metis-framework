package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import eu.europeana.metis.mediaprocessing.extraction.ThumbnailGenerator.ThumbnailWithSize;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThumbnailGeneratorTest {

  private static final String IMAGE_MAGICK = "Image Magick";
  private static final String COLOR_MAP_FILE = "color map file";

  private static CommandExecutor commandExecutor;
  private static ThumbnailGenerator thumbnailGenerator;

  @BeforeAll
  static void prepare() {
    commandExecutor = mock(CommandExecutor.class);
    thumbnailGenerator = spy(new ThumbnailGenerator(commandExecutor, IMAGE_MAGICK, COLOR_MAP_FILE));
  }

  @BeforeEach
  void resetMocks() {
    reset(commandExecutor, thumbnailGenerator);
  }

  @Test
  void testThumbnailGeneration()
      throws MediaExtractionException, CommandExecutionException, IOException {

    // Define first thumbnail
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 1")).when(thumbnail1).getContentPath();
    final int size1 = 123;

    // Define second thumbnail
    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 2")).when(thumbnail2).getContentPath();
    final int size2 = 321;

    // Define other method input
    final List<ThumbnailWithSize> thumbnails = Arrays
        .asList(new ThumbnailWithSize(thumbnail1, size1), new ThumbnailWithSize(thumbnail2, size2));
    final String url = "testUrl";
    final File content = new File("content file");
    final List<String> command = Arrays.asList("command1", "command2");
    final List<String> commandResponse = Arrays.asList("response1", "response2");
    final ImageMetadata imageMetadata = new ImageMetadata(200, 200, "sRGB",
        Arrays.asList("WHITE", "BLACK"));

    // Mock the thumbnail generator
    doReturn(thumbnails).when(thumbnailGenerator).prepareThumbnailFiles(url);
    doReturn(command).when(thumbnailGenerator)
        .createThumbnailGenerationCommand(same(thumbnails), notNull(), same(content));
    doReturn(commandResponse).when(commandExecutor).execute(command, false);
    doReturn(imageMetadata).when(thumbnailGenerator).parseCommandResponse(commandResponse);
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    doNothing().when(thumbnailGenerator).copyFile(any(), any());

    // Call the method and verify the result.
    final Pair<ImageMetadata, List<Thumbnail>> result = thumbnailGenerator
        .generateThumbnails(url, ResourceType.IMAGE, content);
    assertSame(imageMetadata, result.getLeft());
    assertEquals(Arrays.asList(thumbnail1, thumbnail2), result.getRight());
    verify(thumbnail1, never()).close();
    verify(thumbnail2, never()).close();

    // Check that the copy method was called: the source was too small for the large thumbnail.
    verify(thumbnailGenerator, times(1)).copyFile(any(), any());
    verify(thumbnailGenerator).copyFile(content, thumbnail2.getContentPath());

    // Check that the copy method is not called again for text.
    thumbnailGenerator.generateThumbnails(url, ResourceType.TEXT, content);
    verify(thumbnailGenerator, times(1)).copyFile(any(), any());

    // Check null content
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, ResourceType.TEXT, null));

    // Check exception during command execution - thumbnails should be closed.
    doThrow(new CommandExecutionException("TEST", null)).when(commandExecutor)
        .execute(command, false);
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content));
    doReturn(commandResponse).when(commandExecutor).execute(command, false);
    verify(thumbnail1, times(1)).close();
    verify(thumbnail2, times(1)).close();

    // Check empty thumbnail - thumbnails should be closed.
    doReturn(0L).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content));
    doThrow(new IOException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content));
    doThrow(new RuntimeException()).when(thumbnailGenerator).getFileSize(any());
    assertThrows(MediaExtractionException.class,
        () -> thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content));
    doReturn(1024L).when(thumbnailGenerator).getFileSize(any());
    verify(thumbnail1, times(4)).close();
    verify(thumbnail2, times(4)).close();

    // Check that all is well again.
    thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content);
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
  }

  @Test
  void testCreateThumbnailGenerationCommand() {

    // Define first thumbnail
    final ThumbnailImpl thumbnail1 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 1")).when(thumbnail1).getContentPath();
    final int size1 = 123;

    // Define second thumbnail
    final ThumbnailImpl thumbnail2 = mock(ThumbnailImpl.class);
    doReturn(Paths.get("File 2")).when(thumbnail2).getContentPath();
    final int size2 = 321;

    // Define other method input
    final List<ThumbnailWithSize> input = Arrays
        .asList(new ThumbnailWithSize(thumbnail1, size1), new ThumbnailWithSize(thumbnail2, size2));
    final File file = new File("content file");

    // Make call for image
    final List<String> commandImage = thumbnailGenerator
        .createThumbnailGenerationCommand(input, ResourceType.IMAGE, file);

    // Verify image
    final List<String> expectedImage = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", thumbnail1.getContentPath().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", thumbnail2.getContentPath().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n%c", "histogram:info:");
    assertEquals(expectedImage, commandImage);

    // Make call for text
    final List<String> commandText = thumbnailGenerator
        .createThumbnailGenerationCommand(input, ResourceType.TEXT, file);

    // Verify text
    final List<String> expectedText = Arrays.asList(IMAGE_MAGICK, file.getPath() + "[0]",
        "-format", "%w\n%h\n%[colorspace]\n", "-write", "info:",
        "-background", "white", "-alpha", "remove", "(", "+clone",
        "-thumbnail", size1 + "x", "-write", thumbnail1.getContentPath().toString(), "+delete", ")",
        "-thumbnail", size2 + "x", "-write", thumbnail2.getContentPath().toString(),
        "-colorspace", "sRGB", "-dither", "Riemersma", "-remap", COLOR_MAP_FILE,
        "-format", "\n%c", "histogram:info:");
    assertEquals(expectedText, commandText);
  }

  @Test
  void testPrepareThumbnailFiles() throws MediaExtractionException, IOException {

    // Make the call
    final String url = "http://images.is.ed.ac.uk/MediaManager/srvr?mediafile=/Size3/UoEcar-4-NA/1007/0012127c.jpg";
    final String md5 = "6d27e9f0dcdbf33afc07d952cc5c2833";
    final List<ThumbnailWithSize> thumbnails = thumbnailGenerator.prepareThumbnailFiles(url);

    // Parse and verify result: there are two thumbnails with unique sizes.
    assertEquals(2, thumbnails.size());
    final Map<Integer, ThumbnailImpl> thumbnailMap = thumbnails.stream().collect(
        Collectors.toMap(ThumbnailWithSize::getImageSize, ThumbnailWithSize::getThumbnail));
    assertEquals(2, thumbnailMap.size());

    // Inspect the thumbnails and then close them.
    try (ThumbnailImpl thumbnail1 = thumbnailMap.get(200); ThumbnailImpl thumbnail2 = thumbnailMap
        .get(400)) {

      // Check the medium thumbnail including its size and name.
      assertNotNull(thumbnail1);
      assertEquals(md5 + "-MEDIUM", thumbnail1.getTargetName());
      assertFalse(thumbnail1.hasContent());
      assertEquals(0, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());

      // Check the large thumbnail including its size and name.
      assertNotNull(thumbnail2);
      assertEquals(md5 + "-LARGE", thumbnail2.getTargetName());
      assertFalse(thumbnail1.hasContent());
      assertEquals(0, thumbnail1.getContentSize());
      assertEquals(url, thumbnail1.getResourceUrl());

    }
  }
}
