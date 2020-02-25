package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandExecutorTest {

  private static final List<String> COMMAND_INPUT = Collections.singletonList("Command input");
  private static final String COMMAND_OUTPUT = "Command output";
  private static final String ERROR_OUTPUT = "Error output";
  private static final Supplier<InputStream> COMMAND_OUTPUT_STREAM = () -> new ByteArrayInputStream(
      COMMAND_OUTPUT.getBytes(Charset.defaultCharset()));
  private static final Supplier<InputStream> ERROR_OUTPUT_STREAM = () -> new ByteArrayInputStream(
      ERROR_OUTPUT.getBytes(Charset.defaultCharset()));
  private static final Supplier<InputStream> EMPTY_STREAM = () -> new ByteArrayInputStream(
      new byte[0]);
  private static final int TIMEOUT = 60;

  private static ProcessFactory processFactory;
  private static Process process;
  private static CommandExecutor commandExecutor;

  @BeforeAll
  static void createMocks() {
    process = mock(Process.class);
    processFactory = mock(ProcessFactory.class);
    commandExecutor = spy(new CommandExecutor(TIMEOUT, processFactory));
  }

  @BeforeEach
  void resetMocks() throws IOException, InterruptedException {
    reset(process, processFactory, commandExecutor);
    doReturn(process).when(processFactory).createProcess(anyList(), anyBoolean());
    doReturn(true).when(process).waitFor(eq((long) TIMEOUT), eq(TimeUnit.SECONDS));
  }

  @Test
  void testRegularCommand() throws IOException, CommandExecutionException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final String result = commandExecutor.executeInternal(COMMAND_INPUT, false);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, false);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testRedirectedCommand() throws IOException, CommandExecutionException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final String result = commandExecutor.executeInternal(COMMAND_INPUT, true);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, true);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testCommandWithOutputAndError() throws IOException, CommandExecutionException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(ERROR_OUTPUT_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final String result = commandExecutor.executeInternal(COMMAND_INPUT, false);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, false);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testCommandWithError() {

    // Set up regular command
    doReturn(EMPTY_STREAM.get()).when(process).getInputStream();
    doReturn(ERROR_OUTPUT_STREAM.get()).when(process).getErrorStream();

    // Perform call
    assertThrows(IOException.class, () -> commandExecutor.executeInternal(COMMAND_INPUT, false));
  }

  @Test
  void testCommandWithTimeout() throws InterruptedException {

    // Set up timeout
    doReturn(false).when(process).waitFor(eq((long) TIMEOUT), eq(TimeUnit.SECONDS));

    // Perform call
    assertThrows(CommandExecutionException.class,
        () -> commandExecutor.executeInternal(COMMAND_INPUT, true));

    // Verify
    verify(process, times(1)).destroyForcibly();
  }

  @Test
  void testCommandWithInteruption() throws InterruptedException {

    // Set up timeout
    doThrow(new InterruptedException()).when(process)
        .waitFor(eq((long) TIMEOUT), eq(TimeUnit.SECONDS));

    // Perform call
    assertThrows(CommandExecutionException.class,
        () -> commandExecutor.executeInternal(COMMAND_INPUT, true));

    // Verify
    verify(process, times(1)).destroyForcibly();
  }

  @Test
  void testExecuteMethodHappyFlow() throws IOException, CommandExecutionException {

    // Set up the input and the output.
    final List<String> command = Collections.singletonList("command");
    final String result = "result";
    doReturn(result).when(commandExecutor).executeInternal(any(), anyBoolean());

    // Run with redirect and verify that the internal call was made.
    assertEquals(result, commandExecutor.execute(command, true));
    verify(commandExecutor, times(1)).executeInternal(command, true);
    verify(commandExecutor, times(1)).executeInternal(any(), anyBoolean());

    // Run without redirect and verify that the internal call was made.
    assertEquals(result, commandExecutor.execute(command, false));
    verify(commandExecutor, times(1)).executeInternal(command, false);
    verify(commandExecutor, times(2)).executeInternal(any(), anyBoolean());
  }

  @Test
  void testExecuteMethodWithExceptions() throws IOException, CommandExecutionException {

    // Define command
    final List<String> command = Collections.singletonList("command");

    // Test CommandExecutionException
    doThrow(CommandExecutionException.class).when(commandExecutor)
        .executeInternal(eq(command), eq(true));
    assertThrows(CommandExecutionException.class, () -> commandExecutor.execute(command, true));

    // Test IOException
    doThrow(IOException.class).when(commandExecutor).executeInternal(eq(command), eq(true));
    assertThrows(CommandExecutionException.class, () -> commandExecutor.execute(command, true));

    // Test RuntimeException
    doThrow(RuntimeException.class).when(commandExecutor).executeInternal(eq(command), eq(true));
    assertThrows(CommandExecutionException.class, () -> commandExecutor.execute(command, true));
  }
}
