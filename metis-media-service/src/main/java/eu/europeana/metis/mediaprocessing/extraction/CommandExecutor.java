package eu.europeana.metis.mediaprocessing.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class executes commands (like you would in a terminal). It imposes a maximum number of
 * processes that can perform IO at any given time.
 */
class CommandExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

  private final ExecutorService commandIOThreadPool;

  /**
   * Constructor.
   *
   * @param commandIOThreadPoolSize The maximum number of processes that can perform IO at any given
   * time.
   */
  CommandExecutor(int commandIOThreadPoolSize) {
    this.commandIOThreadPool = Executors.newFixedThreadPool(commandIOThreadPoolSize);
  }

  /**
   * Execute a command.
   *
   * @param command The command to execute, as a list of directives and parameters
   * @param redirectErrorStream Whether to return the contents of the error stream as part of the
   * command's output.
   * @return The output of the command as a list of lines.
   * @throws IOException In case a problem occurs.
   */
  List<String> execute(List<String> command, boolean redirectErrorStream) throws IOException {

    // Create process and start it.
    final Process process = new ProcessBuilder(command).redirectErrorStream(redirectErrorStream)
        .start();

    // Open error stream and log contents if any - use IO thread pool.
    if (!redirectErrorStream) {
      final Supplier<String> commandSupplier = () -> String.join(" ", command);
      commandIOThreadPool.execute(() -> readAndLogErrorStream(process, commandSupplier));
    }

    // Read process output into lines.
    try (InputStream in = process.getInputStream()) {
      return IOUtils.readLines(in, Charset.defaultCharset());
    }
  }

  private void readAndLogErrorStream(Process process, Supplier<String> command) {
    try (InputStream errorStream = process.getErrorStream()) {
      final String output = IOUtils.toString(errorStream, Charset.defaultCharset());
      if (!StringUtils.isBlank(output)) {
        LOGGER.warn("Command: [{}]\nerror output:\n{}", command.get(), output);
      }
    } catch (IOException e) {
      LOGGER.error("Error stream reading failed for command [{}]", command.get(), e);
    }
  }

  /**
   * Shuts down this command executor. Current tasks will be finished, but no new tasks will be
   * accepted.
   */
  void shutdown() {
    commandIOThreadPool.shutdown();
  }
}
