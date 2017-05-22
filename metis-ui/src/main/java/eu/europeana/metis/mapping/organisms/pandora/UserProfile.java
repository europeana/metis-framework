package eu.europeana.metis.mapping.organisms.pandora;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.OrganizationRole;
import eu.europeana.metis.ui.mongo.domain.UserDTO;

/**
 * The class represents Metis user with all its public information (both in LDAP and MongoDB). 
 * @author alena
 *
 */
public class UserProfile extends User {

    private String country;

    private String skype;

    private Boolean europeanaNetworkMember;

    //FIXME after we change the organizations widget there will be a list of organizations binding.
    private String organization;
    
//    public UserProfile() { }
    
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
    
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
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

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public void setLdapUser(User user) {
		setActive(user.isActive());
		setApproved(user.isApproved());
		setDn(user.getDn());
		setMetisAuthenticationDn(user.getMetisAuthenticationDn());
		setUsersDn(user.getUsersDn());
		setEmail(user.getEmail());
		setFirstName(user.getFirstName());
		setLastName(user.getLastName());
		setPassword(user.getPassword());
		setDescription(user.getDescription());
	}

	public void setDBUser(DBUser dbUser) {
		Country c = dbUser.getCountry();
		if ( c != null  ) {
			setCountry(c.getName());			
		}
		setSkype(dbUser.getSkypeId());
		setEuropeanaNetworkMember(dbUser.getEuropeanaNetworkMember());
		List<OrganizationRole> organizationRoles = dbUser.getOrganizationRoles();
		List<String> organizations = new ArrayList<String>();
		if (organizationRoles != null) {
			for (OrganizationRole or: organizationRoles) {
				if (or.getOrganizationId() != null) {
					organizations.add(or.getOrganizationId());				
				}
			}			
		}
		StringBuilder orgs = new StringBuilder();
		if (!organizations.isEmpty()) {
			for (int i = 0; i < organizations.size() - 1; i++) {
				orgs.append(organizations.get(i)).append(",");
			}
			orgs.append(organizations.get(organizations.size() - 1));						
		}
		setOrganization(orgs.toString());
	}
}
