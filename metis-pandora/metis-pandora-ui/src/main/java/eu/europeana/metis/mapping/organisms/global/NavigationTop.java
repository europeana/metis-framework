package eu.europeana.metis.mapping.organisms.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author alena
 *
 */
public class NavigationTop {
	
	 private String home_url;
	 
     private String home_text;
     
     private Map<String, String> next_prev;
     
     private Boolean footer;
     
     private Map<String, Object> global;

	public NavigationTop(String home_url, String home_text, Boolean footer) {
		super();
		this.home_url = home_url;
		this.home_text = home_text;
		this.next_prev = new HashMap<>();
		this.footer = footer;
		this.global = new HashMap<>();
	}
	
	public void addNextPrev(String next_url, String prev_url, String results_url) {
		next_prev.put("next_url", next_url);
		next_prev.put("prev_url", prev_url);
		next_prev.put("results_url", results_url);
	}
	
	public void addGlobal(Boolean search_active, Boolean settings_active, String logoUrl, String logoText, String menuId, List<NavigationTopMenu> items) {
		Map<String, Object> options = new HashMap<>();
		options.put("search_active", search_active);
		options.put("settings_active", settings_active);
		this.global.put("options", options);
		
		Map<String, String> logo = new HashMap<>();
		logo.put("url", logoUrl);
		logo.put("text", logoText);
		this.global.put("logo", logo);
		
		Map<String, Object> primary_nav = new HashMap<>();
		primary_nav.put("items", items);
		primary_nav.put("menu_id", menuId);
		this.global.put("primary_nav", primary_nav);

		Map<String,Object> utilityNav = new HashMap<>();
		utilityNav.put("menu_id", "settings-menu");
		utilityNav.put("style_modifier", "caret-right");
		utilityNav.put("tabindex", "6");
		Map<String, String> utilityNavItems = new HashMap<>();
		utilityNavItems.put("url", "#");
		utilityNavItems.put("text", "Wiki");
		utilityNavItems.put("icon", "settings");
		utilityNav.put("items", utilityNavItems);
		this.global.put("utility_nav", utilityNav);
	}

	public String getHome_url() {
		return home_url;
	}

	public void setHome_url(String home_url) {
		this.home_url = home_url;
	}

	public String getHome_text() {
		return home_text;
	}

	public void setHome_text(String home_text) {
		this.home_text = home_text;
	}
	
	public Map<String, String> getNext_prev() {
		return next_prev;
	}
	
	public void setNext_prev(Map<String, String> next_prev) {
		this.next_prev = next_prev;
	}

	public Boolean getFooter() {
		return footer;
	}

	public void setFooter(Boolean footer) {
		this.footer = footer;
	}
	
	public Map<String, Object> getGlobal() {
		return global;
	}
	
	public void setGlobal(Map<String, Object> global) {
		this.global = global;
	}
}
