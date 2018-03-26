package eu.europeana.metis.mediaservice;

public enum UrlType {
	OBJECT("edm:object"),
	HAS_VIEW("edm:hasView"),
	IS_SHOWN_BY("edm:isShownBy"),
	IS_SHOWN_AT("edm:isShownAt");
	
	public final String tagName;
	
	UrlType(String tagName) {
		this.tagName = tagName;
	}
}
