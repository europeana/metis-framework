package eu.europeana.enrichment.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.lang.NotImplementedException;

/**
 * Map Path to other objects.
 */
@SuppressWarnings("unchecked")
public class PathMap<T> {
	Map<Path, T> map = new IdentityMap();

	public T get(Path path) {
		return map.get(path);
	}

	/*
	 * Two update operations.
	 */
	public void put(Path path, T value) {
		map.put(path, value);
	}

	public void remove(Path path) {
		map.remove(path);
	}

	@Override
	public String toString() {
		String result = "Format: path = value \n";
		for (Path path : map.keySet()) {
			result += path + "=" + map.get(path) + "\n";
		}
		return result;
	}

	/**
	 * Assumes that it holds Paths with data, where each attribute has a value,
	 * and checks if there is a Path that satisfies the query.
	 * 
	 * 
	 * @param query
	 * @return
	 */
	public MatchResultIterable<T> ask(Path query) throws Exception {
		return matchAll(query, true);
	}

	/**
	 * Assumes that it holds Paths with queries, where an attribute may have no
	 * value, and checks if there is a Path that is answered by the data.
	 * 
	 * @param data
	 * @return
	 */
	public MatchResultIterable<T> answer(Path data) throws Exception {
		if (!data.isEmpty() && data.getLast().isAttributeQuery()) {
			throw new Exception("Cannot answer an attribute query " + data);
		}
		return matchAll(data, false);
	}

	private MatchResultIterable<T> matchAll(Path x, boolean ask)
			throws Exception {

		List<MatchResult<T>> result = new ArrayList<MatchResult<T>>();

		for (Path candidate : map.keySet()) {

			if (ask) {
				if (!candidate.isEmpty()
						&& candidate.getLast().isAttributeQuery()) {
					throw new Exception(
							"Attribute query path cannot server as  a data: "
									+ candidate);
				}
			}

			// element queries should have the same length
			// attribute queries should not be longer
			boolean lengthCheck = ask ? checkLength(x, candidate)
					: checkLength(candidate, x);

			if (lengthCheck) {
				MatchResult<T> matchResult = matchPath(candidate, x, ask);
				// evaluate match
				if (matchResult != null) {

					T value = map.get(candidate);
					matchResult.setAnsweredQuery(x);
					if (value != null) {
						matchResult.setStoredObject(value);
					}
					result.add(matchResult);
				}
			}

		}
		return new MatchResultIterable<T>(new MatchResultIterator<T>(result));
	}

	private boolean checkLength(Path query, Path candidate) {

		if (query.isEmpty()) {
			return candidate.isEmpty();
		} else {
			if (query.getLast().isAttributeQuery()) {
				// attribute query
				return query.size() <= candidate.size();
			} else {
				// element query
				return query.size() == candidate.size();
			}
		}
	}

	private MatchResult<T> matchPath(Path query, Path data, boolean ask)
			throws Exception {

		if (query.isEmpty() && data.isEmpty()) {
			return new MatchResult<T>();
		}
		MatchResult<T> matchResult = null;

		Iterator<PathElement> dataIterator = data.iterator();
		for (PathElement q : query) {

			// if (matchResult != null && matchResult.getResult() ==
			// MatchResult.Result.attribute_match) {
			// throw new Exception
			// ("Error: attribute query in the middle of a path: " + query);
			// }

			// data shorter than query
			if (!dataIterator.hasNext()) {
				break;
			}

			PathElement d = dataIterator.next();
			matchResult = ask ? matchPathElement(q, d) : matchPathElement(d, q);

			// mismatch, stop further evaluation
			if (matchResult == null) {
				break;
			}
		}
		// all matched
		return matchResult;
	}

	private MatchResult<T> matchPathElement(PathElement d, PathElement q)
			throws Exception {
		if (q.getExpanded().equals(d.getExpanded())) {

			// element match
			if (q.hasAttributes()) {
				// if query has attributes, match them

				// 1. query for attribute value (by attribute name, EL[@att])
				if (q.isAttributeQuery()) {

					String dataAttrValue = d.getAttributeValue(q
							.getQueryAttribute());

					if (dataAttrValue == null) {

						// failed on a attribute query
						return null;
					} else {

						// found attribute value
						return new MatchResult<T>(q.getQueryAttribute(),
								d.getAttributeValue(q.getQueryAttribute()));
					}

				} else {

					// 2. query for element value (by attribute-value pair)
					for (NamespacedName queryAttr : q.getAttributesAsSet()) {

						if (!q.getAttributeValue(queryAttr).equals(
								d.getAttributeValue(queryAttr))) {

							// an attr-value pair from query has no match in
							// data
							return null;
						}
					}
				}
			}
			return new MatchResult<T>();
		} else {
			return null;
		}
	}

	public Set<Path> keySet() {
		return map.keySet();
	}

	public boolean containsPath(Path path) {
		return map.containsKey(path);
	}

	public void clear() {

		for (Path path : map.keySet()) {
			path.clear();
		}

	}

	/**
	 * Matching result: {@link MatchResult#getAttributeValue()} hold the value
	 * in the case of an attribute value match of <code>null</code> in the case
	 * of an element value match.
	 * 
	 */
	public static class MatchResult<T> {
		private NamespacedName attributeName;
		private String attributeValue;
		private T storedObject;
		// private Path matchedData;
		private Path answeredQuery;

		public enum Result {
			element_match, attribute_match
		};

		private Result result;

		public Result getResult() {
			return result;
		}

		public T getStoredObject() {
			return storedObject;
		}

		public void setStoredObject(T storedObject) {
			this.storedObject = storedObject;
		}

		public String getAttributeValue() {
			return attributeValue;
		}

		public void setAnsweredQuery(Path answeredQuery) {
			this.answeredQuery = answeredQuery;
		}

		public MatchResult() {
			this.result = Result.element_match;
		}

		public MatchResult(NamespacedName attributeName, String attributeValue) {
			this.attributeValue = attributeValue;
			this.result = Result.attribute_match;
		}

		@Override
		public String toString() {
			return "MatchResult [result=" + result + ", attributeName="
					+ attributeName + ", attributeValue=" + attributeValue
					+ ", answeredQuery=" + answeredQuery + ", storedObject="
					+ storedObject + "]";
		}

	}

	public static class MatchResultIterator<T> implements
			Iterator<MatchResult<T>> {

		// TODO replace with more efficient storage
		Iterator<MatchResult<T>> iterator;

		boolean isEmpty = true;

		public MatchResultIterator(List<MatchResult<T>> list) {
			isEmpty = list.isEmpty();
			this.iterator = list.iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public MatchResult<T> next() {
			return iterator.next();
		}

		@Override
		public void remove() {
			throw new NotImplementedException();
		}

		public boolean isEmpty() {
			return isEmpty;
		}

	}

	public static class MatchResultIterable<T> implements
			Iterable<MatchResult<T>> {

		private MatchResultIterator<T> iterator;

		public MatchResultIterable(MatchResultIterator<T> iterator) {
			super();
			this.iterator = iterator;
		}

		@Override
		public Iterator<MatchResult<T>> iterator() {
			return iterator;
		}

		public boolean isEmpty() {
			return iterator.isEmpty();
		}
	}
}
