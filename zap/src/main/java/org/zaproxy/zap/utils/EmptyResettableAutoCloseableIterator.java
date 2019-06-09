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
 * An immutable {@code ResettableAutoCloseableIterator} without behaviour.
 *
 * <p>Useful as placeholder when a non-{@code null} {@code ResettableAutoCloseableIterator} is
 * required but does not to have to do anything.
 *
 * @param <E> the type of elements returned by this iterator
 * @see #emptyIterator()
 * @see ResettableAutoCloseableIterator
 * @since 2.4.0
 */
public final class EmptyResettableAutoCloseableIterator<E>
        implements ResettableAutoCloseableIterator<E> {

    private static final EmptyResettableAutoCloseableIterator<?> EMPTY_ITERATOR =
            new EmptyResettableAutoCloseableIterator<>();

    private EmptyResettableAutoCloseableIterator() {}

    public static <T> EmptyResettableAutoCloseableIterator<T> emptyIterator() {
        @SuppressWarnings("unchecked")
        EmptyResettableAutoCloseableIterator<T> iterator =
                (EmptyResettableAutoCloseableIterator<T>) EMPTY_ITERATOR;
        return iterator;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        return null;
    }

    @Override
    public void remove() {}

    @Override
    public void reset() {}

    @Override
    public void close() {}
}
