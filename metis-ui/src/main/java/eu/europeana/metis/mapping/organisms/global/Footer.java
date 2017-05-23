//package eu.europeana.metis.mapping.organisms.global;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import eu.europeana.metis.mapping.util.MetisMappingUtil;
//
//public class Footer {
//
//	private Map<String, Object> navigation;
//
//	public Footer(List<Entry<String, String>> linkList1, String linkListTitle1,
//				  List<Entry<String, String>> linkList2, String linkListTitle2,
//				  List<Entry<String, String>> linkList3, String linkListTitle3,
//				  List<Entry<String, String>> subFooter, Map<String, Boolean> social) {
//		this.navigation = new HashMap<>();
//		Map<String, Object> footer = new HashMap<>();
//
//		Map<String, Object> linklist1 = new HashMap<>();
//		linklist1.put("title", linkListTitle1);
//		linklist1.put("items", MetisMappingUtil.buildSimplePairs(linkList1, "text", "url"));
//		footer.put("linklist1", linklist1);
//
//		Map<String, Object> linklist2 = new HashMap<>();
//		linklist2.put("title", linkListTitle2);
//		linklist2.put("items", MetisMappingUtil.buildSimplePairs(linkList2, "text", "url"));
//		footer.put("linklist2", linklist2);
//
//		Map<String, Object> linklist3 = new HashMap<>();
//		linklist3.put("title", linkListTitle3);
//		linklist3.put("items", MetisMappingUtil.buildSimplePairs(linkList3, "text", "url"));
//		footer.put("linklist3", linklist3);
//
//		footer.put("social", social);
//
//		Map<String, Object> subfooter = new HashMap<>();
//		subfooter.put("items", MetisMappingUtil.buildSimplePairs(subFooter, "text", "url"));
//		footer.put("subfooter", subfooter);
//
//		this.navigation.put("footer", footer);
//	}
//
//	public Map<String, Object> getNavigation() {
//		return navigation;
//	}
//
//
//	public void setNavigation(Map<String, Object> navigation) {
//		this.navigation = navigation;
//	}
//}
