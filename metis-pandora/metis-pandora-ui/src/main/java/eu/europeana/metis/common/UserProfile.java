package eu.europeana.metis.common;

import java.util.List;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.UserDTO;

/**
 * The class represents Metis user with all its public information (both in LDAP and MongoDB). 
 * @author alena
 *
 */
public class UserProfile extends User {

    private Country country;

    private String skype;

    private Boolean europeanaNetworkMember;

    private List<String> organizations;
    
    public UserProfile() {
		// TODO Auto-generated constructor stub
	}
    
    public void init(UserDTO userDTO) {
		User user = userDTO.getUser();
		if (user != null) {
			setLdapUser(user);			
		}
		DBUser dbUser = userDTO.getDbUser();
		if (dbUser  != null) {
			setDBUser(dbUser);			
		}
	}
    
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getSkype() {
		return skype;
	}

	public void setSkype(String skype) {
		this.skype = skype;
	}

	public Boolean getEuropeanaNetworkMember() {
		return europeanaNetworkMember;
	}

	public void setEuropeanaNetworkMember(Boolean europeanaNetworkMember) {
		this.europeanaNetworkMember = europeanaNetworkMember;
	}

	public List<String> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<String> organizations) {
		this.organizations = organizations;
	}
	
	public void setLdapUser(User user) {
		setActive(user.isActive());
		setApproved(user.isApproved());
		setDn(user.getDn());
		setMetisAuthenticationDn(user.getMetisAuthenticationDn());
		setUsersDn(user.getUsersDn());
		setEmail(user.getEmail());
		setFullName(user.getFullName());
		setLastName(user.getLastName());
		setPassword(user.getPassword());
		setDescription(user.getDescription());
	}

	public void setDBUser(DBUser dbUser) {
		// TODO Auto-generated method stub
		setCountry(dbUser.getCountry());
		setSkype(dbUser.getSkypeId());
		setEuropeanaNetworkMember(dbUser.getEuropeanaNetworkMember());
		setOrganizations(dbUser.getOrganizations());
	}
}
