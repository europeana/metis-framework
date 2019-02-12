package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;
import eu.europeana.metis.mediaprocessing.extraction.CommandExecutor.ProcessFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandExecutorTest {

  private static final List<String> COMMAND_INPUT = Collections.singletonList("Command input");
  private static final List<String> COMMAND_OUTPUT = Collections.singletonList("Command output");
  private static final String ERROR_OUTPUT = "Error output";
  private static final Supplier<InputStream> COMMAND_OUTPUT_STREAM = () -> new ByteArrayInputStream(
      String.join("\n", COMMAND_OUTPUT).getBytes(Charset.defaultCharset()));
  private static final Supplier<InputStream> ERROR_OUTPUT_STREAM = () -> new ByteArrayInputStream(
      ERROR_OUTPUT.getBytes(Charset.defaultCharset()));
  private static final Supplier<InputStream> EMPTY_STREAM = () -> new ByteArrayInputStream(
      new byte[0]);


  private static ProcessFactory processFactory;
  private static Process process;
  private static CommandExecutor commandExecutor;

  @BeforeAll
  static void prepare() {
    process = mock(Process.class);
    processFactory = mock(ProcessFactory.class);
    commandExecutor = new CommandExecutor(1, processFactory);
  }

  @BeforeEach
  void resetMocks() throws IOException {
    reset(process, processFactory);
    doReturn(process).when(processFactory).createProcess(anyList(), anyBoolean());
  }

  @Test
  void testRegularCommand() throws CommandExecutionException, IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.execute(COMMAND_INPUT, false);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, false);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testRedirectedCommand() throws CommandExecutionException, IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.execute(COMMAND_INPUT, true);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, true);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testCommandWithOutputAndError() throws CommandExecutionException, IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(ERROR_OUTPUT_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.execute(COMMAND_INPUT, false);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, false);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }
}
