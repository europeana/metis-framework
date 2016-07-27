/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.path;

import eu.europeana.enrichment.context.Namespaces;

/**
 * 
 * XPath scanner.
 * 
 * @author Borys Omelayenko
 */
class XPScanner {

	String string;
	int pos = 0;

	public XPScanner(String string) {
		super();
		this.string = string;
	}

	private boolean isIdentifier(char c) {
		return Character.isJavaIdentifierPart(c) || c == '-' || c == '.';
	}

	public String nextIdentifier() {
		int offset = 0;
		while ((pos + offset < string.length())
				&& (isIdentifier(string.charAt(pos + offset)))) {
			offset++;
		}

		String result = string.substring(pos, pos + offset);
		pos += offset;
		return result;
	}

	public boolean hasNextIdenitifer() {
		char c = string.charAt(pos);
		return Character.isJavaIdentifierStart(c) && c != '@';
	}

	public String nextLiteral() {
		char quote = string.charAt(pos);
		if (quote != '\'' && quote != '\"') {
			return null;
		}
		int offset = 1;
		while (!(string.charAt(pos + offset) == quote)) {
			offset++;
		}

		String result = string.substring(pos + 1, pos + offset);
		pos += offset + 1;
		return result;
	}

	public boolean skipString(String prefix) {
		if (string.substring(pos).startsWith(prefix)) {
			pos += prefix.length();
			return true;
		}
		return false;
	}

	public boolean eof() {
		return (pos >= string.length());
	}

	public int pos() {
		return pos;
	}

	@Override
	public String toString() {
		return string;
	}

	public NamespacedName expandNamespaceForTag(Namespaces namespaces)
			throws Exception {
		if (this.hasNextIdenitifer()) {
			String nick = this.nextIdentifier();
			if (this.skipString(":")) {
				// it was a namespace nick, go for the element
				String uri = namespaces.getUri(nick);
				if (uri == null) {
					throw new Exception("Namespace nick " + nick
							+ " not found in " + this);
				}
				String name = this.nextIdentifier();
				if (name.isEmpty()) {
					throw new Exception("Empty qualified name in " + this);
				}
				return new NamespacedName(name, uri);
			} else {
				// unqualified element
				if (nick.isEmpty()) {
					throw new Exception("Empty unqualified name in " + this);
				}
				return new NamespacedName(nick, "");
			}
		}
		throw new Exception("Error with qualified names on " + this);
	}

}
