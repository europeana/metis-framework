package eu.europeana.metis.mapping.organisms.pandora;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author alena
 *
 */
public class Mapping_Card_List {
//	{{#mapping_card}}
//		{{#card}}
//			{{>organisms/pandora/mapping-card}}
//		{{/card}}
//		{{#children}}
//	  		{{#mapping_cards}}
//	    		{{> templates/Pandora/Mapping-Card-List}}
//	  		{{/mapping_cards}}
//		{{/children}}
// {{/mapping_card}}
	
//	{{>organisms/pandora/mapping-card}}
//	{{#children}}
//	    {{>templates/Pandora/Mapping-Card-List}}
//	{{/children}}
	
//	private Map<String, Object> mapping_card;
	
	private Mapping_card mapping_card;
	
	private Map<String, Object> children;
	
	public Mapping_Card_List(Mapping_card mappingCard) {
		if (mappingCard != null) {
			this.setMapping_card(mappingCard); //new HashMap<>();
//			buildMappingCardList(mappingCard);			
		}
	}
	
	public void buildMappingCardList(Mapping_card mappingCard) {
//		mapping_card.put("card", mappingCard);		
		children = new HashMap<>();
		
//		List<Mapping_card> cardChildren = mappingCard.getChildren();		
//		if (cardChildren != null && !cardChildren.isEmpty()) {
//			List<Mapping_Card_List> childrenList = new ArrayList<>();
//			for (Mapping_card c : cardChildren) {
//				childrenList.add(new Mapping_Card_List(c));
//			}		
//			children.put("children", childrenList);
//		} else {
//			children.put("children", "false");
//		}
	}
//	
//	public Map<String, Object> getMapping_card() {
//		return mapping_card;
//	}
//
//	public void setMapping_card(Map<String, Object> mapping_card) {
//		this.mapping_card = mapping_card;
//	}

	public Mapping_card getMapping_card() {
		return mapping_card;
	}

	public void setMapping_card(Mapping_card mapping_card) {
		this.mapping_card = mapping_card;
	}

	public Map<String, Object> getChildren() {
		return children;
	}

	public void setChildren(Map<String, Object> children) {
		this.children = children;
	}
	
	
}
