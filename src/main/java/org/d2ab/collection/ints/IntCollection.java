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

package org.d2ab.collection.ints;

import org.d2ab.collection.Collectionz;
import org.d2ab.collection.chars.CharCollection;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.util.Strict;

import java.util.Collection;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * A primitive specialization of {@link Collection} for {code int} values. Supplements all {@link Integer}-valued
 * methods with corresponding {@code int}-valued methods.
 */
public interface IntCollection extends Collection<Integer>, IntIterable {
	// TODO: Extract out relevant parts to IterableIntCollection

	@Override
	default boolean isEmpty() {
		return size() == 0;
	}

	@Override
	default void clear() {
		iterator().removeAll();
	}

	@Override
	default Integer[] toArray() {
		assert Strict.LENIENT : "IntCollection.toArray()";

		return toArray(new Integer[size()]);
	}

	@Override
	default <T> T[] toArray(T[] a) {
		assert Strict.LENIENT : "IntCollection.toArray(Object[])";

		return Collectionz.toArray(this, a);
	}

	/**
	 * Collect the {@code ints} in this {@code IntCollection} into an {@code int}-array.
	 */
	default int[] toIntArray() {
		return new ArrayIntList(this).toIntArray();
	}

	/**
	 * @return an {@link IntList} view of this {@code IntCollection}, which is updated as the {@code IntCollection}
	 * changes. The list does not implement {@link RandomAccess} and is best accessed in sequence.
	 *
	 * @since 2.2
	 */
	default IntList asList() {
		return IntList.Base.from(this);
	}

	@Override
	default boolean add(Integer x) {
		assert Strict.LENIENT : "IntCollection.add(Integer)";

		return addInt(x);
	}

	default boolean addInt(int x) {
		throw new UnsupportedOperationException();
	}

	@Override
	default boolean contains(Object o) {
		assert Strict.LENIENT : "IntCollection.contains(Object)";

		return o instanceof Integer && containsInt((int) o);
	}

	@Override
	default boolean remove(Object o) {
		assert Strict.LENIENT : "IntCollection.remove(Object)";

		return o instanceof Integer && removeInt((int) o);
	}

	@Override
	default boolean addAll(Collection<? extends Integer> c) {
		assert Strict.LENIENT : "IntCollection.add(Collection)";

		return Collectionz.addAll(this, c);
	}

	default boolean addAllInts(int... xs) {
		boolean changed = false;
		for (int x : xs)
			changed |= addInt(x);
		return changed;
	}

	default boolean addAllInts(IntCollection xs) {
		if (xs.isEmpty())
			return false;

		xs.forEachInt(this::addInt);
		return true;
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		assert Strict.LENIENT : "IntCollection.containsAll(Collection)";

		return Collectionz.containsAll(this, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		assert Strict.LENIENT : "IntCollection.removeAll(Collection)";

		return Collectionz.removeAll(this, c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		assert Strict.LENIENT : "IntCollection.retainAll(Collection)";

		return Collectionz.retainAll(this, c);
	}

	@Override
	default boolean removeIf(Predicate<? super Integer> filter) {
		assert Strict.LENIENT : "IntCollection.removeIf(Predicate)";

		return removeIntsIf(filter::test);
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL);
	}

	@Override
	default CharCollection asChars() {
		return new CharCollection() {
			@Override
			public CharIterator iterator() {
				return CharIterator.from(IntCollection.this.iterator());
			}

			@Override
			public int size() {
				return IntCollection.this.size();
			}
		};
	}

	/**
	 * Base class for {@link IntCollection} implementations.
	 */
	abstract class Base implements IntCollection {
		public static IntCollection create(int... ints) {
			return from(IntList.create(ints));
		}

		public static IntCollection from(final IntCollection collection) {
			return new Base() {
				@Override
				public IntIterator iterator() {
					return collection.iterator();
				}

				@Override
				public int size() {
					return collection.size();
				}

				@Override
				public boolean addInt(int x) {
					return collection.addInt(x);
				}
			};
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(size() * 5); // heuristic
			builder.append("[");

			boolean tail = false;
			for (IntIterator iterator = iterator(); iterator.hasNext(); ) {
				if (tail)
					builder.append(", ");
				else
					tail = true;
				builder.append(iterator.nextInt());
			}

			builder.append("]");
			return builder.toString();
		}
	}
}
