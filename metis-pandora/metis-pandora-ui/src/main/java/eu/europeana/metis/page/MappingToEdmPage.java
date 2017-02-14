package eu.europeana.metis.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.controller.MetisPageController;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.molecules.controls.DropdownMenu;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.organisms.pandora.Mapping_card;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.util.MappingCardStub;
import eu.europeana.metis.service.MappingService;

/**
 * 
 * @author alena
 *
 */
public class MappingToEdmPage extends MetisPage {
	final static Logger logger = Logger.getLogger(MappingToEdmPage.class);
	
	private MappingService mappingService;
	
	private static final int DEFAULT_COUNT = 20;
	
	private static final int DEFAULT_OFFSET = 0;
	
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
	public List<Mapping_card> buildMappingCard() {
//		return MappingCardStub.buildMappingCardModel();
		List<Mapping_card> displayList = new ArrayList<>();
		
		Mapping testMapping = getMappingService().getByName(null);
		List<Attribute> attributes = testMapping.getMappings().getAttributes();
		List<Element> elements = testMapping.getMappings().getElements();
		addChildFields(displayList, attributes, elements, 0);
//		System.out.println(MetisMappingUtil.toJson(displayList));
		return displayList;
	}
	
	private void addChildFields(List<Mapping_card> displayList, List<Attribute> attributes,  List<Element> elements, int depth) {
		if (attributes != null && !attributes.isEmpty()) {
			for (Attribute attribute : attributes) {
				Statistics statistics = getMappingService().getStatisticsForField(attribute, null);
				if (statistics != null) {
					displayList.add(new Mapping_card(attribute, statistics, DEFAULT_OFFSET, DEFAULT_COUNT, depth));	
					logger.info("*** FIELD IS ADDED: " + attribute.getPrefix() + ":" + attribute.getName() + "; DEPTH: "+ depth + " ***");				
				} else {
					logger.info("*** FIELD IS NOT ADDED: " + attribute.getPrefix() + ":" + attribute.getName() + "; ***");
				}
			}			
		}
		if (elements != null && !elements.isEmpty()) {
			for (Element element : elements) {
				Statistics statistics = getMappingService().getStatisticsForField(element, null);
				if (element.isHasMapping()) {
					displayList.add(new Mapping_card(element, statistics, DEFAULT_OFFSET, DEFAULT_COUNT, depth));			
				} else {
					logger.info("*** FIELD IS NOT ADDED: " + element.getPrefix() + ":" + element.getName() + "; ***");
				}
				addChildFields(displayList, element.getAttributes(), element.getElements(), depth + 1);
			}			
		}
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

	public MappingService getMappingService() {
		return mappingService;
	}

	public void setMappingService(MappingService mappingService) {
		this.mappingService = mappingService;
	}
}
