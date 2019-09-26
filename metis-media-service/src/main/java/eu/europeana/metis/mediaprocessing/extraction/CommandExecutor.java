package eu.europeana.metis.mediaprocessing.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.mediaprocessing.exception.CommandExecutionException;

/**
 * This class executes commands (like you would in a terminal). It imposes a maximum number of
 * processes that can perform command-line IO at any given time.
 * <p>The command provided is sanitized before executed based on a predefined regex, for safety. In
 * case of an invalid command an exception will be thrown.</p>
 */
class CommandExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

  private final ProcessFactory processFactory;

  private final int commandTimeout;

  /**
   * Constructor.
   *
   * @param commandTimeout The maximum amount of time, in seconds, a command is allowed to take
   * before it is forcibly destroyed (i.e. cancelled).
   */
  CommandExecutor(int commandTimeout) {
    this(commandTimeout, (command, redirectErrorStream) -> new ProcessBuilder(command)
        .redirectErrorStream(redirectErrorStream).start());
  }

  /**
   * Constructor.
   *
   * @param commandTimeout The maximum amount of time, in seconds, a command is allowed to take
   * before it is forcibly destroyed (i.e. cancelled).
   * @param processFactory A function that, given a command and whether to redirect the error
   * stream, creates a {@link Process} for executing that command.
   */
  CommandExecutor(int commandTimeout, ProcessFactory processFactory) {
    this.commandTimeout = commandTimeout;
    this.processFactory = processFactory;
  }
  
  /**
   * Execute a command.
   *
   * @param command The command to execute, as a list of directives and parameters
   * @param redirectErrorStream Whether to return the contents of the error stream as part of the
   * command's output. If this is false, and there is error output but no regular output, an
   * exception will be thrown.
   * @return The output of the command as a list of lines.
   * @throws CommandExecutionException In case a problem occurs.
   */
  List<String> execute(List<String> command, boolean redirectErrorStream)
      throws CommandExecutionException {
    try {
      return executeInternal(command, redirectErrorStream);
    } catch (IOException | RuntimeException e) {
      throw new CommandExecutionException("Problem while executing command.", e);
    }
  }

  List<String> executeInternal(List<String> command, boolean redirectErrorStream)
      throws IOException, CommandExecutionException {

    // Create process and start it.
    final Process process = processFactory.createProcess(command, redirectErrorStream);

    // Wait for the process to finish (or the time-out to elapse).
    try {
      if (!process.waitFor(commandTimeout, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new CommandExecutionException("The process did not terminate within the timeout of " +
            commandTimeout + " seconds. It was forcibly destroyed.");
      }
    } catch (InterruptedException e) {
      process.destroyForcibly();
      Thread.currentThread().interrupt();
      throw new CommandExecutionException("Process was interrupted.", e);
    }

    // Open error stream and read it.
    final String error;
    if (redirectErrorStream) {
      error = null;
    } else {
      try (InputStream errorStream = process.getErrorStream()) {
        final String errorStreamContents = IOUtils.toString(errorStream, Charset.defaultCharset());
        error = StringUtils.isBlank(errorStreamContents) ? null : errorStreamContents;
      }
    }

    // Read process output into lines.
    final List<String> result;
    try (InputStream in = process.getInputStream()) {
      result = IOUtils.readLines(in, Charset.defaultCharset());
    }

    // If there is no regular output but there is error output, throw an exception.
    if (error != null) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Command presented with error:\nCommand: [{}]\nError: {}",
            String.join(" ", command), error);
      }
      if (result.isEmpty()) {
        throw new IOException("External process returned error content:\n" + error);
      }
    }

    // Else return the result.
    return result;
  }

  /**
   * Implementations of this class can create {@link Process} instances.
   */
  public interface ProcessFactory {

    /**
     * Create a {@link Process} instance.
     *
     * @param command The command to execute.
     * @param redirectErrorStream Whether to return the contents of the error stream as part of the
     * command's output.
     * @return The process.
     * @throws IOException In case a problem occurs creating the process.
     */
    Process createProcess(List<String> command, boolean redirectErrorStream) throws IOException;
  }
}
