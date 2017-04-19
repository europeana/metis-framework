package eu.europeana.metis.mapping.atoms;

import java.util.HashMap;
import java.util.Map;

public class WelcomeMessage {
	
	private Map<String, String> welcome_message;

	public WelcomeMessage(String text_first, String user_name,String text_end) {
		welcome_message = new HashMap<>();
		welcome_message.put(text_first, "text_first");
		welcome_message.put(user_name, "user_name");
		welcome_message.put(text_end, "text_end");
	}
}
