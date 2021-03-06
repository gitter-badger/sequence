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

package org.d2ab.sequence;

import org.d2ab.collection.Arrayz;
import org.d2ab.collection.doubles.*;
import org.d2ab.function.DoubleBiPredicate;
import org.d2ab.function.DoubleIntConsumer;
import org.d2ab.function.DoubleIntPredicate;
import org.d2ab.function.DoubleIntToDoubleFunction;
import org.d2ab.iterator.Iterators;
import org.d2ab.iterator.doubles.*;
import org.d2ab.iterator.ints.IntIterator;
import org.d2ab.iterator.longs.LongIterator;

import java.util.*;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.Collections.emptyIterator;

/**
 * An {@link Iterable} sequence of {@code double} values with {@link Stream}-like operations for refining,
 * transforming and collating the list of doubles.
 */
@FunctionalInterface
public interface DoubleSequence extends DoubleCollection {
	/**
	 * Create empty {@code DoubleSequence} with no contents.
	 */
	static DoubleSequence empty() {
		return once(emptyIterator());
	}

	/**
	 * Create a {@code DoubleSequence} with the given doubles.
	 */
	static DoubleSequence of(double... ds) {
		return () -> DoubleIterator.of(ds);
	}

	/**
	 * Create an {@code DoubleSequence} with the given {@code doubles}, limited to the given size.
	 */
	static DoubleSequence from(double[] is, int size) {
		return () -> DoubleIterator.from(is, size);
	}

	/**
	 * Create an {@code DoubleSequence} with the given {@code doubles}, reading from the given offset and limited to
	 * the
	 * given size.
	 */
	static DoubleSequence from(double[] is, int offset, int size) {
		return () -> DoubleIterator.from(is, offset, size);
	}

	/**
	 * Create a {@code DoubleSequence} from a {@link DoubleIterable}.
	 *
	 * @see #cache(DoubleIterable)
	 */
	static DoubleSequence from(DoubleIterable iterable) {
		return iterable::iterator;
	}

	/**
	 * Create a {@code DoubleSequence} from an {@link Iterable} of {@code Double} values.
	 *
	 * @see #cache(Iterable)
	 */
	static DoubleSequence from(Iterable<Double> iterable) {
		return from(DoubleIterable.from(iterable));
	}

	/**
	 * Create a once-only {@code DoubleSequence} from a {@link PrimitiveIterator.OfDouble} of double values. Note that
	 * {@code DoubleSequence}s created from {@link PrimitiveIterator.OfDouble}s cannot be passed over more than once.
	 * Further attempts will register the {@code DoubleSequence} as empty.
	 *
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @since 1.1
	 */
	static DoubleSequence once(PrimitiveIterator.OfDouble iterator) {
		return from(DoubleIterable.once(iterator));
	}

	/**
	 * Create a once-only {@code DoubleSequence} from an {@link Iterator} of {@code Double} values. Note that
	 * {@code DoubleSequence}s created from {@link Iterator}s cannot be passed over more than once. Further attempts
	 * will register the {@code DoubleSequence} as empty.
	 *
	 * @see #cache(Iterator)
	 * @since 1.1
	 */
	static DoubleSequence once(Iterator<Double> iterator) {
		return once(DoubleIterator.from(iterator));
	}

