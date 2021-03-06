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

import org.d2ab.iterator.doubles.DoubleIterator;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.d2ab.test.IsDoubleIterableContainingInOrder.containsDoubles;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class DoubleCollectionAsListTest {
	private final DoubleList emptyList = DoubleCollection.Base.from(DoubleList.create()).asList();
	private final DoubleList list = DoubleCollection.Base.from(DoubleList.create(1, 2, 3, 4, 5, 1, 2, 3, 4, 5))
	                                                     .asList();

	@Test
	public void subList() {
		DoubleList subList = list.subList(2, 8);
		twice(() -> assertThat(subList, containsDoubles(3, 4, 5, 1, 2, 3)));

		assertThat(subList.removeDoubleAt(1), is(4.0));
		twice(() -> assertThat(subList, containsDoubles(3, 5, 1, 2, 3)));

		assertThat(subList.removeDoubleExactly(5), is(true));
		twice(() -> assertThat(subList, containsDoubles(3, 1, 2, 3)));

		DoubleIterator subListIterator = subList.iterator();
		assertThat(subListIterator.hasNext(), is(true));
		assertThat(subListIterator.nextDouble(), is(3.0));
		subListIterator.remove();
		twice(() -> assertThat(subList, containsDoubles(1, 2, 3)));

		subList.removeDoublesIf(x -> x % 2 == 0);
		twice(() -> assertThat(subList, containsDoubles(1, 3)));

		subList.clear();
		twice(() -> assertThat(subList, is(emptyIterable())));
	}

	@Test
	public void size() {
		assertThat(emptyList.size(), is(0));
		assertThat(list.size(), is(10));
	}

	@Test
	public void isEmpty() {
		assertThat(emptyList.isEmpty(), is(true));
		assertThat(list.isEmpty(), is(false));
	}

	@Test
	public void containsDoubleExactly() {
		assertThat(emptyList.containsDoubleExactly(2), is(false));
		for (double i = 1; i < 5; i++)
			assertThat(list.containsDoubleExactly(i), is(true));
		assertThat(list.containsDoubleExactly(17), is(false));
	}

	@Test
	public void iterator() {
		assertThat(emptyList, is(emptyIterable()));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void iteratorRemove() {
		DoubleIterator iterator = list.iterator();
		iterator.nextDouble();
		iterator.nextDouble();
		iterator.remove();
		iterator.nextDouble();
		iterator.remove();

		assertThat(list, containsDoubles(1, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void toDoubleArray() {
		assertArrayEquals(new double[0], emptyList.toDoubleArray(), 0.0);
		assertArrayEquals(new double[]{1, 2, 3, 4, 5, 1, 2, 3, 4, 5}, list.toDoubleArray(), 0.0);
	}

	@Test
	public void addDoubleExactly() {
		assertThat(emptyList.addDoubleExactly(1), is(true));
		assertThat(emptyList, containsDoubles(1));

		assertThat(list.addDoubleExactly(6), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void removeDoubleExactly() {
		assertThat(emptyList.removeDoubleExactly(17), is(false));

		assertThat(list.removeDoubleExactly(2), is(true));
		assertThat(list, containsDoubles(1, 3, 4, 5, 1, 2, 3, 4, 5));

		assertThat(list.removeDoubleExactly(17), is(false));
		assertThat(list, containsDoubles(1, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void containsAllDoublesExactly() {
		assertThat(emptyList.containsAllDoublesExactly(DoubleList.create(2, 3)), is(false));

		assertThat(list.containsAllDoublesExactly(DoubleList.create(2, 3)), is(true));
		assertThat(list.containsAllDoublesExactly(DoubleList.create(2, 17)), is(false));
	}

	@Test
	public void addAllDoubles() {
		assertThat(emptyList.addAllDoubles(DoubleList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllDoubles(DoubleList.create(1, 2)));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllDoubles(DoubleList.create(6, 7, 8)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addAllDoublesAt() {
		assertThat(emptyList.addAllDoublesAt(0, DoubleList.create()), is(false));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> emptyList.addAllDoublesAt(0, DoubleList.create(1, 2)));
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.addAllDoublesAt(2, DoubleList.create(17, 18, 19)));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void removeAllDoublesExactly() {
		assertThat(emptyList.removeAllDoublesExactly(DoubleList.create(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.removeAllDoublesExactly(DoubleList.create(1, 2, 5)), is(true));
		assertThat(list, containsDoubles(3, 4, 3, 4));
	}

	@Test
	public void retainAllDoublesExactly() {
		assertThat(emptyList.retainAllDoublesExactly(DoubleList.create(1, 2)), is(false));
		assertThat(emptyList, is(emptyIterable()));

		assertThat(list.retainAllDoublesExactly(DoubleList.create(1, 2, 3)), is(true));
		assertThat(list, containsDoubles(1, 2, 3, 1, 2, 3));
	}

	@Test
	public void replaceAllDoubles() {
		emptyList.replaceAllDoubles(x -> x + 1);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, () -> list.replaceAllDoubles(x -> x + 1));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void sortDoubles() {
		expecting(UnsupportedOperationException.class, emptyList::sortDoubles);
		assertThat(emptyList, is(emptyIterable()));

		expecting(UnsupportedOperationException.class, list::sortDoubles);
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void clear() {
		emptyList.clear();
		assertThat(emptyList, is(emptyIterable()));

		list.clear();
		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void testEquals() {
		assertThat(emptyList.equals(emptyList()), is(true));
		assertThat(emptyList.equals(asList(1.0, 2.0)), is(false));

		assertThat(list.equals(asList(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0)), is(true));
		assertThat(list.equals(asList(5.0, 4.0, 3.0, 2.0, 1.0, 5.0, 4.0, 3.0, 2.0, 1.0)), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(emptyList.hashCode(), is(emptyList().hashCode()));
		assertThat(list.hashCode(), is(asList(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0).hashCode()));
	}

	@Test
	public void testToString() {
		assertThat(emptyList.toString(), is("[]"));
		assertThat(list.toString(), is("[1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0]"));
	}

	@Test
	public void getDouble() {
		assertThat(list.getDouble(0), is(1.0));
		assertThat(list.getDouble(2), is(3.0));
		assertThat(list.getDouble(4), is(5.0));
		assertThat(list.getDouble(5), is(1.0));
		assertThat(list.getDouble(7), is(3.0));
		assertThat(list.getDouble(9), is(5.0));
	}

	@Test
	public void setDouble() {
		expecting(UnsupportedOperationException.class, () -> list.setDouble(2, 17));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void addDoubleAt() {
		expecting(UnsupportedOperationException.class, () -> list.addDoubleAt(0, 17));
		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void indexOfDoubleExactly() {
		assertThat(emptyList.indexOfDoubleExactly(17), is(-1));

		assertThat(list.indexOfDoubleExactly(1), is(0));
		assertThat(list.indexOfDoubleExactly(3), is(2));
		assertThat(list.indexOfDoubleExactly(5), is(4));
	}

	@Test
	public void lastIndexOfDoubleExactly() {
		assertThat(emptyList.lastIndexOfDoubleExactly(17), is(-1));

		assertThat(list.lastIndexOfDoubleExactly(1), is(5));
		assertThat(list.lastIndexOfDoubleExactly(3), is(7));
		assertThat(list.lastIndexOfDoubleExactly(5), is(9));
	}

	@Test
	public void listIteratorEmpty() {
		DoubleListIterator emptyIterator = emptyList.listIterator();
		assertThat(emptyIterator.hasNext(), is(false));
		expecting(NoSuchElementException.class, emptyIterator::nextDouble);
		expecting(UnsupportedOperationException.class, emptyIterator::hasPrevious);
		expecting(UnsupportedOperationException.class, emptyIterator::previousDouble);
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		expecting(UnsupportedOperationException.class, () -> emptyIterator.add(17));
		assertThat(emptyIterator.hasNext(), is(false));
		assertThat(emptyIterator.nextIndex(), is(0));
		assertThat(emptyIterator.previousIndex(), is(-1));

		assertThat(emptyList, is(emptyIterable()));
	}

	@Test
	public void listIterator() {
		DoubleListIterator listIterator = list.listIterator();

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(0));
		assertThat(listIterator.previousIndex(), is(-1));
		assertThat(listIterator.nextDouble(), is(1.0));

		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(1));
		assertThat(listIterator.previousIndex(), is(0));
		assertThat(listIterator.nextDouble(), is(2.0));

		expecting(UnsupportedOperationException.class, () -> listIterator.add(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(2));
		assertThat(listIterator.previousIndex(), is(1));
		assertThat(listIterator.nextDouble(), is(3.0));

		expecting(UnsupportedOperationException.class, () -> listIterator.set(17));
		assertThat(listIterator.hasNext(), is(true));
		assertThat(listIterator.nextIndex(), is(3));
		assertThat(listIterator.previousIndex(), is(2));
		assertThat(listIterator.nextDouble(), is(4.0));

		assertThat(list, containsDoubles(1, 2, 3, 4, 5, 1, 2, 3, 4, 5));
	}

	@Test
	public void exhaustiveListIterator() {
		DoubleListIterator listIterator = list.listIterator();

		AtomicInteger i = new AtomicInteger(0);
		twice(() -> {
			while (listIterator.hasNext()) {
				assertThat(listIterator.nextDouble(), is((double) (i.get() % 5 + 1)));
				assertThat(listIterator.nextIndex(), is(i.get() + 1));
				assertThat(listIterator.previousIndex(), is(i.get()));
				i.incrementAndGet();
			}
			assertThat(i.get(), is(10));
		});
	}

	@Test
	public void listIteratorRemoveAll() {
		DoubleIterator iterator = list.iterator();

		int i = 0;
		while (iterator.hasNext()) {
			assertThat(iterator.nextDouble(), is((double) (i % 5 + 1)));
			iterator.remove();
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void listIteratorRemove() {
		DoubleListIterator listIterator = list.listIterator();

		int i = 0;
		while (listIterator.hasNext()) {
			assertThat(listIterator.nextDouble(), is((double) (i % 5 + 1)));
			assertThat(listIterator.nextIndex(), is(1));
			assertThat(listIterator.previousIndex(), is(0));
			listIterator.remove();
			assertThat(listIterator.nextIndex(), is(0));
			assertThat(listIterator.previousIndex(), is(-1));
			i++;
		}
		assertThat(i, is(10));

		assertThat(list, is(emptyIterable()));
	}

	@Test
	public void stream() {
		assertThat(emptyList.stream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.stream().collect(Collectors.toList()),
		           contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void parallelStream() {
		assertThat(emptyList.parallelStream().collect(Collectors.toList()), is(emptyIterable()));
		assertThat(list.parallelStream().collect(Collectors.toList()),
		           contains(1.0, 2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, 5.0));
	}

	@Test
	public void removeDoublesIf() {
		emptyList.removeDoublesIf(x -> x == 1);
		assertThat(emptyList, is(emptyIterable()));

		list.removeDoublesIf(x -> x == 1);
		assertThat(list, containsDoubles(2, 3, 4, 5, 2, 3, 4, 5));
	}

	@Test
	public void forEachDouble() {
		emptyList.forEachDouble(x -> {
			throw new IllegalStateException("Should not get called");
		});

		AtomicInteger i = new AtomicInteger(0);
		list.forEachDouble(x -> assertThat(x, is((double) (i.getAndIncrement() % 5 + 1))));
		assertThat(i.get(), is(10));
	}
}
