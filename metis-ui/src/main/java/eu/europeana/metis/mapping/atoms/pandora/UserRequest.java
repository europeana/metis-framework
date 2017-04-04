package eu.europeana.metis.mapping.atoms.pandora;

/**
 * Class represents the model for the user organization affiliation requests table.
 * @author alena
 *
 */
public class UserRequest {

	private String id;
	
	private String email;
	
	private String organization;
	
	private String date;
	
	private String type;
	
	private String request_type;
	
	private String status;
	
	private String status_type;
	
	private String url;

	public UserRequest(String id, String email, String organization, String date, Boolean isDeleteRequest, String status) {
		this.id  = id;
		this.email = email;
		this.organization = organization;
		this.date = date;
		this.url = "/profile?userId=" + id;
		if (isDeleteRequest) {
			this.type = "Delete";
			this.request_type = "user-request-type-delete";
		} else {
			this.type = "Add";
			this.request_type = "user-request-type-add";
		}
		if (status == null || status.equalsIgnoreCase("Pending")) {
			this.status = "Pending";
			this.status_type = "user-request-pending";
		} else if (status.equalsIgnoreCase("Accepted")) {
			this.status = "Accepted";
			this.status_type = "user-request-accepted";
		} else if (status.equalsIgnoreCase("Rejected")) {
			this.status = "Rejected";
			this.status_type = "user-request-rejected";
		}
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getOrganization() {
		return organization;
	}


	public void setOrganization(String organization) {
		this.organization = organization;
	}


	public String getDate() {
		return date;
	}


	public void setDate(String date) {
		this.date = date;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getRequest_type() {
		return request_type;
	}


	public void setRequest_type(String request_type) {
		this.request_type = request_type;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus_type() {
		return status_type;
	}

	public void setStatus_type(String status_type) {
		this.status_type = status_type;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