	/**
	 * Create a once-only {@code DoubleSequence} from a {@link DoubleStream} of items. Note that
	 * {@code DoubleSequence}s created from {@link DoubleStream}s cannot be passed over more than once. Further
	 * attempts will register the {@code DoubleSequence} as empty.
	 *
	 * @throws IllegalStateException if the {@link DoubleStream} is exhausted.
	 * @see #cache(DoubleStream)
	 * @since 1.1
	 */
	static DoubleSequence once(DoubleStream stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a once-only {@code DoubleSequence} from a {@link Stream} of items. Note that {@code DoubleSequence}s
	 * created from {@link Stream}s cannot be passed over more than once. Further attempts will register the
	 * {@code DoubleSequence} as empty.
	 *
	 * @throws IllegalStateException if the {@link Stream} is exhausted.
	 * @see #cache(Stream)
	 * @since 1.1
	 */
	static DoubleSequence once(Stream<Double> stream) {
		return once(stream.iterator());
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of a {@link PrimitiveIterator.OfDouble}.
	 *
	 * @see #cache(Iterator)
	 * @see #cache(DoubleStream)
	 * @see #cache(Stream)
	 * @see #cache(DoubleIterable)
	 * @see #cache(Iterable)
	 * @see #once(PrimitiveIterator.OfDouble)
	 * @since 1.1
	 */
	static DoubleSequence cache(PrimitiveIterator.OfDouble iterator) {
		return from(DoubleList.copy(iterator));
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of an {@link Iterator} of {@link Double}s.
	 *
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @see #cache(DoubleStream)
	 * @see #cache(Stream)
	 * @see #cache(DoubleIterable)
	 * @see #cache(Iterable)
	 * @see #once(Iterator)
	 * @since 1.1
	 */
	static DoubleSequence cache(Iterator<Double> iterator) {
		return cache(DoubleIterator.from(iterator));
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of a {@link DoubleStream}.
	 *
	 * @see #cache(Stream)
	 * @see #cache(DoubleIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @see #cache(Iterator)
	 * @see #once(DoubleStream)
	 * @since 1.1
	 */
	static DoubleSequence cache(DoubleStream stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of a {@link Stream} of {@link Double}s.
	 *
	 * @see #cache(DoubleStream)
	 * @see #cache(DoubleIterable)
	 * @see #cache(Iterable)
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @see #cache(Iterator)
	 * @see #once(Stream)
	 * @since 1.1
	 */
	static DoubleSequence cache(Stream<Double> stream) {
		return cache(stream.iterator());
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of an {@link DoubleIterable}.
	 *
	 * @see #cache(Iterable)
	 * @see #cache(DoubleStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @see #cache(Iterator)
	 * @see #from(DoubleIterable)
	 * @since 1.1
	 */
	static DoubleSequence cache(DoubleIterable iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * Create a {@code DoubleSequence} from a cached copy of an {@link Iterable} of {@code Double} values.
	 *
	 * @see #cache(DoubleIterable)
	 * @see #cache(DoubleStream)
	 * @see #cache(Stream)
	 * @see #cache(PrimitiveIterator.OfDouble)
	 * @see #cache(Iterator)
	 * @see #from(Iterable)
	 * @since 1.1
	 */
	static DoubleSequence cache(Iterable<Double> iterable) {
		return cache(iterable.iterator());
	}

	/**
	 * A {@code DoubleSequence} of all the {@link Double} values starting at the given value and ending at {@link
	 * Double#MAX_VALUE}.
	 *
	 * @see #range(double, double, double, double)
	 */
	static DoubleSequence steppingFrom(double start, double step) {
		return recurse(start, d -> d + step);
	}

	/**
	 * A {@code DoubleSequence} of all the {@link Double} values between the given start and end positions, inclusive,
	 * using
	 * the given step between iterations and the given accuracy to check whether the end value has occurred.
	 *
	 * @throws IllegalArgumentException if {@code step < 0}
	 * @see #steppingFrom(double, double)
	 */
	static DoubleSequence range(double start, double end, double step, double accuracy) {
		if (step < 0)
			throw new IllegalArgumentException("Require step to be >= 0");

		return end > start ?
		       recurse(start, d -> d + step).until(d -> d - accuracy >= end) :
		       recurse(start, d -> d - step).until(d -> d + accuracy <= end);
	}

	static DoubleSequence recurse(double seed, DoubleUnaryOperator op) {
		return () -> new InfiniteDoubleIterator() {
			private double previous;
			private boolean hasPrevious;

			@Override
			public double nextDouble() {
				previous = hasPrevious ? op.applyAsDouble(previous) : seed;
				hasPrevious = true;
				return previous;
			}
		};
	}

	/**
	 * @return a {@code DoubleSequence} that is generated from the given supplier and thus never terminates.
	 *
	 * @see #recurse(double, DoubleUnaryOperator)
	 * @see #endingAt(double, double)
	 * @see #until(double, double)
	 */
	static DoubleSequence generate(DoubleSupplier supplier) {
		return () -> (InfiniteDoubleIterator) supplier::getAsDouble;
	}

	/**
	 * @return a {@code DoubleSequence} where each {@link #iterator()} is generated by polling for a supplier and then
	 * using it to generate the sequence of {@code doubles}. The sequence never terminates.
	 *
	 * @see #recurse(double, DoubleUnaryOperator)
	 * @see #endingAt(double, double)
	 * @see #until(double, double)
	 */
	static DoubleSequence multiGenerate(Supplier<? extends DoubleSupplier> supplierSupplier) {
		return () -> {
			DoubleSupplier doubleSupplier = supplierSupplier.get();
			return (InfiniteDoubleIterator) doubleSupplier::getAsDouble;
		};
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between {@code 0}, inclusive and {@code 1}, exclusive, that
	 * never terminates. Each run of this {@code DoubleSequence}'s {@link #iterator()} will produce a new random
	 * sequence of doubles. This method is equivalent to {@code random (Random::new)}.
	 *
	 * @see #random(Supplier)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random() {
		return random(Random::new);
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between {@code 0}, inclusive and {@code 1}, exclusive, that
	 * never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one for
	 * each
	 * new {@link #iterator()}.
	 *
	 * @see #random()
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random(Supplier<? extends Random> randomSupplier) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			return random::nextDouble;
		});
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. Each run of this {@code DoubleSequence}'s {@link #iterator()} will produce a new random
	 * sequence of doubles. This method is equivalent to {@code random(Random::new, upper}.
	 *
	 * @see #random(Supplier, double)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random(double upper) {
		return random(0, upper);
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between {@code 0}, inclusive, and the upper bound, exclusive,
	 * that never terminates. The given supplier is used to produce the instance of {@link Random} that is used, one
	 * for
	 * each new {@link #iterator()}.
	 *
	 * @see #random(double)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random(Supplier<? extends Random> randomSupplier, double upper) {
		return random(randomSupplier, 0, upper);
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between the lower bound, inclusive, and upper bound,
	 * exclusive, that never terminates. Each run of this {@code DoubleSequence}'s {@link #iterator()} will produce a
	 * new random sequence of doubles. This method is equivalent to {@code random(Random::new, lower, upper}.
	 *
	 * @see #random(Supplier, double, double)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random(double lower, double upper) {
		return random(Random::new, lower, upper);
	}

	/**
	 * @return a {@code DoubleSequence} of random doubles between the lower bound, inclusive, and upper bound,
	 * exclusive, that never terminates. The given supplier is used to produce the instance of {@link Random} that is
	 * used, one for each new {@link #iterator()}.
	 *
	 * @see #random(double, double)
	 * @see Random#nextDouble()
	 * @since 1.2
	 */
	static DoubleSequence random(Supplier<? extends Random> randomSupplier, double lower, double upper) {
		return multiGenerate(() -> {
			Random random = randomSupplier.get();
			double bound = upper - lower;
			return () -> random.nextDouble() * bound + lower;
		});
	}

	/**
	 * Terminate this {@code DoubleSequence} before the given element compared to the given accuracy, with the previous
	 * element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #until(DoublePredicate)
	 * @see #endingAt(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence until(double terminal, double accuracy) {
		return () -> new ExclusiveTerminalDoubleIterator(iterator(), terminal, accuracy);
	}

	/**
	 * Terminate this {@code DoubleSequence} at the given element compared to the given accuracy, including it as the
	 * last element in this {@code DoubleSequence}.
	 *
	 * @see #endingAt(DoublePredicate)
	 * @see #until(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence endingAt(double terminal, double accuracy) {
		return () -> new InclusiveTerminalDoubleIterator(iterator(), terminal, accuracy);
	}

	/**
	 * Terminate this {@code DoubleSequence} before the element that satisfies the given predicate, with the
	 * previous element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #until(double, double)
	 * @see #endingAt(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence until(DoublePredicate terminal) {
		return () -> new ExclusiveTerminalDoubleIterator(iterator(), terminal);
	}

	/**
	 * Terminate this {@code DoubleSequence} at the element that satisfies the given predicate, including the
	 * element as the last element in this {@code DoubleSequence}.
	 *
	 * @see #endingAt(double, double)
	 * @see #until(double, double)
	 * @see #generate(DoubleSupplier)
	 * @see #recurse(double, DoubleUnaryOperator)
	 */
	default DoubleSequence endingAt(DoublePredicate terminal) {
		return () -> new InclusiveTerminalDoubleIterator(iterator(), terminal);
	}

	/**
	 * Begin this {@code DoubleSequence} just after the given element is encountered, not including the element in the
	 * {@code DoubleSequence}.
	 *
	 * @see #startingAfter(DoublePredicate)
	 * @see #startingFrom(double, double)
	 * @since 1.1
	 */
	default DoubleSequence startingAfter(double element, double accuracy) {
		return () -> new ExclusiveStartingDoubleIterator(iterator(), element, accuracy);
	}

	/**
	 * Begin this {@code DoubleSequence} when the given element is encountered, including the element as the first
	 * element
	 * in the {@code DoubleSequence}.
	 *
	 * @see #startingFrom(DoublePredicate)
	 * @see #startingAfter(double, double)
	 * @since 1.1
	 */
	default DoubleSequence startingFrom(double element, double accuracy) {
		return () -> new InclusiveStartingDoubleIterator(iterator(), element, accuracy);
	}

	/**
	 * Begin this {@code DoubleSequence} just after the given predicate is satisfied, not including the element that
	 * satisfies the predicate in the {@code DoubleSequence}.
	 *
	 * @see #startingAfter(double, double)
	 * @see #startingFrom(DoublePredicate)
	 * @since 1.1
	 */
	default DoubleSequence startingAfter(DoublePredicate predicate) {
		return () -> new ExclusiveStartingDoubleIterator(iterator(), predicate);
	}

	/**
	 * Begin this {@code DoubleSequence} when the given predicate is satisfied, including the element that satisfies
	 * the predicate as the first element in the {@code DoubleSequence}.
	 *
	 * @see #startingFrom(double, double)
	 * @see #startingAfter(DoublePredicate)
	 * @since 1.1
	 */
	default DoubleSequence startingFrom(DoublePredicate predicate) {
		return () -> new InclusiveStartingDoubleIterator(iterator(), predicate);
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to another set of {@code doubles} specified by the given
	 * {@code mapper} function.
	 */
	default DoubleSequence map(DoubleUnaryOperator mapper) {
		return () -> new DelegatingUnaryDoubleIterator(iterator()) {
			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextDouble());
			}
		};
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to another set of {@code doubles} specified by the given
	 * {@code mapper} function, while providing the current index to the mapper.
	 *
	 * @since 1.2
	 */
	default DoubleSequence mapIndexed(DoubleIntToDoubleFunction mapper) {
		return () -> new DelegatingUnaryDoubleIterator(iterator()) {
			private int index;

			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextDouble(), index++);
			}
		};
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to their boxed {@link Double} counterparts.
	 */
	default Sequence<Double> box() {
		return toSequence(Double::valueOf);
	}

	/**
	 * Map the {@code doubles} in this {@code DoubleSequence} to a {@link Sequence} of values.
	 */
	default <T> Sequence<T> toSequence(DoubleFunction<T> mapper) {
		return () -> Iterators.from(iterator(), mapper);
	}

	/**
	 * Skip a set number of {@code doubles} in this {@code DoubleSequence}.
	 */
	default DoubleSequence skip(int skip) {
		return () -> new SkippingDoubleIterator(iterator(), skip);
	}

	/**
	 * Skip a set number of {@code doubles} at the end of this {@code DoubleSequence}.
	 *
	 * @since 1.1
	 */
	default DoubleSequence skipTail(int skip) {
		if (skip == 0)
			return this;

		return () -> new TailSkippingDoubleIterator(iterator(), skip);
	}

	/**
	 * Limit the maximum number of {@code doubles} returned by this {@code DoubleSequence}.
	 */
	default DoubleSequence limit(int limit) {
		return () -> new LimitingDoubleIterator(iterator(), limit);
	}

	/**
	 * Append the given {@code doubles} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(double... doubles) {
		return append(DoubleIterable.of(doubles));
	}

	/**
	 * Append the {@code doubles} in the given {@link DoubleIterable} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(DoubleIterable that) {
		return new ChainingDoubleIterable(this, that)::iterator;
	}

	/**
	 * Append the {@link Double}s in the given {@link Iterable} to the end of this {@code DoubleSequence}.
	 */
	default DoubleSequence append(Iterable<Double> iterable) {
		return append(DoubleIterable.from(iterable));
	}

	/**
	 * Append the {@code doubles} in the given {@link PrimitiveIterator.OfDouble} to the end of this
	 * {@code DoubleSequence}.
	 * <p>
	 * The appended {@code doubles} will only be available on the first traversal of the resulting {@code
	 * DoubleSequence}.
	 */
	default DoubleSequence append(PrimitiveIterator.OfDouble iterator) {
		return append(DoubleIterable.once(iterator));
	}

	/**
	 * Append the {@link Double}s in the given {@link Iterator} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@link Double}s will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}.
	 */
	default DoubleSequence append(Iterator<Double> iterator) {
		return append(DoubleIterator.from(iterator));
	}

	/**
	 * Append the {@code double} values of the given {@link DoubleStream} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@link Double}s will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}.
	 */
	default DoubleSequence append(DoubleStream stream) {
		return append(stream.iterator());
	}

	/**
	 * Append the {@link Double}s in the given {@link Stream} to the end of this {@code DoubleSequence}.
	 * <p>
	 * The appended {@link Double}s will only be available on the first traversal of the resulting
	 * {@code DoubleSequence}.
	 */
	default DoubleSequence append(Stream<Double> stream) {
		return append(stream.iterator());
	}

	/**
	 * Filter the elements in this {@code DoubleSequence}, keeping only the elements that match the given
	 * {@link DoublePredicate}.
	 */
	default DoubleSequence filter(DoublePredicate predicate) {
		return () -> new FilteringDoubleIterator(iterator(), predicate);
	}

	/**
	 * Filter the elements in this {@code DoubleSequence}, keeping only the elements that match the given
	 * {@link DoubleIntPredicate}, which is passed each {@code double} together with its index in the sequence.
	 *
	 * @since 1.2
	 */
	default DoubleSequence filterIndexed(DoubleIntPredicate predicate) {
		return () -> new IndexedFilteringDoubleIterator(iterator(), predicate);
	}

	/**
	 * Filter this {@code DoubleSequence} to another sequence of doubles while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The predicate has access to the previous double and the current double in the iteration. If the current
	 * double is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default DoubleSequence filterBack(double firstPrevious, DoubleBiPredicate predicate) {
		return () -> new BackPeekingFilteringDoubleIterator(iterator(), firstPrevious, predicate);
	}

	/**
	 * Filter this {@code DoubleSequence} to another sequence of doubles while peeking at the next double in the
	 * sequence.
	 * <p>
	 * The predicate has access to the current double and the next double in the iteration. If the current double is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default DoubleSequence filterForward(double lastNext, DoubleBiPredicate predicate) {
		return () -> new ForwardPeekingFilteringDoubleIterator(iterator(), lastNext, predicate);
	}

	/**
	 * @return an {@code DoubleSequence} containing only the {@code doubles} found in the given target array, compared
	 * to the given precision.
	 *
	 * @since 2.0
	 */
	default DoubleSequence includingExactly(double... elements) {
		return filter(e -> Arrayz.containsExactly(elements, e));
	}

	/**
	 * @return an {@code DoubleSequence} containing only the {@code doubles} found in the given target array, compared
	 * with the given precision.
	 *
	 * @since 2.0
	 */
	default DoubleSequence including(double[] elements, double precision) {
		return filter(e -> Arrayz.contains(elements, e, precision));
	}

	/**
	 * @return an {@code DoubleSequence} containing only the {@code doubles} not found in the given target array,
	 * compared to the given precision.
	 *
	 * @since 2.0
	 */
	default DoubleSequence excludingExactly(double... elements) {
		return filter(e -> !Arrayz.containsExactly(elements, e));
	}

	/**
	 * @return an {@code DoubleSequence} containing only the {@code doubles} not found in the given target array,
	 * compared with the given precision.
	 *
	 * @since 2.0
	 */
	default DoubleSequence excluding(double[] elements, double precision) {
		return filter(e -> !Arrayz.contains(elements, e, precision));
	}

	/**
	 * Collect the elements in this {@code DoubleSequence} into an {@link DoubleList}.
	 */
	default DoubleList toList() {
		return toList(DoubleList::create);
	}

	/**
	 * Collect the elements in this {@code DoubleSequence} into an {@link DoubleList} of the type determined by the
	 * given constructor.
	 */
	default DoubleList toList(Supplier<? extends DoubleList> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code DoubleSequence} into an {@link DoubleSet}.
	 */
	default DoubleSet toSet() {
		return toSet(RawDoubleSet::new);
	}

	/**
	 * Collect the elements in this {@code DoubleSequence} into an {@link DoubleSet} of the type determined by the
	 * given constructor.
	 */
	default <S extends DoubleSet> S toSet(Supplier<? extends S> constructor) {
		return toCollection(constructor);
	}

	/**
	 * Collect the elements in this {@code DoubleSequence} into an {@link DoubleSortedSet}.
	 */
	default DoubleSortedSet toSortedSet() {
		return toSet(SortedListDoubleSet::new);
	}

	/**
	 * Collect this {@code DoubleSequence} into an {@link DoubleCollection} of the type determined by the given
	 * constructor.
	 */
	default <U extends DoubleCollection> U toCollection(Supplier<? extends U> constructor) {
		return collectInto(constructor.get());
	}

	/**
	 * Collect this {@code DoubleSequence} into an arbitrary container using the given constructor and adder.
	 */
	default <C> C collect(Supplier<? extends C> constructor, ObjDoubleConsumer<? super C> adder) {
		return collectInto(constructor.get(), adder);
	}

	/**
	 * Collect this {@code DoubleSequence} into the given {@link DoubleCollection}.
	 */
	default <U extends DoubleCollection> U collectInto(U collection) {
		collection.addAllDoubles(this);
		return collection;
	}

	/**
	 * Collect this {@code DoubleSequence} into the given container using the given adder.
	 */
	default <C> C collectInto(C result, ObjDoubleConsumer<? super C> adder) {
		forEachDouble(x -> adder.accept(result, x));
		return result;
	}

	/**
	 * Join this {@code DoubleSequence} into a string separated by the given delimiter.
	 */
	default String join(String delimiter) {
		return join("", delimiter, "");
	}

	/**
	 * Join this {@code DoubleSequence} into a string separated by the given delimiter, with the given prefix and
	 * suffix.
	 */
	default String join(String prefix, String delimiter, String suffix) {
		StringBuilder result = new StringBuilder(prefix);

		boolean started = false;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); started = true) {
			double each = iterator.nextDouble();
			if (started)
				result.append(delimiter);
			result.append(each);
		}

		return result.append(suffix).toString();
	}

	/**
	 * Reduce this {@code DoubleSequence} into a single {@code double} by iteratively applying the given binary
	 * operator to the current result and each {@code double} in the sequence.
	 */
	default OptionalDouble reduce(DoubleBinaryOperator operator) {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		double result = iterator.reduce(iterator.nextDouble(), operator);
		return OptionalDouble.of(result);
	}

	/**
	 * Reduce this {@code DoubleSequence} into a single {@code double} by iteratively applying the given binary
	 * operator to the current result and each {@code double} in the sequence, starting with the given identity as the
	 * initial result.
	 */
	default double reduce(double identity, DoubleBinaryOperator operator) {
		return iterator().reduce(identity, operator);
	}

	/**
	 * @return the first double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are no
	 * doubles in the {@code DoubleSequence}.
	 */
	default OptionalDouble first() {
		return at(0);
	}

	/**
	 * @return the last double of this {@code DoubleSequence} or an empty {@link OptionalDouble} if there are no
	 * doubles
	 * in the {@code DoubleSequence}.
	 */
	default OptionalDouble last() {
		DoubleIterator iterator = iterator();
		if (!iterator.hasNext())
			return OptionalDouble.empty();

		double last;
		do
			last = iterator.nextDouble(); while (iterator.hasNext());

		return OptionalDouble.of(last);
	}

	/**
	 * @return the {@code double} at the given index, or an empty {@link OptionalDouble} if the {@code DoubleSequence}
	 * is smaller than the index.
	 *
	 * @since 1.2
	 */
	default OptionalDouble at(int index) {
		DoubleIterator iterator = iterator();
		iterator.skip(index);

		if (!iterator.hasNext())
			return OptionalDouble.empty();

		return OptionalDouble.of(iterator.nextDouble());
	}

	/**
	 * @return the first double of those in this {@code DoubleSequence} matching the given predicate, or an empty
	 * {@link
	 * OptionalDouble} if there are no matching doubles in the {@code DoubleSequence}.
	 *
	 * @see #filter(DoublePredicate)
	 * @see #at(int, DoublePredicate)
	 * @since 1.2
	 */
	default OptionalDouble first(DoublePredicate predicate) {
		return at(0, predicate);
	}

	/**
	 * @return the last double of those in this {@code DoubleSequence} matching the given predicate, or an empty {@link
	 * OptionalDouble} if there are no matching doubles in the {@code DoubleSequence}.
	 *
	 * @see #filter(DoublePredicate)
	 * @see #at(int, DoublePredicate)
	 * @since 1.2
	 */
	default OptionalDouble last(DoublePredicate predicate) {
		return filter(predicate).last();
	}

	/**
	 * @return the {@code double} at the given index out of doubles matching the given predicate, or an empty {@link
	 * OptionalDouble} if the matching {@code DoubleSequence} is smaller than the index.
	 *
	 * @see #filter(DoublePredicate)
	 * @since 1.2
	 */
	default OptionalDouble at(int index, DoublePredicate predicate) {
		return filter(predicate).at(index);
	}

	/**
	 * Skip x number of steps in between each invocation of the iterator of this {@code DoubleSequence}.
	 */
	default DoubleSequence step(int step) {
		return () -> new SteppingDoubleIterator(iterator(), step);
	}

	/**
	 * @return a {@code DoubleSequence} where each item occurs only once, the first time it is encountered, compared to
	 * the given precision.
	 */
	default DoubleSequence distinct(double precision) {
		return () -> new DistinctDoubleIterator(iterator(), precision);
	}

	/**
	 * @return a {@code DoubleSequence} where each item occurs only once, the first time it is encountered.
	 */
	default DoubleSequence distinctExactly() {
		return () -> new DistinctExactlyDoubleIterator(iterator());
	}

	/**
	 * @return the smallest double in this {@code DoubleSequence}.
	 */
	default OptionalDouble min() {
		return reduce(Math::min);
	}

	/**
	 * @return the greatest double in this {@code DoubleSequence}.
	 */
	default OptionalDouble max() {
		return reduce(Math::max);
	}

	/**
	 * @return the number of doubles in this {@code DoubleSequence}.
	 *
	 * @since 1.2
	 */
	default int size() {
		return iterator().size();
	}

	/**
	 * @return an unsized {@link Spliterator.OfDouble} for this {@code DoubleSequence}.
	 *
	 * @since 2.2
	 */
	@Override
	default Spliterator.OfDouble spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), 0);
	}

	/**
	 * @return true if all doubles in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean all(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			if (!predicate.test(iterator.nextDouble()))
				return false;
		return true;
	}

	/**
	 * @return true if no doubles in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean none(DoublePredicate predicate) {
		return !any(predicate);
	}

	/**
	 * @return true if any double in this {@code DoubleSequence} satisfy the given predicate, false otherwise.
	 */
	default boolean any(DoublePredicate predicate) {
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			if (predicate.test(iterator.nextDouble()))
				return true;
		return false;
	}

	/**
	 * Allow the given {@link DoubleConsumer} to see each element in this {@code DoubleSequence} as it is traversed.
	 */
	default DoubleSequence peek(DoubleConsumer action) {
		return () -> new DelegatingUnaryDoubleIterator(iterator()) {
			@Override
			public double nextDouble() {
				double next = iterator.nextDouble();
				action.accept(next);
				return next;
			}
		};
	}

	/**
	 * Allow the given {@link DoubleIntConsumer} to see each element together with its index in this
	 * {@code DoubleSequence} as it is traversed.
	 *
	 * @since 1.2.2
	 */
	default DoubleSequence peekIndexed(DoubleIntConsumer action) {
		return () -> new DelegatingUnaryDoubleIterator(iterator()) {
			private int index;

			@Override
			public double nextDouble() {
				double next = iterator.nextDouble();
				action.accept(next, index++);
				return next;
			}
		};
	}

	/**
	 * @return this {@code DoubleSequence} sorted according to the natural order of the double values.
	 *
	 * @see #reverse()
	 */
	default DoubleSequence sorted() {
		return () -> {
			double[] array = toDoubleArray();
			Arrays.sort(array);
			return DoubleIterator.of(array);
		};
	}

	/**
	 * Prefix the doubles in this {@code DoubleSequence} with the given doubles.
	 */
	default DoubleSequence prefix(double... xs) {
		return () -> new ChainingDoubleIterator(DoubleIterable.of(xs), this);
	}

	/**
	 * Suffix the doubles in this {@code DoubleSequence} with the given doubles.
	 */
	default DoubleSequence suffix(double... xs) {
		return () -> new ChainingDoubleIterator(this, DoubleIterable.of(xs));
	}

	/**
	 * Interleave the elements in this {@code DoubleSequence} with those of the given {@code DoubleIterable}, stopping
	 * when either sequence finishes.
	 */
	default DoubleSequence interleave(DoubleIterable that) {
		return () -> new InterleavingDoubleIterator(this, that);
	}

	/**
	 * @return a {@code DoubleSequence} which iterates over this {@code DoubleSequence} in reverse order.
	 *
	 * @see #sorted()
	 */
	default DoubleSequence reverse() {
		return () -> {
			double[] array = toDoubleArray();
			Arrayz.reverse(array);
			return DoubleIterator.of(array);
		};
	}

	/**
	 * Map this {@code DoubleSequence} to another sequence of doubles while peeking at the previous value in the
	 * sequence.
	 * <p>
	 * The mapper has access to the previous double and the current double in the iteration. If the current double is
	 * the first value in the sequence, and there is no previous value, the provided replacement value is used as
	 * the first previous value.
	 */
	default DoubleSequence mapBack(double firstPrevious, DoubleBinaryOperator mapper) {
		return () -> new BackPeekingMappingDoubleIterator(iterator(), firstPrevious, mapper);
	}

	/**
	 * Map this {@code DoubleSequence} to another sequence of doubles while peeking at the next value in the
	 * sequence.
	 * <p>
	 * The mapper has access to the current double and the next double in the iteration. If the current double is
	 * the last value in the sequence, and there is no next value, the provided replacement value is used as
	 * the last next value.
	 */
	default DoubleSequence mapForward(double lastNext, DoubleBinaryOperator mapper) {
		return () -> new ForwardPeekingMappingDoubleIterator(iterator(), lastNext, mapper);
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints corresponding to the downcast integer value of each
	 * double.
	 */
	default IntSequence toInts() {
		return () -> IntIterator.from(iterator());
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs corresponding to the downcast long value of each
	 * double.
	 */
	default LongSequence toLongs() {
		return () -> LongIterator.from(iterator());
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints corresponding to the downcast rounded int value of each
	 * double.
	 */
	default IntSequence toRoundedInts() {
		return toInts(d -> (int) round(d));
	}

	/**
	 * Convert this sequence of doubles to a sequence of ints using the given converter function.
	 */
	default IntSequence toInts(DoubleToIntFunction mapper) {
		return () -> IntIterator.from(iterator(), mapper);
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs corresponding to the rounded long value of each
	 * double.
	 */
	default LongSequence toRoundedLongs() {
		return toLongs(Math::round);
	}

	/**
	 * Convert this sequence of doubles to a sequence of longs using the given converter function.
	 */
	default LongSequence toLongs(DoubleToLongFunction mapper) {
		return () -> LongIterator.from(iterator(), mapper);
	}

	/**
	 * Repeat this sequence of characters doubles, looping back to the beginning when the iterator runs out of doubles.
	 * <p>
	 * The resulting sequence will never terminate if this sequence is non-empty.
	 */
	default DoubleSequence repeat() {
		return () -> new RepeatingDoubleIterator(this, -1);
	}

	/**
	 * Repeat this sequence of doubles x times, looping back to the beginning when the iterator runs out of doubles.
	 */
	default DoubleSequence repeat(int times) {
		return () -> new RepeatingDoubleIterator(this, times);
	}

	/**
	 * Window the elements of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of elements, each
	 * with
	 * the size of the given window. The first item in each list is the second item in the previous list. The final
	 * {@code DoubleSequence} may be shorter than the window. This is equivalent to {@code window(window, 1)}.
	 */
	default Sequence<DoubleSequence> window(int window) {
		return window(window, 1);
	}

	/**
	 * Window the elements of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of elements, each
	 * with
	 * the size of the given window, stepping {@code step} elements between each window. If the given step is less than
	 * the window size, the windows will overlap each other.
	 */
	default Sequence<DoubleSequence> window(int window, int step) {
		return () -> new WindowingDoubleIterator(iterator(), window, step);
	}

	/**
	 * Batch the elements of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of distinct
	 * elements,
	 * each with the given batch size. This is equivalent to {@code window(size, size)}.
	 */
	default Sequence<DoubleSequence> batch(int size) {
		return window(size, size);
	}

	/**
	 * Batch the elements of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of distinct
	 * elements,
	 * where the given predicate determines where to split the lists of partitioned elements. The predicate is given
	 * the current and next item in the iteration, and if it returns true a partition is created between the elements.
	 */
	default Sequence<DoubleSequence> batch(DoubleBiPredicate predicate) {
		return () -> new PredicatePartitioningDoubleIterator(iterator(), predicate);
	}

	/**
	 * Split the {@code doubles} of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of distinct
	 * elements, around the given {@code double}. The elements around which the sequence is split are not included in
	 * the result.
	 *
	 * @since 1.1
	 */
	default Sequence<DoubleSequence> split(double element) {
		return () -> new SplittingDoubleIterator(iterator(), element);
	}

	/**
	 * Split the {@code doubles} of this {@code DoubleSequence} into a sequence of {@code DoubleSequence}s of distinct
	 * elements, where the given predicate determines which {@code doubles} to split the partitioned elements around.
	 * The
	 * {@code doubles} matching the predicate are not included in the result.
	 *
	 * @since 1.1
	 */
	default Sequence<DoubleSequence> split(DoublePredicate predicate) {
		return () -> new SplittingDoubleIterator(iterator(), predicate);
	}

	/**
	 * @return true if this {@code DoubleSequence} is empty, false otherwise.
	 *
	 * @since 1.1
	 */
	default boolean isEmpty() {
		return iterator().isEmpty();
	}

	/**
	 * Perform the given action for each {@code double} in this {@code DoubleSequence}, with the index of each element
	 * passed as the second parameter in the action.
	 *
	 * @since 1.2
	 */
	default void forEachDoubleIndexed(DoubleIntConsumer action) {
		int index = 0;
		for (DoubleIterator iterator = iterator(); iterator.hasNext(); )
			action.accept(iterator.nextDouble(), index++);
	}
}
