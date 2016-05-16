/*
 * Copyright 2016 Daniel Skogquist Åborg
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
 */

package org.d2ab.collection;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code int} values. Supplements all {@link Integer}-valued
 * methods with corresponding {@code int}-valued methods.
 */
public interface IntCollection extends Collection<Integer>, IntIterable {
	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default boolean add(Integer integer) {
		return addInt(integer);
	}

	default boolean addInt(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		return containsInt((int) o);
	}

	@Override
	default boolean remove(Object o) {
		return removeInt((int) o);
	}

	/**
	 * Collect the {@code ints} in this {@code IntCollection} into an {@code int}-array.
	 */
	default int[] toIntArray() {
		return iterator().toArray(new int[size()]);
	}

	@Override
	default Integer[] toArray() {
		return toArray(new Integer[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return Collectionz.toArray(this, a);
	}

	default boolean addAll(IntCollection c) {
		if (c.isEmpty())
			return false;

		c.forEachInt(this::addInt);
		return true;
	}

	default boolean addAll(int... is) {
		boolean changed = false;
		for (int i : is)
			changed |= addInt(i);
		return changed;
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		return Collectionz.addAll(this, c);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		return removeIntsIf((IntPredicate) filter);
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), 0);
	}
}
