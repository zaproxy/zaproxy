/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Writer;
import org.junit.jupiter.api.Test;

class MultipleWritersUnitTest {

    @Test
    void shouldWriteToLaterWritersEvenIfEarlierWriterFails() throws Exception {
        MultipleWriters writers = new MultipleWriters();
        Writer failingWriter = mock(Writer.class);
        Writer succeedingWriter = mock(Writer.class);

        doThrow(new IOException("write failed")).when(failingWriter).append("abc");

        writers.addWriter(failingWriter);
        writers.addWriter(succeedingWriter);

        assertThrows(IOException.class, () -> writers.write("abc".toCharArray(), 0, 3));

        verify(failingWriter).append("abc");
        verify(succeedingWriter).append("abc");
    }

    @Test
    void shouldFlushLaterWritersEvenIfEarlierWriterFails() throws Exception {
        MultipleWriters writers = new MultipleWriters();
        Writer failingWriter = mock(Writer.class);
        Writer succeedingWriter = mock(Writer.class);

        doThrow(new IOException("flush failed")).when(failingWriter).flush();

        writers.addWriter(failingWriter);
        writers.addWriter(succeedingWriter);

        assertThrows(IOException.class, writers::flush);

        verify(failingWriter).flush();
        verify(succeedingWriter).flush();
    }

    @Test
    void shouldCloseLaterWritersEvenIfEarlierWriterFails() throws Exception {
        MultipleWriters writers = new MultipleWriters();
        Writer failingWriter = mock(Writer.class);
        Writer succeedingWriter = mock(Writer.class);

        doThrow(new IOException("close failed")).when(failingWriter).close();

        writers.addWriter(failingWriter);
        writers.addWriter(succeedingWriter);

        assertThrows(IOException.class, writers::close);

        verify(failingWriter).close();
        verify(succeedingWriter).close();
    }
}
