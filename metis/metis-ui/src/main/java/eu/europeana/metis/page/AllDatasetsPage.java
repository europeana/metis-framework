package eu.europeana.metis.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;

public class AllDatasetsPage extends MetisPage {

	@Override
	public Byte resolveCurrentPage() {
		return 1;
	}

	@Override
	public void addPageContent(Map<String, Object> model) {
		//TODO
	}

	@Override
	public List<Entry<String, String>> resolveBreadcrumbs() {
		List<Entry<String, String>> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(new SimpleEntry<String, String>("Home", "/home-page"));
		breadcrumbs.add(new SimpleEntry<String, String>("All Datasets", "#"));
		return breadcrumbs;
	}
	
	@Override
	public List<NavigationTopMenu> buildUtilityNavigation() {
		return Arrays.asList(
				new NavigationTopMenu("Register", "/register", false),
				new NavigationTopMenu("Login", "/login", true));
	}
}
