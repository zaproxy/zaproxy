/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.utils;

/**
 * An immutable {@code ResettableIterator} without behaviour.
 *
 * <p>Useful as placeholder when a non-{@code null} {@code ResettableIterator} is required but does
 * not to have to do anything.
 *
 * @param <E> the type of elements returned by this iterator
 * @see #emptyIterator()
 * @see ResettableIterator
 * @since 2.4.0
 */
public final class EmptyResettableIterator<E> implements ResettableIterator<E> {

    private static final EmptyResettableIterator<?> EMPTY_ITERATOR =
            new EmptyResettableIterator<>();

    private EmptyResettableIterator() {}

    public static <T> EmptyResettableIterator<T> emptyIterator() {
        @SuppressWarnings("unchecked")
        EmptyResettableIterator<T> iterator = (EmptyResettableIterator<T>) EMPTY_ITERATOR;
        return iterator;
    }

    /** Returns {@code false}, always. */
    @Override
    public boolean hasNext() {
        return false;
    }

    /** Returns {@code null}, always. */
    @Override
    public E next() {
        return null;
    }

    /** Does nothing. */
    @Override
    public void remove() {}

    /** Does nothing. */
    @Override
    public void reset() {}
}
