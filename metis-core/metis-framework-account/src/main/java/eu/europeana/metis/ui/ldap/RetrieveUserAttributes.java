/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */

package eu.europeana.metis.ui.ldap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.core.userdetails.User;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class RetrieveUserAttributes {

	@Autowired
	LdapTemplate ldapTemplate;
	
	public static void main(String[] args) {
//		RetrieveUserAttributes retrieveUserAttributes = new RetrieveUserAttributes();
//		retrieveUserAttributes.getUserBasicAttributes("alena", retrieveUserAttributes.getLdapContext());
		
		
//		template.l
	}
	
	public void createUser(User user) {
		ldapTemplate.create(user);
	}

	
	public LdapContext getLdapContext(){
		LdapContext ctx = null;
		try{
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.SECURITY_PRINCIPAL, "Metis Authentication");
			env.put(Context.SECURITY_CREDENTIALS, "secret");
			env.put(Context.PROVIDER_URL, "ldap://Alenas-iMac:1389");
			ctx = new InitialLdapContext(env, null);
			System.out.println("Connection Successful.");
		}catch(NamingException nex){
			System.out.println("LDAP Connection: FAILED");
			nex.printStackTrace();
		}
		return ctx;
	}

	public static LdapContextSource contextSourceTarget() {
	    LdapContextSource ldapContextSource = new LdapContextSource();
	    ldapContextSource.setUrl("ldap://Alenas-iMac:1389");
	    ldapContextSource.setBase("ou=metis_authentication,dc=europeana,dc=eu");
	    ldapContextSource.setUserDn("cn=Metis Authentication");
	    ldapContextSource.setPassword("secret");
	    return ldapContextSource;

	}
	
	private User getUserBasicAttributes(String username, LdapContext ctx) {
		User user=null;
		try {

			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String[] attrIDs = { "distinguishedName",
					"sn",
					"givenname",
					"mail",
					"telephonenumber"};
			constraints.setReturningAttributes(attrIDs);
			//First input parameter is search bas, it can be "CN=Users,DC=YourDomain,DC=com"
			//Second Attribute can be uid=username
			NamingEnumeration answer = ctx.search("DC=YourDomain,DC=com", "sAMAccountName="
					+ username, constraints);
			if (answer.hasMore()) {
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();
				System.out.println("distinguishedName "+ attrs.get("distinguishedName"));
				System.out.println("givenname "+ attrs.get("givenname"));
				System.out.println("sn "+ attrs.get("sn"));
				System.out.println("mail "+ attrs.get("mail"));
			}else{
				throw new Exception("Invalid User");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return user;
	}

}
