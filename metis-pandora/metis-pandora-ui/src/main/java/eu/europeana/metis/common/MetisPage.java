package eu.europeana.metis.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import eu.europeana.metis.mapping.organisms.global.NavigationTop;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;

/**
 * This is common Metis page with the same assets, bread-crumbs and header instantiated.
 * @author alena
 *
 */
public abstract class MetisPage extends AbstractMetisPage {
	
	public abstract Byte resolveCurrentPage();
	
	@Override
	public List<Entry<String, String>> resolveCssFiles() {
		return Arrays.asList(new SimpleEntry<String, String>("https://europeanastyleguidetest.a.cdnify.io/css/pandora/screen.css", "all"),
//		return Arrays.asList(new SimpleEntry<String, String>("http://localhost/css/pandora/screen.css", "all"),
				new SimpleEntry<String, String>("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css", "all"));
	}

	@Override
	public List<Entry<String, String>> resolveJsFiles() {
//		return Arrays.asList(new SimpleEntry<String, String>("https://europeana-styleguide-test.s3.amazonaws.com/js/modules/require.js", 
//				"https://europeanastyleguidetest.a.cdnify.io/js/modules/main/templates/main-pandora"));
		//to test the java app with a local styleguide assets!
		return Arrays.asList(new SimpleEntry<String, String>("http://localhost/js/modules/require.js", 
				"http://localhost/js/modules/main/templates/main-pandora"));
	}

	@Override
	public List<Entry<String, String>> resolveJsVars() {
		return Arrays.asList(new SimpleEntry<String, String>("pageName", "portal/index"));
	}

	@Override
	public List<Entry<String, String>> resolveBreadcrumbs() {
		List<Entry<String, String>> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(new SimpleEntry<String, String>("Home", "/"));
		return breadcrumbs;
	}

	@Override
	public NavigationTop buildHeader() {
		Byte current = resolveCurrentPage();
		NavigationTop header = new NavigationTop("#", "Home", false);
		header.addNextPrev("next_url_here", "prev_url_here", "results_url_here");
		
		List<NavigationTopMenu> items = new ArrayList<>();
		items.add(new NavigationTopMenu("New Dataset", "/new-dataset-page", (current != null && current == 0), null, null, null, null));
		items.add(new NavigationTopMenu("All Datasets", "/all-datasets-page", (current != null && current == 1), null, null, null, null));
		List<NavigationTopMenu> submenu = Arrays.asList(
				new NavigationTopMenu("Organizations", "#", null, null, null, null, null),
				new NavigationTopMenu("Users", "#", null, null, null, null, null),
				new NavigationTopMenu(true),
				new NavigationTopMenu("Crosswalks", null, null),
				new NavigationTopMenu("Entities", "#", null),
				new NavigationTopMenu("Schemas (XSD)", "#", null));
		items.add(new NavigationTopMenu("Management", "#", (current != null && current == 2), null, null, null, submenu));
		header.addGlobal(false, true, "#", "Europeana Metis", "main-menu", items, buildUtilityNavigation());		
		return header;
	}
	
	/**
	 * The method builds the model for the top-right menu Settings on a Metis page.
	 * @return the list utility navigation menu items.
	 */
	public abstract List<NavigationTopMenu> buildUtilityNavigation();
}
