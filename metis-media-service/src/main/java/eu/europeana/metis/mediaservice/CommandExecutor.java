package eu.europeana.metis.mediaservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CommandExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);
	
	private ExecutorService commandIOThreadPool = Executors.newFixedThreadPool(2);
	
	List<String> runCommand(List<String> command, boolean mergeError) throws IOException {
		return runCommand(command, mergeError, null);
	}
	
	List<String> runCommand(List<String> command, boolean mergeError, byte[] inputBytes) throws IOException {
		Process process = new ProcessBuilder(command).redirectErrorStream(mergeError).start();
		if (!mergeError) {
			commandIOThreadPool.execute(() -> {
				try (InputStream errorStream = process.getErrorStream()) {
					String output = IOUtils.toString(errorStream, Charset.defaultCharset());
					if (!StringUtils.isBlank(output))
						LOGGER.warn("Command: {}\nerror output:\n{}", command, output);
				} catch (IOException e) {
					LOGGER.error("Error stream reading faild for command " + command, e);
				}
			});
		}
		if (inputBytes != null) {
			commandIOThreadPool.execute(() -> {
				try (OutputStream processInput = process.getOutputStream()) {
					processInput.write(inputBytes);
				} catch (IOException e) {
					LOGGER.error("Pushing data to process input stream failed for command " + command, e);
				}
			});
		}
		try (InputStream in = process.getInputStream()) {
			return IOUtils.readLines(in, Charset.defaultCharset());
		}
	}
	
	void shutdown() {
		commandIOThreadPool.shutdown();
	}
}
