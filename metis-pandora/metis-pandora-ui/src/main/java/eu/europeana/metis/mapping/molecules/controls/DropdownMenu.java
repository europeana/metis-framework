package eu.europeana.metis.mapping.molecules.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author alena
 *
 */
public class DropdownMenu {

	private String style_modifier;

	private String menu_id;

	private String button_title;

	private String menu_title;
	
	private List<Map<String, Object>> items;

	public DropdownMenu(String button_title, String menu_title, List<Map<String, Object>> items, String menu_id, String style_modifier) {
		this.button_title = button_title;
		this.menu_title = menu_title;
		this.items = items != null ? items : new ArrayList<>();
		this.menu_id = menu_id;
		this.style_modifier = style_modifier;
	}
	
	public void addMenuItem(String text, String url, String subtitle, Boolean calltoaction, Boolean divider) {
		Map<String, Object> item = new HashMap<>();
		item.put("subtitle", subtitle);
		item.put("url", url);
		item.put("calltoaction", calltoaction);
		item.put("text", text);
		item.put("divider", divider);
		items.add(item);
	}

	public String getMenu_id() {
		return menu_id;
	}

	public void setMenu_id(String menu_id) {
		this.menu_id = menu_id;
	}

	public String getStyle_modifier() {
		return style_modifier;
	}

	public void setStyle_modifier(String style_modifier) {
		this.style_modifier = style_modifier;
	}

	public String getButton_title() {
		return button_title;
	}

	public void setButton_title(String button_title) {
		this.button_title = button_title;
	}

	public String getMenu_title() {
		return menu_title;
	}

	public void setMenu_title(String menu_title) {
		this.menu_title = menu_title;
	}
	
	public List<Map<String, Object>> getItems() {
		return items;
	}
	
	public void setItems(List<Map<String, Object>> items) {
		this.items = items;
	}
}
