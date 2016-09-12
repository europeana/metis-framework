package eu.europeana.metis.mapping.molecules.forms;

/**
 * 
 * @author alena
 *
 */
public class Search {
	
	private String search_box_legend;
	
	private String search_box_label;
	
	private String search_box_hidden;

	public Search(String search_box_legend, String search_box_label, String search_box_hidden) {
		this.search_box_legend = search_box_legend;
		this.search_box_label = search_box_label;
		this.search_box_hidden = search_box_hidden;
	}

	public String getSearch_box_legend() {
		return search_box_legend;
	}

	public void setSearch_box_legend(String search_box_legend) {
		this.search_box_legend = search_box_legend;
	}

	public String getSearch_box_label() {
		return search_box_label;
	}

	public void setSearch_box_label(String search_box_label) {
		this.search_box_label = search_box_label;
	}

	public String getSearch_box_hidden() {
		return search_box_hidden;
	}

	public void setSearch_box_hidden(String search_box_hidden) {
		this.search_box_hidden = search_box_hidden;
	}
	
	
}
