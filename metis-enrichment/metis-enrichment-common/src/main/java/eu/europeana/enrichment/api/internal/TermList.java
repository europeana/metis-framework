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
package eu.europeana.enrichment.api.internal;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * List of terms as stored in vocabularies.
 * 
 * @author Borys Omelayenko
 * 
 */
public class TermList implements Iterable<Term> {

	private TreeSet<Term> list = new TreeSet<Term>();

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<Term> iterator() {
		return list.iterator();
	}

	public TermList() {
		super();
	}

	Term first = null;
	boolean sameLabels = true;
	boolean sameCodes = true;

	public boolean add(Term term) {
		if (term == null) {
			return false;
		}
		// check that all terms and values are equal
		if (first == null) {
			first = term;
		}

		if (!first.getLabel().equals(term.getLabel())) {
			sameLabels = false;
		}
		if (!first.getCode().equals(term.getCode())) {
			sameCodes = false;
		}

		return list.add(term);
	}

	public boolean isSameLabels() {
		return sameLabels;
	}

	public boolean isSameCodes() {
		return sameCodes;
	}

	public void add(TermList t) {
		for (Term term : t) {
			add(term);
		}
	}

	public int size() {
		return list.size();
	}

	public Term getFirst() {

		return list.isEmpty() ? null : list.first();
	}

	public Term getLast() {
		return list.last();
	}

	@Override
	public String toString() {
		String result = "TermList: ";
		for (Term term : list) {
			result += term;
		}
		return result;
	}

}
