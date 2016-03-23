package eu.europeana.metis.ui.ldap;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * @author Alena
 *
 */
public class UserBAK { //extends org.springframework.security.core.userdetails.User {
//	private static final long serialVersionUID = 1L;
	
//	public User() {
//		super(null, null, true, true, true, true, new ArrayList<GrantedAuthority>());
//	}
//
//	public User(String username, String password, boolean enabled, boolean accountNonExpired,
//			boolean credentialsNonExpired, boolean accountNonLocked,
//			Collection<? extends GrantedAuthority> authorities) {
//		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
//	}
//
//	public User(String username, String password) {
//		super(username, password, true, true, true, true, new ArrayList<GrantedAuthority>());
//	}
	
	String name;
	
	String surname;
	
	String email;
	
	String password;
	
	String organization;
	
	String skype;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
