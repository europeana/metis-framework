package eu.europeana.metis.framework.ui.ldap;

import java.util.List;

import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component
public class UserRepositoryImpl implements UserRepository<User> {
	private static final String	basedn			= "ou=users";
	private static final String	queryDelimeter	= ",";

	@Autowired
	private OdmManager			odmManager;

	@Autowired(required = true)
	@Qualifier(value = "ldapTemplate")
	private LdapTemplate		ldapTemplate;

	/*
	 * (non-Javadoc)
	 *
	 * @see ldap.advance.example.UserRepositoryIntf#getAllUserNames()
	 */
	public List<String> getAllUserNames() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ldap.advance.example.UserRepositoryIntf#getAllUsers()
	 */
	public List<User> getAllUsers() {
		SearchControls controls = new SearchControls();
		// controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		return odmManager.search(User.class, new DistinguishedName(basedn), "(uid=*)", controls);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ldap.advance.example.UserRepositoryIntf#getUserDetails(java.lang.String)
	 */
	public User getUserDetails(String userName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ldap.advance.example.UserRepositoryIntf#getUserDetail(java.lang.String)
	 */
	public User getUserDetail(String userName) {
		System.out.println("executing {getUserDetail}");
		System.out.println(bindDN(userName));
		User rUser = odmManager.read(User.class, bindDN(userName));
		return rUser;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ldap.advance.example.UserRepositoryIntf#authenticate(java.lang.String,
	 * java.lang.String)
	 */
	
	public boolean authenticate(String base, String userName, String password) {
		System.out.println("executing {authenticate}");
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ldap.advance.example.UserRepositoryIntf#updateTelePhone(java.lang.String)
	 */
	public User updateTelePhone(String userName, String newNumber) {
		System.out.println("executing {updateTelePhone}");
		User mUser = odmManager.read(User.class, bindDN(userName));
		mUser.setTelephoneNumber(newNumber);
		odmManager.update(mUser);
		return getUserDetails(userName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ldap.advance.example.UserRepositoryIntf#createUser(ldap.advance.example.User)
	 */
	public boolean createUser(User user) {
		System.out.println("executing {createUser}");
		user.setDistinguishedName(bindDN(user.getUid()));
		odmManager.create(user);
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ldap.advance.example.UserRepositoryIntf#remove(java.lang.String)
	 */
	public boolean remove(String uid) {
		// ldapTemplate.unbind(bindDN(uid));
		odmManager.delete(getUserDetail(uid));
		return true;
	}

	public static javax.naming.Name bindDN(String userName) {
		javax.naming.Name name = new DistinguishedName("uid=".concat(userName).concat(queryDelimeter.concat(basedn)));
		return name;
	}
}
