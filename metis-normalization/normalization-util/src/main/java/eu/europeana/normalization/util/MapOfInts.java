/*
 * Created on Oct 12, 2004
 *
 */
package eu.europeana.normalization.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * @param <K>
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11 de Abr de 2013
 */
public class MapOfInts<K> extends Hashtable<K, Integer> implements Serializable{
	
		private static final long serialVersionUID=1;	
		
		//Hashtable<K,Object[]> hashtable;
//		Hashtable<K,Integer> hashtable;
//		int listInitialCapacity=-1;
		
		/**
		 * Creates a new instance of this class.
		 */
		public MapOfInts(){
			super();
		}

		/**
		 * Creates a new instance of this class.
		 * @param initialCapacity
		 */
		public MapOfInts(int initialCapacity){
			super(initialCapacity);
		}

		
		/**
		 * @return sum of all ints
		 */
		public int total() {
			int total=0;
			for(K key: keySet()) {
				total+=get(key);
			}
			return total;
		}
		
		

		/**
		 * @param key
		 * @param value
		 */
		public void addTo(K key, Integer value){
			Integer v=get(key);
			if(v!=null) {
				put(key,value+v);
			} else {
				put(key,value);
			}
		}
		
		/**
		 * @param key
		 */
		public void incrementTo(K key){
			Integer v=get(key);
			if(v!=null) {
				put(key,1+v);
			} else {
				put(key,1);
			}
		}

		/**
		 * @param key
		 * @param value
		 */
		public void subtractTo(K key, Integer value){
			Integer v=get(key);
			if(v!=null) {
				put(key, value-v);
			} else {
				put(key, -value);
			}
		}
		
		/**
		 * @param key
		 */
		public void decrementTo(K key){
			Integer v=get(key);
			if(v!=null) {
				put(key,v-1);
			} else {
				put(key,-1);
			}
		}
		
}
