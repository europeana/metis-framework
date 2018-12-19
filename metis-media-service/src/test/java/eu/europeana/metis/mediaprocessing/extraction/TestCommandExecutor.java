package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestCommandExecutor {
	
	@Test
	public void cdCommand() throws IOException {
		CommandExecutor c = new CommandExecutor(2);
		List<String> lines = c.execute(Collections.singletonList("java"), true);
		assertNotNull(lines);
	}
}
