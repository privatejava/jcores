/*
 * MapEntry.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.jcores.utils;


/**
 * A MapEntry is a key, value pair from a map. 
 * 
 * @author Ralf Biedert
 *
 * @param <K>
 * @param <V>
 * @since 1.0
 */
public class MapEntry<K, V> implements Comparable<MapEntry<K, V>> {
	/** */
	private K key;
	
	/** */
	private V value;

	/**
	 * Creates the key-value entry.
	 * 
	 * @param key The key.
	 * @param value The value.
	 */
	public MapEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Returns the key entry.
	 * 
	 * @return The key entry.
	 */
	public K key() {
		return this.key;
	}
	
	/**
	 * Returns the value entry.
	 * 
	 * @return The value entry.
	 */
	public V value() {
		return this.value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.key.toString() + ": " + this.value.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(MapEntry<K, V> o) {
		if(key instanceof Comparable) {
			return ((Comparable) key).compareTo(o);
		}
		return 0;
	}
}
	