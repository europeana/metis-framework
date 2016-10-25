package eu.europeana.metis.mapping.organisms.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationTopMenu {
	
	private String text;
	
	private String url;
	
	private Boolean is_current;
	
	private Boolean is_divider;
	
	private Boolean subtitle;
	
	private Boolean message;
	
	private Object submenu;

	public NavigationTopMenu(String text, String url, Boolean is_current, Boolean is_divider, Boolean subtitle, Boolean message, List<NavigationTopMenu> items, Boolean hasSubmenu) {
		this.text = text;
		this.url = url;
		this.is_current = is_current;
		this.is_divider = is_divider;
		this.subtitle = subtitle;
		this.message = message;
		if (hasSubmenu != null && !hasSubmenu) {
			this.submenu = false;
		} else if (items != null) {
			Map<String, List<NavigationTopMenu>> subMenu = new HashMap<String, List<NavigationTopMenu>>();
			subMenu.put("items", items);
			this.submenu = subMenu;
		}
	}
	
	public NavigationTopMenu(String text, String url, Boolean is_current) {
		this(text, url, is_current, null, null, null, null, null);
	}
	
	public NavigationTopMenu(String text, String url, Boolean is_current, List<NavigationTopMenu> submenu) {
		this(text, url, is_current, null, null, null, submenu, null);
	}
	
	public NavigationTopMenu(Boolean is_divider) {
		this(null, null, null, is_divider, null, null, null, null);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIs_current() {
		return is_current;
	}

	public void setIs_current(Boolean is_current) {
		this.is_current = is_current;
	}

	public Boolean getIs_divider() {
		return is_divider;
	}

	public void setIs_divider(Boolean is_divider) {
		this.is_divider = is_divider;
	}

	public Boolean getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(Boolean subtitle) {
		this.subtitle = subtitle;
	}

	public Boolean getMessage() {
		return message;
	}

	public void setMessage(Boolean message) {
		this.message = message;
	}

	public Object getSumbenu() {
		return submenu;
	}

	public void setSubmenu(Object submenu) {
		this.submenu = submenu;
	}
}
