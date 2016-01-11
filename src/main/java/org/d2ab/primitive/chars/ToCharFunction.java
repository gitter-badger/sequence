/*
 * Copyright 2015 Daniel Skogquist Åborg
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

package org.d2ab.primitive.chars;

import java.util.function.Function;

/**
 * Represents a function that produces an char-valued result.  This is the
 * {@code char}-producing primitive specialization for {@link Function}.
 * <p>
 * <p>This is a functional interface whose functional method is {@link #applyAsChar(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface ToCharFunction<T> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 *
	 * @return the function result
	 */
	char applyAsChar(T value);
}