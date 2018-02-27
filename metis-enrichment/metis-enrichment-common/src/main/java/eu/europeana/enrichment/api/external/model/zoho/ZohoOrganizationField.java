package eu.europeana.enrichment.api.external.model.zoho;

/**
 * This class supports representation of Zoho organization fields for API to
 * Zoho organization object that contains array of 'val'/'content' fields.
 * @author GrafR
 *
 */
public class ZohoOrganizationField {

    private String val;
    private String content;
    
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ZohoOrganizationField 
				&& this.val.equals(((ZohoOrganizationField) obj).val))
			return true;
				
		return false;
	}
	
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + val.hashCode();
        return result;
    }
	
}
