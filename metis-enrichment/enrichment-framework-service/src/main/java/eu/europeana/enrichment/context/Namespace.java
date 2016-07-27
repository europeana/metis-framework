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
package eu.europeana.enrichment.context;


/**
 * Namespace with nick and uri.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Namespace
{
	private String uri;
	private String nick;

	public String getNick()
	{
		return nick;
	}
	
	public String getUri()
	{
		return uri;
	}

	@Override
	public String toString()
	{
		return getUri();
	}

	public Namespace(String uri, String nick, boolean printable) {
		super();
		this.uri = uri;
		this.nick = nick;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return uri.equals(obj);
	}
}
