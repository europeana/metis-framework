package eu.europeana.metis.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.config.NavigationPaths;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.organisms.pandora.Mapping_card;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.service.MappingService;
import eu.europeana.metis.templates.Global;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.Logo;
import eu.europeana.metis.templates.MenuItem;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.Navigation;
import eu.europeana.metis.templates.NextPrev;
import eu.europeana.metis.templates.Options;
import eu.europeana.metis.templates.PrimaryNav;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import eu.europeana.metis.templates.UtilityNav;
import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import eu.europeana.metis.templates.page.mappingtoedm.ActionMenu;
import eu.europeana.metis.templates.page.mappingtoedm.Dropdown;
import eu.europeana.metis.templates.page.mappingtoedm.FieldValueCell;
import eu.europeana.metis.templates.page.mappingtoedm.MappingCard;
import eu.europeana.metis.templates.page.mappingtoedm.MetisMappingToEdmPageModel;
import eu.europeana.metis.templates.page.mappingtoedm.SearchBox;
import eu.europeana.metis.templates.page.mappingtoedm.Section;
import eu.europeana.metis.templates.page.mappingtoedm.SectionMenu;
import eu.europeana.metis.templates.page.mappingtoedm.Tooltip;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingToEdmPage extends MetisPage {

  private final Logger LOGGER = LoggerFactory.getLogger(MappingToEdmPage.class);

  private MappingService mappingService;

  private static final int DEFAULT_COUNT = 20;

  private static final int DEFAULT_OFFSET = 0;

  private UserDTO userDTO;

  public MappingToEdmPage(MetisuiConfig config) {
    super(config);
  }

//	@Override
//	public void addPageContent(Map<String, Object> model) {
//		model.put("action_menu", buildActionMenu());
//		model.put("mapping_card", buildMappingCard());
//	}

  @Override
  public List<JsVar> resolveJsVars() {
    JsVar jsVar = new JsVar();
    jsVar.setName("pageName");
    jsVar.setValue("metisMappingPage");
    return java.util.Collections.singletonList(jsVar);
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
    LOGGER.info("*** MAPPING IS BUILT! ***");
    return displayList;
  }

  private void addChildFields(List<Mapping_card> displayList, List<Attribute> attributes,
      List<Element> elements, int depth) {
    if (attributes != null && !attributes.isEmpty()) {
      for (Attribute attribute : attributes) {
        Statistics statistics = getMappingService().getStatisticsForField(attribute, null);
        if (statistics != null) {
          displayList
              .add(new Mapping_card(attribute, statistics, DEFAULT_OFFSET, DEFAULT_COUNT, depth));
          LOGGER.info("*** ATTRIBUTE IS ADDED: " + attribute.getPrefix() + ":" + attribute.getName()
              + "; DEPTH: " + depth + " ***");
        }
      }
    }
    if (elements != null && !elements.isEmpty()) {
      for (Element element : elements) {
        Statistics statistics = getMappingService().getStatisticsForField(element, null);
        if (element.isHasMapping()) {
          displayList
              .add(new Mapping_card(element, statistics, DEFAULT_OFFSET, DEFAULT_COUNT, depth));
          LOGGER.info(
              "*** ELEMENT IS ADDED: " + element.getPrefix() + ":" + element.getName() + "; DEPTH: "
                  + depth + " ***");
        }
        addChildFields(displayList, element.getAttributes(), element.getElements(), depth + 1);
      }
    }
  }

//  private Object buildActionMenu() {
//    Map<String, Object> action_menu = new HashMap<>();
//    List<Map<String, DropdownMenu>> sections = new ArrayList<>();
//    for (DropdownMenu menu : buildMenus()) {
//      Map<String, DropdownMenu> menuItem = new HashMap<>();
//      menuItem.put("menu", menu);
//      sections.add(menuItem);
//    }
//    action_menu.put("sections", sections);
//    action_menu.put("search_box", buildSearchBox());
//    return action_menu;
//  }

//  private List<DropdownMenu> buildMenus() {
//    List<DropdownMenu> menu = new ArrayList<>();
//
//    DropdownMenu menu1 = new DropdownMenu("View", "Show", null, "object_furtheractions",
//        "theme_select");
//    menu1.addMenuItem("Mapping to EDM",
//        "javascript:$('.dropdown-trigger').innerHTML = 'Mapping to EDM'");
//    menu1.addMenuItem("Source Schema", "#");
//    menu1.addMenuItem("Per Item", "#");
//    menu1.addMenuItem("XSLT Editor", "#");
//    menu.add(menu1);
//
//    DropdownMenu menu2 = new DropdownMenu("Browse", " Filter by:", null, "object_furtheractions2",
//        "theme_select");
//    menu2.addMenuItem("dc:title", "#");
//    menu2.addMenuItem("@xml:lang", "#");
//    menu2.addMenuItem("dc:creator", "#");
//    menu2.addMenuItem("@xml:lang", "#");
//    menu2.addMenuItem("dc:language", "#");
//    menu2.addMenuItem("@xml:lang", "#");
//    menu2.addMenuItem("dc:subject", "#");
//    menu2.addMenuItem("@xml:lang", "#");
//    menu2.addMenuItem("dc:identifier", "#");
//    menu.add(menu2);
//
//    return menu;
//  }

//  private Map<String, String> buildSearchBox() {
//    Map<String, String> searchBox = new HashMap<>();
//    searchBox.put("search_box_legend", "Search");
//    searchBox.put("search_box_label", "Search Label");
//    searchBox.put("search_box_hidden", "Search Hidden");
//    return searchBox;
//  }

  public MappingService getMappingService() {
    return mappingService;
  }

  public void setMappingService(MappingService mappingService) {
    this.mappingService = mappingService;
  }

  @Override
  public List<Breadcrumb> resolveBreadcrumbs() {
    Breadcrumb breadcrumb1 = new Breadcrumb();
    Breadcrumb breadcrumb2 = new Breadcrumb();
    Breadcrumb breadcrumb3 = new Breadcrumb();
    Breadcrumb breadcrumb4 = new Breadcrumb();
    breadcrumb1.setText("Grandparent");
    breadcrumb1.setUrl("#");
    breadcrumb2.setText("Parent");
    breadcrumb2.setUrl("#");
    breadcrumb3.setText("Child");
    breadcrumb3.setUrl("#");
    breadcrumb4.setText("Grandchild");
    breadcrumb4.setUrl("#");
    return Arrays.asList(breadcrumb1, breadcrumb2, breadcrumb3, breadcrumb4);
  }

  @Override
  public Map<String, Object> buildModel() {
    MetisMappingToEdmPageModel metisMappingToEdmPageModel = new MetisMappingToEdmPageModel();
    metisMappingToEdmPageModel.setIsJava(true);
    metisMappingToEdmPageModel.setCssFiles(resolveCssFiles());
    metisMappingToEdmPageModel.setJsFiles(resolveJsFiles());
    metisMappingToEdmPageModel.setPageTitle("Pandora");
    metisMappingToEdmPageModel.setBreadcrumbs(resolveBreadcrumbs());
    metisMappingToEdmPageModel.setMetisHeader(createMetisHeader());
    metisMappingToEdmPageModel.setActionMenu(createActionMenu());
    metisMappingToEdmPageModel.setMappingCard(createMappingCard());

    ObjectMapper m = new ObjectMapper();
    Map<String, Object> modelMap = m.convertValue(metisMappingToEdmPageModel, Map.class);
    return modelMap;
  }

  private MappingCard createMappingCard()
  {
    MappingCard mappingCard = new MappingCard();
    mappingCard.setName("about");
    mappingCard.setObjectId("mapping_card_01");
    mappingCard.setPrefix("@rdf");

    Dropdown dropdown = new Dropdown();
    dropdown.setMenuTitle("Set item as:");
    dropdown.setButtonTitle(" ");
    dropdown.setMenuId("mapping-card-dropdown");
    dropdown.setStyleModifier("theme_select");
    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    SubmenuItem submenuItem3 = new SubmenuItem();
    SubmenuItem submenuItem4 = new SubmenuItem();
    SubmenuItem submenuItem5 = new SubmenuItem();
    submenuItem1.setText("Valid");
    submenuItem1.setUrl("#");
    submenuItem2.setText("Suspicious");
    submenuItem2.setUrl("#");
    submenuItem3.setText("Invalid");
    submenuItem3.setUrl("#");
    submenuItem4.setDivider(true);
    submenuItem4.setUrl("#");
    submenuItem5.setText("Remove all marks");
    submenuItem5.setUrl("#");

    dropdown.setItems(Collections.newArrayList(submenuItem1, submenuItem2, submenuItem3, submenuItem4, submenuItem5));
    mappingCard.setDropdown(dropdown);

    //Build the mocked fields
    Mapping testMapping = getMappingService().getByName(null);
    List<Attribute> attributes = testMapping.getMappings().getAttributes();
    List<Element> elements = testMapping.getMappings().getElements();

    FieldValueCell fieldValueCell1 = new FieldValueCell();
    fieldValueCell1.setObjectId("object_id00");
    fieldValueCell1.setOccurence("123");
    Tooltip tooltip1 = new Tooltip();
    tooltip1.setTooltippedText("http://mint-projects...");
    tooltip1.setTooltipText("The Tooltip for Text or URL 1");
    fieldValueCell1.setTooltip(tooltip1);
    FieldValueCell fieldValueCell2 = new FieldValueCell();
    fieldValueCell2.setObjectId("object_id01");
    fieldValueCell2.setOccurence("123");
    Tooltip tooltip2 = new Tooltip();
    tooltip2.setTooltippedText("http://mint-projects.image...");
    tooltip2.setTooltipText("http://mint-projects.image.ntua.gr/photography/ProvidedCHO/MHF/zbiory.mhf.krakow.pl:MHF");
    fieldValueCell2.setTooltip(tooltip2);

    mappingCard.setFieldValueCells(Collections.newArrayList(fieldValueCell1, fieldValueCell2));
    return mappingCard;
  }

  private List<ActionMenu> createActionMenu() {
    ActionMenu actionMenu = new ActionMenu();

    SubmenuItem section1SubmenuItem1 = new SubmenuItem();
    SubmenuItem section1SubmenuItem2 = new SubmenuItem();
    SubmenuItem section1SubmenuItem3 = new SubmenuItem();
    SubmenuItem section1SubmenuItem4 = new SubmenuItem();
    section1SubmenuItem1.setSubmenu(false);
    section1SubmenuItem1.setText("Mapping to EDM");
    section1SubmenuItem1.setUrl("javascript:$('.dropdown-trigger').innerHTML = 'Mapping to EDM'");
    section1SubmenuItem2.setSubmenu(false);
    section1SubmenuItem2.setText("Source Schema");
    section1SubmenuItem2.setUrl("#");
    section1SubmenuItem3.setSubmenu(false);
    section1SubmenuItem3.setText("Per Item");
    section1SubmenuItem3.setUrl("#");
    section1SubmenuItem4.setSubmenu(false);
    section1SubmenuItem4.setText("XSLT Editor");
    section1SubmenuItem4.setUrl("#");

    SectionMenu sectionMenu1 = new SectionMenu();
    sectionMenu1.setButtonTitle("View");
    sectionMenu1.setMenuTitle("Show:");
    sectionMenu1.setMenuId("object_furtheractions");
    sectionMenu1.setStyleModifier("theme_select");
    sectionMenu1.setItems(Collections
        .newArrayList(section1SubmenuItem1, section1SubmenuItem2, section1SubmenuItem3,
            section1SubmenuItem4));
    Section section1 = new Section();
    section1.setMenu(sectionMenu1);

    SubmenuItem section2SubmenuItem1 = new SubmenuItem();
    SubmenuItem section2SubmenuItem2 = new SubmenuItem();
    SubmenuItem section2SubmenuItem3 = new SubmenuItem();
    SubmenuItem section2SubmenuItem4 = new SubmenuItem();
    SubmenuItem section2SubmenuItem5 = new SubmenuItem();
    SubmenuItem section2SubmenuItem6 = new SubmenuItem();
    SubmenuItem section2SubmenuItem7 = new SubmenuItem();
    SubmenuItem section2SubmenuItem8 = new SubmenuItem();
    SubmenuItem section2SubmenuItem9 = new SubmenuItem();

    section2SubmenuItem1.setSubmenu(false);
    section2SubmenuItem1.setText("dc:title");
    section2SubmenuItem1.setUrl("#");
    section2SubmenuItem2.setSubmenu(false);
    section2SubmenuItem2.setText("@xml:lang");
    section2SubmenuItem2.setUrl("#");
    section2SubmenuItem3.setSubmenu(false);
    section2SubmenuItem3.setText("dc:creator");
    section2SubmenuItem3.setUrl("#");
    section2SubmenuItem4.setSubmenu(false);
    section2SubmenuItem4.setText("@xml:lang");
    section2SubmenuItem4.setUrl("#");
    section2SubmenuItem5.setSubmenu(false);
    section2SubmenuItem5.setText("dc:language");
    section2SubmenuItem5.setUrl("#");
    section2SubmenuItem6.setSubmenu(false);
    section2SubmenuItem6.setText("@xml:lang");
    section2SubmenuItem6.setUrl("#");
    section2SubmenuItem7.setSubmenu(false);
    section2SubmenuItem7.setText("dc:subject");
    section2SubmenuItem7.setUrl("#");
    section2SubmenuItem8.setSubmenu(false);
    section2SubmenuItem8.setText("@xml:lang");
    section2SubmenuItem8.setUrl("#");
    section2SubmenuItem9.setSubmenu(false);
    section2SubmenuItem9.setText("dc:identifier");
    section2SubmenuItem9.setUrl("#");

    SectionMenu sectionMenu2 = new SectionMenu();
    sectionMenu2.setButtonTitle("Browse");
    sectionMenu2.setMenuTitle("Filter by:");
    sectionMenu2.setMenuId("object_furtheractions2");
    sectionMenu2.setStyleModifier("theme_select");
    sectionMenu2.setItems(Collections
        .newArrayList(section2SubmenuItem1, section2SubmenuItem2, section2SubmenuItem3,
            section2SubmenuItem4, section2SubmenuItem5, section2SubmenuItem6, section2SubmenuItem7,
            section2SubmenuItem8, section2SubmenuItem9));

    Section section2 = new Section();
    section2.setMenu(sectionMenu2);

    actionMenu.setSections(Collections.newArrayList(section1, section2));

    SearchBox searchBox = new SearchBox();
    searchBox.setSearchBoxHidden("Search Hidden");
    searchBox.setSearchBoxLabel("Search Label");
    searchBox.setSearchBoxLegend("Search");
    actionMenu.setSearchBox(searchBox);

    return Collections.newArrayList(actionMenu);
  }

  private MetisHeader createMetisHeader() {
    Options options = new Options();
    options.setSearchActive(false);
    options.setSettingsActive(true);
    Logo logo = new Logo();
    logo.setUrl("#");
    logo.setText("Europeana Pandora");

    Global global = new Global();
    global.setOptions(options);
    global.setLogo(logo);
    global.setPrimaryNav(createPrimaryNav());
    global.setUtilityNav(createUtilityNav());

    NextPrev nextPrev = new NextPrev();
    nextPrev.setNextUrl("next_url_here");
    nextPrev.setPrevUrl("prev_url_here");
    nextPrev.setResultsUrl("results_url_here");
    Navigation navigation = new Navigation();
    navigation.setHomeUrl("#");
    navigation.setHomeText("Return to Home");
    navigation.setNextPrev(nextPrev);
    navigation.setFooter(false);
    navigation.setGlobal(global);

    MetisHeader metisHeader = new MetisHeader();
    metisHeader.setNavigation(navigation);

    return metisHeader;
  }

  private PrimaryNav createPrimaryNav() {
    List<SubmenuItem> submenuItems = new ArrayList<>();
    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    SubmenuItem submenuItem3 = new SubmenuItem();
    submenuItem1.setText("New Dataset");
    submenuItem1.setUrl("/new-dataset-page");
    submenuItem1.setIsCurrent(false);
    submenuItem1.setSubmenu(false);
    submenuItem2.setText("All Datasets");
    submenuItem2.setUrl("/all-datasets-page");
    submenuItem2.setIsCurrent(false);
    submenuItem2.setSubmenu(false);

    Submenu submenu = new Submenu();
    SubmenuItem subMenuSubmenuItem1 = new SubmenuItem();
    SubmenuItem subMenuSubmenuItem2 = new SubmenuItem();
    SubmenuItem subMenuSubmenuItem3 = new SubmenuItem();
    SubmenuItem subMenuSubmenuItem4 = new SubmenuItem();
    SubmenuItem subMenuSubmenuItem5 = new SubmenuItem();
    SubmenuItem subMenuSubmenuItem6 = new SubmenuItem();
    subMenuSubmenuItem1.setText("Organizations");
    subMenuSubmenuItem1.setUrl("#");
    subMenuSubmenuItem1.setSubtitle(false);
    subMenuSubmenuItem1.setSubmenu(false);
    subMenuSubmenuItem2.setText("Users");
    subMenuSubmenuItem2.setUrl("#");
    subMenuSubmenuItem2.setSubtitle(false);
    subMenuSubmenuItem2.setSubmenu(false);
    subMenuSubmenuItem3.setIsDivider(true);
    subMenuSubmenuItem3.setSubmenu(false);
    subMenuSubmenuItem4.setText("Crosswalks");
    subMenuSubmenuItem4.setSubmenu(false);
    subMenuSubmenuItem5.setText("Entities");
    subMenuSubmenuItem5.setUrl("#");
    subMenuSubmenuItem5.setSubmenu(false);
    subMenuSubmenuItem6.setText("Schemas (XSD)");
    subMenuSubmenuItem6.setUrl("#");
    subMenuSubmenuItem6.setSubmenu(false);

    submenu.setItems(Collections
        .newArrayList(subMenuSubmenuItem1, subMenuSubmenuItem2, subMenuSubmenuItem3,
            subMenuSubmenuItem4, subMenuSubmenuItem5, subMenuSubmenuItem6));

    submenuItem3.setText("Management");
    submenuItem3.setUrl("#");
    submenuItem3.setIsCurrent(false);
    submenuItem3.setSubmenu(submenu);

    PrimaryNav primaryNav = new PrimaryNav();
    primaryNav.setSubmenu(false);
    primaryNav.setMenuId("main-menu");
    primaryNav.setItems(Collections.newArrayList(submenuItem1, submenuItem2, submenuItem3));

    return primaryNav;
  }

  private UtilityNav createUtilityNav() {
    MenuItem menuItem = new MenuItem();
    menuItem.setText("Settings");
    menuItem.setUrl("#");
    menuItem.setIcon("settings");
    List<SubmenuItem> submenuItems = new ArrayList<>();
    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    SubmenuItem submenuItem3 = new SubmenuItem();
    submenuItem1.setText("My Profile");
    submenuItem1.setUrl("#");
    submenuItem1.setSubmenu(false);
    submenuItem2.setSubmenu(false);
    submenuItem2.setIsDivider(true);
    submenuItem3.setText("Logout");
    submenuItem3.setUrl("#");
    submenuItem3.setSubtitle(false);

    Submenu submenu = new Submenu();
    submenu
        .setItems(Collections.newArrayList(submenuItem1, submenuItem2, submenuItem3, submenuItem3));
    menuItem.setSubmenu(submenu);

    UtilityNav utilityNav = new UtilityNav();
    utilityNav.setMenuId("settings-menu");
    utilityNav.setStyleModifier("caret-right");
    utilityNav.setTabindex("6");
    utilityNav.setItems(Collections.newArrayList(menuItem));

    return utilityNav;
  }

  @Override
  public void addPageContent() {

  }

  public void setUserDTO(UserDTO userDTO) {
    this.userDTO = userDTO;
  }
}
