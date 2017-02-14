package eu.europeana.metis.mapping.organisms.pandora;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.MappingType;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.molecules.controls.DropdownMenu;
import eu.europeana.metis.mapping.molecules.pandora.FieldValue;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;

/**
 * Java model representing a mapping card component: "/organisms/pandora/mapping-card".
 * @author alena
 *
 */
public class Mapping_card {
	
	private String object_id;

	private String prefix;

	private String name;
	
	private String xpath;
	
	private String depth;
	
	//TODO delete this after Eduardo removes it from a Sylegyuide template
	private DropdownMenu dropdown;

	private List<FieldValue> field_value_cells;

	public Mapping_card(Attribute field, Statistics statistics, int offset, int count, Integer depth) {
		if (depth == null) {
			this.depth = "depth_0";
		} else {
			this.depth = "depth_" + depth;
		}
		if (field == null) {
			return;
		}
		//FIXME!
		if (field.getId() == null) {
			field.setId(new ObjectId());
		}
		this.setObject_id(field.getId().toString());
		this.prefix = field.getPrefix();
		this.name = field.getName();
		List<SimpleMapping> mappings = field.getMappings();
		if (mappings != null && !mappings.isEmpty()) {
			for (SimpleMapping simpleMapping : mappings) {
				if (simpleMapping.getType() == MappingType.XPATH) {
					this.xpath = simpleMapping.getSourceField();
					break;
				}	
			}			
		}
		this.dropdown = buildFlagDropdown();
		List<FieldValue> fields = new ArrayList<>();
		int i = 0;
		if (statistics != null && statistics.getValues() != null && !statistics.getValues().isEmpty()) {
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
		}
		this.field_value_cells = fields;
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
	
	public String getObject_id() {
		return object_id;
	}
	
	public void setObject_id(String object_id) {
		this.object_id = object_id;
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

	public String getDepth() {
		return depth;
	}

	public void setDepth(String depth) {
		this.depth = depth;
	}
}
