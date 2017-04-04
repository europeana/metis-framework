package eu.europeana.metis.mapping.molecules.pandora;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.europeana.metis.mapping.util.MetisMappingUtil;

/**
 * Java model representing a field value component: "/molecules/pandora/field-value".
 * @author alena
 *
 */
public class FieldValue {
	
	private String object_id;
	
	private List<Map<String, String>> tooltip;
	
	private long occurence;
	
	private static final String TOOLTIP_TEXT = "tooltip_text";
	
	private static final String TOOLTIPPED_TEXT = "tooltipped_text";
	
	private static final int limit = 50;
	
	//TODO Add field value status after Eduardo adds it to the model

	public FieldValue(String object_id, String tooltip_text, long occurence) {
		this.object_id = object_id;
		this.occurence = occurence;
		String tooltipped_text = tooltip_text == null ? null : tooltip_text.length() <= limit ? tooltip_text : tooltip_text.substring(0, limit);
		if (tooltip_text.length() > limit) {
			tooltipped_text = tooltipped_text.concat("...");
		}
		this.tooltip = MetisMappingUtil.buildSimplePairs(Arrays.asList(new AbstractMap.SimpleEntry<>(tooltipped_text, tooltip_text)), TOOLTIPPED_TEXT, TOOLTIP_TEXT);
	}

	public String getObject_id() {
		return object_id;
	}

	public void setObject_id(String object_id) {
		this.object_id = object_id;
	}

	public List<Map<String, String>> getTooltip() {
		return tooltip;
	}

	public void setTooltip(List<Map<String, String>> tooltip) {
		this.tooltip = tooltip;
	}

	public long getOccurence() {
		return occurence;
	}

	public void setOccurence(long occurence) {
		this.occurence = occurence;
	}
}
