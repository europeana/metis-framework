//package eu.europeana.metis.mapping.organisms.pandora;
//
//import eu.europeana.metis.ui.ldap.domain.LdapUser;
//import eu.europeana.metis.ui.mongo.domain.User;
//import java.util.ArrayList;
//import java.util.List;
//
//import eu.europeana.metis.core.common.Country;
//import eu.europeana.metis.ui.mongo.domain.OrganizationRole;
//import eu.europeana.metis.ui.mongo.domain.UserDTO;
//
///**
// * The class represents Metis user with all its public information (both in LDAP and MongoDB).
// *
// * @author alena
// */
//public class UserProfile extends LdapUser {
//
//  private String country;
//
//  private String skype;
//
//  private Boolean europeanaNetworkMember;
//
//  private String organization;
//
//  public void init(UserDTO userDTO) {
//    LdapUser ldapUser = userDTO.getLdapUser();
//    User user = userDTO.getUser();
//    setLdapUser(ldapUser);
//    setDBUser(user);
//  }
//
//  public String getCountry() {
//    return country;
//  }
//
//  public void setCountry(String country) {
//    this.country = country;
//  }
//
//  public String getSkype() {
//    return skype;
//  }
//
//  public void setSkype(String skype) {
//    this.skype = skype;
//  }
//
//  public Boolean getEuropeanaNetworkMember() {
//    return europeanaNetworkMember;
//  }
//
//  public void setEuropeanaNetworkMember(Boolean europeanaNetworkMember) {
//    this.europeanaNetworkMember = europeanaNetworkMember;
//  }
//
//  public String getOrganization() {
//    return organization;
//  }
//
//  public void setOrganization(String organization) {
//    this.organization = organization;
//  }
//
//  public void setLdapUser(LdapUser ldapUser) {
//    if (ldapUser != null) {
//      setActive(ldapUser.isActive());
//      setApproved(ldapUser.isApproved());
//      setDn(ldapUser.getDn());
//      setMetisAuthenticationDn(ldapUser.getMetisAuthenticationDn());
//      setUsersDn(ldapUser.getUsersDn());
//      setEmail(ldapUser.getEmail());
//      setFirstName(ldapUser.getFirstName());
//      setLastName(ldapUser.getLastName());
//      setPassword(ldapUser.getPassword());
//      setDescription(ldapUser.getDescription());
//    }
//  }
//
//  public void setDBUser(User user) {
//    if (user != null) {
//      Country c = user.getCountry();
//      if (c != null) {
//        setCountry(c.getName());
//      }
//      setSkype(user.getSkypeId());
//      setEuropeanaNetworkMember(user.getEuropeanaNetworkMember());
//      List<OrganizationRole> organizationRoles = user.getOrganizationRoles();
//      List<String> organizations = new ArrayList<String>();
//      if (organizationRoles != null) {
//        for (OrganizationRole or : organizationRoles) {
//          if (or.getOrganizationId() != null) {
//            organizations.add(or.getOrganizationId());
//          }
//        }
//      }
//      StringBuilder orgs = new StringBuilder();
//      if (!organizations.isEmpty()) {
//        for (int i = 0; i < organizations.size() - 1; i++) {
//          orgs.append(organizations.get(i)).append(",");
//        }
//        orgs.append(organizations.get(organizations.size() - 1));
//      }
//      setOrganization(orgs.toString());
//    }
//  }
//}
