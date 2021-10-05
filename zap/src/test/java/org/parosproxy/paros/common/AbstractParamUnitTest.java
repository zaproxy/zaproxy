/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.parosproxy.paros.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link AbstractParam}. */
class AbstractParamUnitTest {

    private static final String VALUE_KEY = "config.value";
    private static final String VALUE = "Value X";

    private static final String VALUES_KEY = "config.values";
    private static final String VALUES_VALUE_KEY = "value";
    private static final List<String> VALUES;

    static {
        VALUES = new ArrayList<>(5);
        VALUES.add("Value 1");
        VALUES.add("Value 2");
        VALUES.add("Value 3");
        VALUES.add("Value 4");
        VALUES.add("Value 5");
    }

    @Test
    void shouldNotHaveConfigByDefault() {
        // Given / When
        AbstractParam param = createTestAbstractParam();
        // Then
        assertThat(param.getConfig(), is(equalTo(null)));
    }

    @Test
    void shouldParseLoadedFileConfiguration() {
        // Given
        TestAbstractParam param = createTestAbstractParam();
        FileConfiguration config = createTestConfig();
        // When
        param.load(config);
        // Then
        assertThat(param.getValue(), is(equalTo(VALUE)));
        assertThat(param.getValues(), is(equalTo(VALUES)));
    }

    @Test
    void shouldBeCloneableByDefault() {
        // Given
        TestAbstractParam param = createTestAbstractParam();
        // When
        TestAbstractParam clone = param.clone();
        // Then
        assertThat(clone, is(not(equalTo(null))));
        assertThat(clone.getValue(), is(equalTo(null)));
        assertThat(clone.getValues(), is(equalTo(null)));
    }

    @Test
    void shouldHaveLoadedConfigsAfterCloning() {
        // Given
        TestAbstractParam param = createTestAbstractParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        // When
        TestAbstractParam clone = param.clone();
        // Then
        assertThat(clone, is(not(equalTo(null))));
        assertThat(clone.getValue(), is(equalTo(VALUE)));
        assertThat(clone.getValues(), is(equalTo(VALUES)));
    }

    private static TestAbstractParam createTestAbstractParam() {
        return new TestAbstractParam();
    }

    private static class TestAbstractParam extends AbstractParam {

        private String value;
        private List<String> values;

        @Override
        protected void parse() {
            if (getConfig() == null) {
                return;
            }

            value = getConfig().getString(VALUE_KEY);
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(VALUES_KEY);
            values = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                values.add(sub.getString(VALUES_VALUE_KEY));
            }
        }

        String getValue() {
            return value;
        }

        List<String> getValues() {
            return values;
        }

        @Override
        public TestAbstractParam clone() {
            return (TestAbstractParam) super.clone();
        }
    }

    private static FileConfiguration createTestConfig() {
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        config.setProperty(VALUE_KEY, VALUE);
        for (int i = 0, size = VALUES.size(); i < size; ++i) {
            config.setProperty(VALUES_KEY + "(" + i + ")." + VALUES_VALUE_KEY, VALUES.get(i));
        }
        return config;
    }
}
