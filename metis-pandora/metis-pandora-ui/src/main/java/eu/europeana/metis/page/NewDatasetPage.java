package eu.europeana.metis.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import eu.europeana.metis.common.MetisPage;

public class NewDatasetPage extends MetisPage {

	@Override
	public Byte resolveCurrentPage() {
		return 0;
	}

	@Override
	public void addPageContent(Map<String, Object> model) {
		//TODO
	}

	@Override
	public List<Entry<String, String>> resolveBreadcrumbs() {
		List<Entry<String, String>> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(new SimpleEntry<String, String>("Home", "/home-page"));
		breadcrumbs.add(new SimpleEntry<String, String>("Create Dataset", "#"));
		return breadcrumbs;
	}
}
