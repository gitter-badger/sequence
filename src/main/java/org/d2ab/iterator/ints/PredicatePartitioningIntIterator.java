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

package org.d2ab.iterator.ints;

import org.d2ab.collection.ints.IntList;
import org.d2ab.function.IntBiPredicate;
import org.d2ab.iterator.DelegatingTransformingIterator;
import org.d2ab.iterator.chars.CharIterator;
import org.d2ab.sequence.IntSequence;

import java.util.NoSuchElementException;

/**
 * A {@link CharIterator} that can batch up another iterator by comparing two items in sequence and deciding whether
 * to split up in a batch on those items.
 */
public class PredicatePartitioningIntIterator extends
                                              DelegatingTransformingIterator<Integer, IntIterator, IntSequence> {
	private final IntBiPredicate predicate;
	private int next;
	private boolean hasNext;

	public PredicatePartitioningIntIterator(IntIterator iterator, IntBiPredicate predicate) {
		super(iterator);
		this.predicate = predicate;
	}

	@Override
	public boolean hasNext() {
		if (!hasNext && super.hasNext()) {
			next = iterator.nextInt();
			hasNext = true;
		}
		return hasNext;
	}

	@Override
	public IntSequence next() {
		if (!hasNext())
			throw new NoSuchElementException();

		IntList buffer = IntList.create();
		do {
			buffer.addInt(next);

			hasNext = iterator.hasNext();
			if (!hasNext)
				break;

			int following = iterator.nextInt();
			boolean split = predicate.test(next, following);
			next = following;
			if (split)
				break;
		} while (hasNext);
		return IntSequence.from(buffer);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
