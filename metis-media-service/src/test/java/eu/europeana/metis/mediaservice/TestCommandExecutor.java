package eu.europeana.metis.mediaservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestCommandExecutor {
	
	@Test
	public void cdCommand() throws IOException {
		CommandExecutor c = new CommandExecutor();
		List<String> lines = c.runCommand(Arrays.asList("java"), true);
		assertNotNull(lines);
	}
}
