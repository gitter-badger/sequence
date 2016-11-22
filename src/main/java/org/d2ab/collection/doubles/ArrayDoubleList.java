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

package org.d2ab.collection.doubles;

import org.d2ab.collection.Arrayz;
import org.d2ab.iterator.doubles.DoubleIterator;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

/**
 * A {@link DoubleList} backed by a double-array, supporting all {@link DoubleList}-methods by modifying and/or replacing the
 * underlying array.
 */
public class ArrayDoubleList implements DoubleList {
	private double[] contents;
	private int size;

	public static ArrayDoubleList of(double... contents) {
		return new ArrayDoubleList(contents);
	}

	public ArrayDoubleList() {
		this(10);
	}

	public ArrayDoubleList(int capacity) {
		this.contents = new double[capacity];
	}

	public ArrayDoubleList(DoubleCollection xs) {
		this();
		addAllDoubles(xs);
	}

	/**
	 * Private to avoid conflict with standard int-taking capacity constructor.
	 * Use {@link #of(double...)} for public access.
	 *
	 * @see #ArrayDoubleList(int)
	 * @see #of(double...)
	 */
	private ArrayDoubleList(double... contents) {
		this.contents = Arrays.copyOf(contents, contents.length);
		this.size = contents.length;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public double[] toDoubleArray() {
		return Arrays.copyOfRange(contents, 0, size);
	}

	@Override
	public DoubleIterator iterator() {
		return listIterator();
	}

	@Override
	public DoubleListIterator listIterator(int index) {
		rangeCheckForAdd(index);
		return new ListIter(index);
	}

	@Override
	public DoubleList subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sortDoubles() {
		Arrays.sort(contents, 0, size);
	}

	@Override
	public int binarySearchExactly(double x) {
		return Arrays.binarySearch(contents, 0, size, x);
	}

	@Override
	public void replaceAllDoubles(DoubleUnaryOperator operator) {
		for (int i = 0; i < size; i++)
			contents[i] = operator.applyAsDouble(contents[i]);
	}

	@Override
	public double getDouble(int index) {
		rangeCheck(index);
		return contents[index];
	}

	@Override
	public double setDouble(int index, double x) {
		rangeCheck(index);
		double previous = contents[index];
		contents[index] = x;
		return previous;
	}

	@Override
	public void addDoubleAt(int index, double x) {
		rangeCheckForAdd(index);
		uncheckedAdd(index, x);
	}

	@Override
	public double removeDoubleAt(int index) {
		rangeCheck(index);
		double previous = contents[index];
		uncheckedRemove(index);
		return previous;
	}

	@Override
	public int lastIndexOfDoubleExactly(double x) {
		for (int i = size - 1; i >= 0; i--)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public int indexOfDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return i;

		return -1;
	}

	@Override
	public Spliterator.OfDouble spliterator() {
		return Arrays.spliterator(contents, 0, size);
	}

	@Override
	public boolean addDouble(double x) {
		growIfNecessaryBy(1);
		contents[size++] = x;
		return true;
	}

	@Override
	public boolean addAllDoubles(double... xs) {
		if (xs.length == 0)
			return false;

		growIfNecessaryBy(xs.length);
		System.arraycopy(xs, 0, contents, size, xs.length);
		size += xs.length;
		return true;
	}

	@Override
	public boolean addAllDoubles(DoubleCollection xs) {
		if (xs.isEmpty())
			return false;

		if (xs instanceof ArrayDoubleList) {
			ArrayDoubleList axs = (ArrayDoubleList) xs;

			growIfNecessaryBy(axs.size);
			System.arraycopy(axs.contents, 0, contents, size, axs.size);
			size += axs.size;

			return true;
		} else {
			xs.forEachDouble(this::addDouble);
			return true;
		}
	}

	@Override
	public boolean addAllDoublesAt(int index, double... xs) {
		if (xs.length == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xs.length);
		System.arraycopy(contents, index, contents, index + xs.length, size - index);
		System.arraycopy(xs, 0, contents, index, xs.length);
		size += xs.length;
		return true;
	}

	@Override
	public boolean addAllDoublesAt(int index, DoubleCollection xs) {
		if (xs.size() == 0)
			return false;

		rangeCheckForAdd(index);
		growIfNecessaryBy(xs.size());
		System.arraycopy(contents, index, contents, index + xs.size(), size - index);

		if (xs instanceof ArrayDoubleList) {
			ArrayDoubleList il = (ArrayDoubleList) xs;
			System.arraycopy(il.contents, 0, contents, index, il.size);
		} else {
			DoubleIterator iterator = xs.iterator();
			for (int i = index; i < xs.size(); i++)
				contents[i] = iterator.nextDouble();
		}

		size += xs.size();

		return true;
	}

	@Override
	public boolean containsAllDoublesExactly(double... xs) {
		for (double x : xs)
			if (!containsDoubleExactly(x))
				return false;

		return true;
	}

	@Override
	public boolean removeDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return uncheckedRemove(i);

		return false;
	}

	@Override
	public boolean containsDoubleExactly(double x) {
		for (int i = 0; i < size; i++)
			if (contents[i] == x)
				return true;

		return false;
	}

	@Override
	public boolean removeAllDoublesExactly(double... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (Arrayz.containsExactly(xs, contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public boolean retainAllDoublesExactly(double... xs) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (!Arrayz.containsExactly(xs, contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public boolean removeDoublesIf(DoublePredicate filter) {
		boolean modified = false;
		for (int i = 0; i < size; i++)
			if (filter.test(contents[i]))
				modified |= uncheckedRemove(i--);
		return modified;
	}

	@Override
	public void forEachDouble(DoubleConsumer consumer) {
		for (int i = 0; i < size; i++)
			consumer.accept(contents[i]);
	}

	private void growIfNecessaryBy(int grow) {
		int newSize = size + grow;
		if (newSize > contents.length) {
			int newCapacity = newSize + (newSize >> 1);
			double[] copy = new double[newCapacity];
			System.arraycopy(contents, 0, copy, 0, size);
			contents = copy;
		}
	}

	private boolean uncheckedAdd(int index, double x) {
		growIfNecessaryBy(1);
		System.arraycopy(contents, index, contents, index + 1, size++ - index);
		contents[index] = x;
		return true;
	}

	private boolean uncheckedRemove(int index) {
		System.arraycopy(contents, index + 1, contents, index, size-- - index - 1);
		return true;
	}

	private void rangeCheck(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private void rangeCheckForAdd(int index) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("index: " + index + " size: " + size);
	}

	private class ListIter implements DoubleListIterator {
		protected int nextIndex;
		protected int currentIndex;
		protected boolean addOrRemove;
		protected boolean nextOrPrevious;

		private ListIter(int index) {
			this.nextIndex = index;
			this.currentIndex = index - 1;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public double nextDouble() {
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[currentIndex = nextIndex++];
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public double previousDouble() {
			addOrRemove = false;
			nextOrPrevious = true;
			return contents[currentIndex = --nextIndex];
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public void remove() {
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextDouble() or previousDouble() not called");

			uncheckedRemove(nextIndex = currentIndex--);
			addOrRemove = true;
		}

		@Override
		public void set(double x) {
			if (addOrRemove)
				throw new IllegalStateException("add() or remove() called");
			if (!nextOrPrevious)
				throw new IllegalStateException("nextDouble() or previousDouble() not called");

			contents[currentIndex] = x;
		}

		@Override
		public void add(double x) {
			uncheckedAdd(currentIndex = nextIndex++, x);
			addOrRemove = true;
		}
	}
}