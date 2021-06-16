/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.parosproxy.paros.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.TableContext;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.ascan.VariantFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StandardParameterParser;

class SessionUnitTest {

    private Session session;
    private VariantFactory factory;

    @BeforeEach
    void setUp() throws Exception {
        factory = new VariantFactory();
        Model model = mock(Model.class);
        given(model.getVariantFactory()).willReturn(factory);
        Control.initSingletonForTesting(model);

        session = new Session(model);
        given(model.getSession()).willReturn(session);
    }

    /** Tests related to {@link Context}. */
    static class ContextRelatedUnitTest extends WithConfigsTest {

        private Session session;
        private TableContext tableContext;

        @BeforeEach
        void setup() {
            Database database = mock(Database.class);
            given(model.getDb()).willReturn(database);
            tableContext = mock(TableContext.class);
            given(database.getTableContext()).willReturn(tableContext);

            session = new Session(model);
        }

        @Test
        void shouldImportContextWithJustName() throws Exception {
            // Given
            String name = "Context Name";
            File contextFile = contextFile(String.format("<name>%s</name>", name));
            // When
            Context context = session.importContext(contextFile);
            // Then
            assertThat(context.getId(), is(equalTo(1)));
            assertThat(context.getName(), is(equalTo(name)));
            assertThat(context.getDescription(), is(nullValue()));
            assertThat(context.isInScope(), is(equalTo(false)));
            assertThat(context.getIncludeInContextRegexs(), is(empty()));
            assertThat(context.getExcludeFromContextRegexs(), is(empty()));
            assertThat(context.getTechSet(), is(notNullValue()));
            assertThat(context.getTechSet().getIncludeTech(), is(empty()));
            assertThat(context.getTechSet().getExcludeTech(), is(empty()));
            assertThat(context.getUrlParamParser(), is(instanceOf(StandardParameterParser.class)));
            assertThat(context.getPostParamParser(), is(instanceOf(StandardParameterParser.class)));
            assertThat(context.getDataDrivenNodes(), is(empty()));
        }

        private static File contextFile(String content) throws Exception {
            Path file = Files.createTempFile(tempDir, "context", null);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<configuration>\n"
                                + "    <context>");
                writer.write(content);
                writer.write("\n" + "    </context>\n" + "</configuration>");
            }
            return file.toFile();
        }
    }

    private static final class LeadNameVariant implements Variant {

        LeadNameVariant() {}

        @Override
        public String getLeafName(String nodeName, HttpMessage msg) {
            return "Test";
        }

        @Override
        public void setMessage(HttpMessage msg) {}

        @Override
        public List<NameValuePair> getParamList() {
            return null;
        }

        @Override
        public String setParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }

        @Override
        public String setEscapedParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }
    }
}
