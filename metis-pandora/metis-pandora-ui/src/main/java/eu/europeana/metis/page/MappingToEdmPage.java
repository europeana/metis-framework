package eu.europeana.metis.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.mapping.molecules.controls.DropdownMenu;
import eu.europeana.metis.mapping.organisms.global.NavigationTop;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.organisms.pandora.Mapping_card;
import eu.europeana.metis.mapping.util.MappingCardStub;

/**
 * 
 * @author alena
 *
 */
public class MappingToEdmPage extends MetisPage {
	
	//FIXME
	@Override
	public Byte resolveCurrentPage() {
		return null;
	}
	
	@Override
	public void addPageContent(Map<String, Object> model) {
		model.put("action_menu", buildActionMenu());
		model.put("mapping_card", buildMappingCard());
	}
	
	//FIXME mapping card currently is generated with a stub data.
	private Mapping_card buildMappingCard() {
		return MappingCardStub.buildMappingCardModel();
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
		menu1.addMenuItem("Mapping to EDM", "javascript:$('.dropdown-trigger').innerHTML = 'Mapping to EDM'");
		menu1.addMenuItem("Source Schema", "#");
		menu1.addMenuItem("Per Item", "#");
		menu1.addMenuItem("XSLT Editor", "#");
		menu.add(menu1);
			
		DropdownMenu menu2 = new DropdownMenu("Browse", " Filter by:", null, "object_furtheractions2", "theme_select");
		menu2.addMenuItem("dc:title", "#");
		menu2.addMenuItem("@xml:lang", "#");
		menu2.addMenuItem("dc:creator", "#");
		menu2.addMenuItem("@xml:lang", "#");
		menu2.addMenuItem("dc:language", "#");
		menu2.addMenuItem("@xml:lang", "#");
		menu2.addMenuItem("dc:subject", "#");
		menu2.addMenuItem("@xml:lang", "#");
		menu2.addMenuItem("dc:identifier", "#");
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
	
	@Override
	public List<NavigationTopMenu> buildUtilityNavigation() {
		return Arrays.asList(
				new NavigationTopMenu("Register", "/register", false),
				new NavigationTopMenu("Login", "/login", true));
	}
}
