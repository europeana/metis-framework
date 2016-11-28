package eu.europeana.metis.mapping.organisms.pandora;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.molecules.controls.DropdownMenu;
import eu.europeana.metis.mapping.molecules.pandora.FieldValue;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;
import eu.europeana.metis.mapping.util.MappingCardStub;

/**
 * 
 * @author alena
 *
 */
public class Mapping_card {

	private String prefix;

	private String name;
	
	private String xpath;
	
	private DropdownMenu dropdown;

	private List<FieldValue> field_value_cells;
	
//	private List<Mapping_card> child_cards;

	public Mapping_card(Element element, int offset, int count) {		
		Statistics statistics = MappingCardStub.getStatistics();
		this.xpath = statistics.getXpath();
		this.prefix = element.getPrefix();
		this.name = element.getName();
		this.dropdown = buildFlagDropdown();
		List<FieldValue> fields = new ArrayList<>();
		int i = 0;
		for (StatisticsValue value : statistics.getValues()) {
			if (i < offset) {
				continue;
			}
			if (i >= count) {
				break;
			}
			fields.add(new FieldValue(value.getId().toHexString(), value.getValue(), value.getOccurence()));
			i++;
		}
		this.field_value_cells = fields;
//		this.child_cards = new ArrayList<>();
//		for (Element el : element.getElements()) {
//			this.child_cards.add(new Mapping_card(el, offset, count));
//		}
	}

	/**
	 * The dropdown menu is created 
	 * @return
	 */
	private DropdownMenu buildFlagDropdown() {
		DropdownMenu menu = new DropdownMenu(" ", "Set item as:", null, "mapping-card-dropdown", "theme_select");
		menu.addMenuItem("Valid", "#");
		menu.addMenuItem("Suspicious", "#");
		menu.addMenuItem("Invalid", "#");
		menu.addMenuItem(true);
		menu.addMenuItem("Remove all marks", "#");
		return menu;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public List<FieldValue> getField_value_cells() {
		return field_value_cells;
	}

	public void setField_value_cells(List<FieldValue> field_value_cells) {
		this.field_value_cells = field_value_cells;
	}
	
	public DropdownMenu getDropdown() {
		return dropdown;
	}

	public void setDropdown(DropdownMenu dropdown) {
		this.dropdown = dropdown;
	}
	
//	public List<Mapping_card> getChild_cards() {
//		return child_cards;
//	}
//
//	public void setChild_cards(List<Mapping_card> child_cards) {
//		this.child_cards = child_cards;
//	}
}
