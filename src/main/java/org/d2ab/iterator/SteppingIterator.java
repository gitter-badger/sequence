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

package org.d2ab.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.d2ab.iterator.SteppingIterator.State.*;

public class SteppingIterator<T> extends DelegatingUnaryIterator<T> {
	private final int step;

	private boolean skipOnHasNext;

	enum State {INIT, HAS_NEXT, NEXT}

	private State state = INIT;

	public SteppingIterator(Iterator<T> iterator, int step) {
		super(iterator);
		this.step = step;
	}

	@Override
	public boolean hasNext() {
		if (skipOnHasNext) {
			Iterators.skip(iterator, step - 1);
			skipOnHasNext = false;
		}

		state = HAS_NEXT;
		return iterator.hasNext();
	}

	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();

		skipOnHasNext = true;
		state = NEXT;
		return iterator.next();
	}

	@Override
	public void remove() {
		if (state == HAS_NEXT)
			throw new IllegalStateException("Cannot remove immediately after calling hasNext()");
		if (state != NEXT)
			throw new IllegalStateException("Can only remove after calling next()");

		super.remove();
	}
}
