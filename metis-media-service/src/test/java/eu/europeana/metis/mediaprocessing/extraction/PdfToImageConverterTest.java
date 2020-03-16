package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PdfToImageConverterTest {

  private static final String GHOST_SCRIPT_COMMAND = "gs command";

  private static CommandExecutor commandExecutor;
  private static PdfToImageConverter pdfToImageConverter;

  @BeforeAll
  static void createMocks() {
    commandExecutor = mock(CommandExecutor.class);
    pdfToImageConverter = spy(new PdfToImageConverter(commandExecutor, GHOST_SCRIPT_COMMAND));
  }

  @BeforeEach
  void resetMocks() {
    reset(commandExecutor, pdfToImageConverter);
  }

  @Test
  void testDiscoverGhostScriptCommand() throws MediaProcessorException {

    // ghostscript command
    final String ghostScriptCommand = "gs";
    final List<String> ghostScriptVersionCommand = Arrays.asList(ghostScriptCommand, "--version");

    // Test right version
    doReturn("9.26").when(commandExecutor)
            .execute(eq(ghostScriptVersionCommand), eq(true), any());
    assertEquals(ghostScriptCommand,
            PdfToImageConverter.discoverGhostScriptCommand(commandExecutor));

    // Test other commands
    doReturn("8.26").when(commandExecutor)
            .execute(eq(ghostScriptVersionCommand), eq(true), any());
    assertThrows(MediaProcessorException.class,
            () -> PdfToImageConverter.discoverGhostScriptCommand(commandExecutor));
    doReturn("Other command").when(commandExecutor)
            .execute(eq(ghostScriptVersionCommand), eq(true), any());
    assertThrows(MediaProcessorException.class,
            () -> PdfToImageConverter.discoverGhostScriptCommand(commandExecutor));

    // Test command execution exception
    doThrow(new MediaProcessorException("", null)).when(commandExecutor)
            .execute(eq(ghostScriptVersionCommand), eq(true), any());
    assertThrows(MediaProcessorException.class,
            () -> PdfToImageConverter.discoverGhostScriptCommand(commandExecutor));
  }

  @Test
  void testCreatePdfConversionCommand() {

    // Create input and output files
    final Path inputFile = mock(Path.class);
    doReturn(inputFile).when(inputFile).toAbsolutePath();
    doReturn("input_file").when(inputFile).toString();
    final Path outputFile = mock(Path.class);
    doReturn(outputFile).when(outputFile).toAbsolutePath();
    doReturn("output_file").when(outputFile).toString();

    // Get the command
    final List<String> expectedCommand = Arrays.asList(GHOST_SCRIPT_COMMAND,
            "-q",
            "-dQUIET",
            "-dSAFER",
            "-dBATCH",
            "-dNOPAUSE",
            "-dNOPROMPT",
            "-dMaxBitmap=500000000",
            "-dAlignToPixels=0",
            "-dGridFitTT=2",
            "-sDEVICE=pngalpha",
            "-dTextAlphaBits=4",
            "-dGraphicsAlphaBits=4",
            "-r72x72",
            "-dFirstPage=1",
            "-dLastPage=1",
            "-sOutputFile=" + outputFile.toString(),
            "-f" + inputFile.toString());

    // Execute the command
    assertEquals(expectedCommand,
            pdfToImageConverter.createPdfConversionCommand(inputFile, outputFile));
  }

  @Test
  void testConvertToPdf() throws MediaExtractionException {

    // Create input and output files
    final Path inputFile = mock(Path.class);
    final Path outputFile = mock(Path.class);

    // Mock
    doReturn(outputFile).when(pdfToImageConverter).createPdfImageFile();
    final List<String> command = Collections.singletonList("command");
    doReturn(command).when(pdfToImageConverter).createPdfConversionCommand(inputFile, outputFile);
    doReturn("").when(commandExecutor).execute(eq(command), eq(false), any());

    // Execute happy flow
    assertEquals(outputFile, pdfToImageConverter.convertToPdf(inputFile));

    // Test for empty input
    assertThrows(MediaExtractionException.class, ()->pdfToImageConverter.convertToPdf(null));

    // Test for problem creating the file
    doThrow(MediaExtractionException.class).when(pdfToImageConverter).createPdfImageFile();
    assertThrows(MediaExtractionException.class, ()->pdfToImageConverter.convertToPdf(inputFile));
  }
}
