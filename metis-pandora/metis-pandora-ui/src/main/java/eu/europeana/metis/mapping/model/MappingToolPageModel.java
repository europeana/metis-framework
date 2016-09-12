package eu.europeana.metis.mapping.model;

import eu.europeana.metis.mapping.molecules.controls.DropdownMenu;
import eu.europeana.metis.mapping.organisms.global.NavigationTop;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.util.MetisMappingUtil;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

/**
 * Stub
 * @author alena
 *
 */
public class MappingToolPageModel {
	
	/**
	 * The entire object model for the Mapping-To_EDM page.
	 */
	private Map<String, Object> modelMap = new HashMap<>();
	
	/**
	 * list of pairs "path"-"media" for css files.
	 */
	private List<Entry<String, String>> cssFiles;

	/**
	 * list of pairs "path"-"data_main" for js files.
	 */
	private List<Entry<String, String>> jsFiles;
	
	/**
	 * list of pairs "name"-"value" for js vars.
	 */
	private List<Entry<String, String>> jsVars;
	
	/**
	 * list of pairs "text"-"url" for breadcrumbs.
	 */
	private List<Entry<String, String>> breadcrumbs;

	public MappingToolPageModel() {
		initSimpleObjects();
	}
	
	public Map<String, Object> buildModel() {
		modelMap.put("breadcrumbs", buildBreadcrumbs());
		modelMap.put("action_menu", buildActionMenu());
		modelMap.put("css_files", buildCss());
		modelMap.put("js_files", buildJs());
		modelMap.put("js_vars", buildJsVars());
		modelMap.put("navigation", buildHeader());
		return modelMap;
	}
	
	public void initSimpleObjects() {
		//css files
		setCssFiles(new ArrayList<>());
		getCssFiles().add(new SimpleEntry<String, String>("https://europeana-styleguide-test.s3.amazonaws.com/css/pandora/screen.css", "all"));
		getCssFiles().add(new SimpleEntry<String, String>("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css", "all"));
		
		//js files
		setJsFiles(new ArrayList<>());
		getJsFiles().add(new SimpleEntry<String, String>("https://europeana-styleguide-test.s3.amazonaws.com/js/modules/require.js", 
				"https://europeana-styleguide-test.s3.amazonaws.com/js/modules/main/templates/main-pandora"));
		
		//js vars
		setJsVars(new ArrayList<>());
		getJsVars().add(new SimpleEntry<String, String>("pageName", "portal/index"));
		
		//breadcrumbs
		setBreadcrumbs(new ArrayList<>());
		getBreadcrumbs().add(new SimpleEntry<String, String>("Grandparent", "#"));
		getBreadcrumbs().add(new SimpleEntry<String, String>("Parent", "#"));
		getBreadcrumbs().add(new SimpleEntry<String, String>("Child", "#"));
		getBreadcrumbs().add(new SimpleEntry<String, String>("Grandchild", "#"));
	}
	
	private List<Map<String, String>> buildCss() {
		return MetisMappingUtil.buildSimplePairs(getCssFiles(), "path", "media");
	}
	
	private List<Map<String, String>> buildJs() {
		return MetisMappingUtil.buildSimplePairs(getJsFiles(), "path", "data_main");
	}
	
	private List<Map<String, String>> buildJsVars() {
		return MetisMappingUtil.buildSimplePairs(getJsVars(), "name", "value");
	}
	
	private List<Map<String, String>>  buildBreadcrumbs() {
		return MetisMappingUtil.buildSimplePairs(getBreadcrumbs(), "text", "url");
	}
	
	private Object buildActionMenu() {
		Map<String, Object> action_menu = new HashMap<>();
		List<Map<String, DropdownMenu>> sections = new ArrayList<>();
		for (DropdownMenu menu: buildMenus()) {
			Map<String, DropdownMenu> menuItem = new HashMap<>();
			menuItem.put("menu", menu);
			sections.add(menuItem);			
		}
		action_menu.put("sections", sections);
		action_menu.put("search_box", buildSearchBox());
		return action_menu;
	}
	
