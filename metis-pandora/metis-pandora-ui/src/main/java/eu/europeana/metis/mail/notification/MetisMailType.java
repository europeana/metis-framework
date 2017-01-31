package eu.europeana.metis.mail.notification;

public enum MetisMailType {	
	ADMIN_ROLE_REQUEST_PENDING("User account creation pending validation: %s %s", "A new user has requested an account creation: %s %s"), 
	USER_ROLE_REQUEST_PENDING("User account creation pending validation", "Your request was sent for approval and you will be notified as soon as your account was activated. \n\nBest regards,\nThe Metis team"), 
	USER_ROLE_REQUEST_APPROVED("", ""), 
	USER_ROLE_REQUEST_REJECTED("", "");
	
	private String mailSubjectTemplate;
	
	private String mailTextTemplate;
	
	private MetisMailType(String subjectTemplate, String textTemplate) {
		this.mailSubjectTemplate = subjectTemplate;
		this.mailTextTemplate = textTemplate;
	}
	
	public String getMailSubject(String userName, String userSurname) {
		return String.format(this.mailSubjectTemplate,
				userName != null && !userName.isEmpty() ? userName : "[UNKNOWN USER]",
				userSurname != null && !userSurname.isEmpty() ? userSurname : "");
	}
	
	public String getMailText(String userName, String userSurname) {
		return String.format(this.mailTextTemplate,
				userName != null && !userName.isEmpty() ? userName : "[UNKNOWN USER]",
				userSurname != null && !userSurname.isEmpty() ? userSurname : "");
	}
}
