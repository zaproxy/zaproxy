/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.parosproxy.paros.model.Session;

/** Unit test for {@link Context}. */
@RunWith(MockitoJUnitRunner.class)
public class ContextUnitTest {

    @Mock Session session;

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Context(session, 1);
    }

    @Test
    public void shouldNullUrlNeverBeIncluded() {
        assertThat(context.isIncluded((String) null), is(false));
    }

    @Test
    public void shouldUseIndexAsDefaultName() {
        // Given
        int index = 1010;
        // When
        Context context = new Context(session, index);
        // Then
        assertThat(context.getName(), is(equalTo(String.valueOf(index))));
    }

    @Test(expected = IllegalContextNameException.class)
    public void shouldNotAllowToSetNullName() {
        // Given
        String name = null;
        // When
        context.setName(name);
        // Then = IllegalContextNameException.class
    }

    @Test(expected = IllegalContextNameException.class)
    public void shouldNotAllowToSetAnEmptyName() {
        // Given
        String name = "";
        // When
        context.setName(name);
        // Then = IllegalContextNameException.class
    }

    @Test
    public void shouldSetNonEmptyName() {
        // Given
        String name = "Default Context";
        // When
        context.setName(name);
        // Then
        assertThat(context.getName(), is(equalTo(name)));
    }

    // TODO Implement more tests

}
