/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.biginsights.textanalytics.patterndiscovery.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a list with exactly one element
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
@SuppressWarnings("unchecked")
public class SingletonList<T> implements List {


	
	T element;
	public SingletonList(T element){
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Object e) {
		// cannot add to a singleton list
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		throw new RuntimeException("cannot add to a SingletonList");

	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		throw new RuntimeException("cannot add to a SingletonList");
	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		throw new RuntimeException("cannot add to a SingletonList");	}

	/* (non-Javadoc)
	 * @see java.util.List#clear()
	 */
	public void clear() {
		element = null;

	}

	/* (non-Javadoc)
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		if(element == null) return false;
		else return element.equals(o);
	}

	/* (non-Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		for (Object o : c) {
			if(! o.equals(element)){
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) {
		if (index == 0) return element;
		else throw new IndexOutOfBoundsException();
	}

	/* (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		if(o.equals(element))	return 0;
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return element == null;
	}

	/* (non-Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator iterator() {
		return new SingletonIterator<T>(element);
	}

	/* (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return new SingletonIterator<T>(element);
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		if(index == 0)
			return new SingletonIterator<T>(element);
		return new SingletonIterator<T>(null);
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if(o.equals(element)){
			element = null;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		if(index == 0){
			T buf = element;
			element = null;
			return buf;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		if(c.contains(element)){
			element = null;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		if(!c.contains(element)){
			element = null;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		if(index == 0){
			this.element = (T)element;
			return element;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.List#size()
	 */
	public int size() {
		if(element == null){
			return 0;
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		if(element==null||fromIndex==0&&toIndex==0)
			return new ArrayList(0);
		if(fromIndex==0&&toIndex==1)
			return new SingletonList(element);
		throw new IndexOutOfBoundsException();
	}

	/* (non-Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		if(element==null){
			return new Object[0];
		}
		Object[] result = new Object[1];
		result[0] = element;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.util.List#toArray(T[])
	 */
	public Object[] toArray(Object[] a) {
		if(element==null){
			return a;
		}
		a[0] = element;
		return a;
	}
	public String toString(){
		return "["+element+"]";
	}
	
	@SuppressWarnings("hiding")
	public class SingletonIterator<T> implements ListIterator{
		T element;
		boolean expired;
		public SingletonIterator(T element){
			this.element = element;
			expired = false;
			if(element == null){
				expired = true;
			}
		}
		public boolean hasNext() {
			return !expired;
		}
		public Object next() {
			if (! expired){
				expired = true;
				return element;
			}
			return null;
		}
		public void remove() {
			element = null;
		}
		public void add(Object e) {
			throw new RuntimeException("cannot add to a SingletonList");
			
		}
		public boolean hasPrevious() {
			
			return !expired;
		}
		public int nextIndex() {
			if(!expired)
			return 0;
			return 1;
		}
		public Object previous() {
			if(expired){
				expired = false;
				return element;
				
			}
			return null;
		}
		public int previousIndex() {
			if(expired)
			return 0;
			return -1;
		}
		public void set(Object e) {
			element = (T)e;
			
		}
	
		
	}

}
