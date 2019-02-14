package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

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

  private static ExecutorService commandThreadPool;
  private static ProcessFactory processFactory;
  private static Process process;
  private static CommandExecutor commandExecutor;

  @BeforeAll
  static void prepare() {
    process = mock(Process.class);
    processFactory = mock(ProcessFactory.class);
    commandThreadPool = mock(ExecutorService.class);
    commandExecutor = spy(new CommandExecutor(commandThreadPool, processFactory));
  }

  @BeforeEach
  void resetMocks() throws IOException {
    reset(process, processFactory, commandThreadPool, commandExecutor);
    doReturn(process).when(processFactory).createProcess(anyList(), anyBoolean());
  }

  @Test
  void testRegularCommand() throws IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.executeInternal(COMMAND_INPUT, false);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, false);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testRedirectedCommand() throws IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(EMPTY_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.executeInternal(COMMAND_INPUT, true);

    // Verify
    verify(processFactory, times(1)).createProcess(COMMAND_INPUT, true);
    verifyNoMoreInteractions(processFactory);
    assertEquals(COMMAND_OUTPUT, result);
  }

  @Test
  void testCommandWithOutputAndError() throws IOException {

    // Set up regular command
    doReturn(COMMAND_OUTPUT_STREAM.get()).when(process).getInputStream();
    doReturn(ERROR_OUTPUT_STREAM.get()).when(process).getErrorStream();

    // Perform call
    final List<String> result = commandExecutor.executeInternal(COMMAND_INPUT, false);

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
  void testExecuteMethodHappyFlow() throws IOException, CommandExecutionException {

    // Set up the input and the output.
    final List<String> command = Collections.singletonList("command");
    final List<String> result = Collections.singletonList("result");
    doReturn(null).when(commandExecutor).executeInternal(any(), anyBoolean());

    // Stub the thread pool such that it executes the callable, but gives a certain result.
    doAnswer(invocation -> {
      Callable<?> callable = invocation.getArgument(0);
      callable.call();
      return CompletableFuture.completedFuture(result);
    }).when(commandThreadPool).submit(ArgumentMatchers.<Callable<?>>any());

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
  void testExecuteMethodWithExceptions() throws ExecutionException, InterruptedException {

    // Set up mocks
    final List<String> command = Collections.singletonList("command");
    final Future<Object> future = mock(Future.class);
    doReturn(future).when(commandThreadPool).submit(ArgumentMatchers.<Callable<?>>any());

    // Test execution exception
    doThrow(ExecutionException.class).when(future).get();
    assertThrows(CommandExecutionException.class, () -> commandExecutor.execute(command, true));

    // Test thread interruption
    doThrow(InterruptedException.class).when(future).get();
    assertThrows(CommandExecutionException.class, () -> commandExecutor.execute(command, true));
  }

  @Test
  void testClose() {
    commandExecutor.close();
    verify(commandThreadPool).shutdown();
  }
}
