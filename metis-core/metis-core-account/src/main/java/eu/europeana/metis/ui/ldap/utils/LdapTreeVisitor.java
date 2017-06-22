package eu.europeana.metis.ui.ldap.utils;

import org.springframework.ldap.core.DirContextOperations;

public interface LdapTreeVisitor {

	void visit(DirContextOperations node, int currentDepth);
}
