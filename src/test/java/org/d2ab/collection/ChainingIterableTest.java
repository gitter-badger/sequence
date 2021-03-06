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

import org.junit.Test;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.d2ab.test.Tests.expecting;
import static org.d2ab.test.Tests.twice;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ChainingIterableTest {
	private final Iterable<String> empty = ChainingIterable.empty();

	private final Iterable<String> abc = ChainingIterable.concat(asList("a", "b", "c"));

	private final Iterable<String> abc_def =
			ChainingIterable.concat(asList("a", "b", "c"), asList("d", "e", "f"));

	private final Iterable<String> abc_def_ghi =
			ChainingIterable.concat(asList("a", "b", "c"), asList("d", "e", "f"), asList("g", "h", "i"));

	@Test
	public void empty() {
		twice(() -> assertThat(empty, is(emptyIterable())));
	}

	@Test
	public void one() {
		twice(() -> assertThat(abc, contains("a", "b", "c")));
	}

	@Test
	public void two() {
		twice(() -> assertThat(abc_def, contains("a", "b", "c", "d", "e", "f")));
	}

	@Test
	public void three() {
		twice(() -> assertThat(abc_def_ghi, contains("a", "b", "c", "d", "e", "f", "g", "h", "i")));
	}

	@Test
	public void lazy() {
		@SuppressWarnings("unchecked")
		Iterable<String> chainingIterable = ChainingIterable.concat(Iterables.of("a", "b", "c"), () -> {
			throw new IllegalStateException(); // Not thrown yet, until below when iterator is requested
		});

		Iterator<String> iterator = chainingIterable.iterator();
		assertThat(iterator.next(), is("a"));
		assertThat(iterator.next(), is("b"));
		assertThat(iterator.next(), is("c"));

		// Exception not thrown until iterator is encountered
		expecting(IllegalStateException.class, iterator::hasNext);
	}
}