	private List<DropdownMenu> buildMenus() {
		List<DropdownMenu> menu = new ArrayList<>();
		
		DropdownMenu menu1 = new DropdownMenu("View", "Show", null, "object_furtheractions", "theme_select");
		menu1.addMenuItem("Mapping to EDM", "javascript:$('.dropdown-trigger').innerHTML = 'Mapping to EDM'", null, null, null);
		menu1.addMenuItem("Source Schema", "#", null, null, null);
		menu1.addMenuItem("Per Item", "#", null, null, null);
		menu1.addMenuItem("XSLT Editor", "#", null, null, null);
		menu.add(menu1);
			
		DropdownMenu menu2 = new DropdownMenu("Browse", " Filter by:", null, "object_furtheractions2", "theme_select");
		menu2.addMenuItem("dc:title", "#", null, null, null);
		menu2.addMenuItem("@xml:lang", "#", null, null, null);
		menu2.addMenuItem("dc:creator", "#", null, null, null);
		menu2.addMenuItem("@xml:lang", "#", null, null, null);
		menu2.addMenuItem("dc:language", "#", null, null, null);
		menu2.addMenuItem("@xml:lang", "#", null, null, null);
		menu2.addMenuItem("dc:subject", "#", null, null, null);
		menu2.addMenuItem("@xml:lang", "#", null, null, null);
		menu2.addMenuItem("dc:identifier", "#", null, null, null);
		menu.add(menu2);
		
		return menu;
	}
	
	private Map<String, String> buildSearchBox() {
		Map<String, String> searchBox = new HashMap<>();
		searchBox.put("search_box_legend", "Search");
		searchBox.put("search_box_label", "Search Label");
		searchBox.put("search_box_hidden", "Search Hidden");
		return searchBox;
	}
	
	/**
	 * The header "../organisms/global/header_v2" contains only the "navigation_top_accessible"-component, 
	 * so we can create the model using only NavigationTop class
	 * @return
	 */
	private NavigationTop buildHeader() {
		NavigationTop header = new NavigationTop("#", "Return to Home", false);
		header.addNextPrev("next_url_here", "prev_url_here", "results_url_here");
		
		List<NavigationTopMenu> items = new ArrayList<>();
		items.add(new NavigationTopMenu("Home", "#", false, null, null, null, null, false));
		
		List<NavigationTopMenu> submenu1 = Arrays.asList(
			new NavigationTopMenu("Submenu Item 11", "#"),
			new NavigationTopMenu("Submenu Item 12", "#"),
			new NavigationTopMenu("Submenu Item 13", "#"));
		items.add(new NavigationTopMenu("Menu Item 1", "javascript:alert('Hello')", submenu1));
		
		List<NavigationTopMenu> submenu2 = Arrays.asList(
				new NavigationTopMenu("Submenu Item 21", "false", null, null, true, null, null, null),
				new NavigationTopMenu("Submenu Item 22", "#", null, null, null, true, null, null),
				new NavigationTopMenu("Submenu Item 23", "#"),
				new NavigationTopMenu(true),
				new NavigationTopMenu("Submenu Item 24", "#"));
		items.add(new NavigationTopMenu("Menu Item 2", "#", submenu2));
		
		items.add(new NavigationTopMenu("", "#", false, null, null, null, null, false));
		header.addGlobal(false, true, "#", "Europeana Pandora", "main_menu", items);		
		return header;
	}
	
	///////////////////////////////////////////////////////////
	public Map<String, Object> getModelMap() {
		return modelMap;
	}
	public void setModelMap(Map<String, Object> modelMap) {
		this.modelMap = modelMap;
	}
	public List<Entry<String, String>> getCssFiles() {
		return cssFiles;
	}
	public void setCssFiles(List<Entry<String, String>> cssFiles) {
		this.cssFiles = cssFiles;
	}
	public List<Entry<String, String>> getJsFiles() {
		return jsFiles;
	}
	public void setJsFiles(List<Entry<String, String>> jsFiles) {
		this.jsFiles = jsFiles;
	}
	public List<Entry<String, String>> getJsVars() {
		return jsVars;
	}
	public void setJsVars(List<Entry<String, String>> jsVars) {
		this.jsVars = jsVars;
	}
	public List<Entry<String, String>> getBreadcrumbs() {
		return breadcrumbs;
	}
	public void setBreadcrumbs(List<Entry<String, String>> breadcrumbs) {
		this.breadcrumbs = breadcrumbs;
	}	
}
