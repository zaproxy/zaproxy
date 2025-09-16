/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.alert.ExampleAlertProvider;

/**
 * Unit tests for {@link AbstractPlugin} getExampleAlerts functionality.
 */
public class AbstractPluginTest {

    /**
     * Test implementation of AbstractPlugin for testing purposes.
     */
    private static class TestPlugin extends AbstractPlugin {
        private final int pluginId;
        private final boolean shouldProvideExamples;

        public TestPlugin(int pluginId, boolean shouldProvideExamples) {
            this.pluginId = pluginId;
            this.shouldProvideExamples = shouldProvideExamples;
        }

        @Override
        public int getId() {
            return pluginId;
        }

        @Override
        public String getName() {
            return "Test Plugin";
        }

        @Override
        public String getDescription() {
            return "Test plugin for getExampleAlerts";
        }

        @Override
        public int getCategory() {
            return Category.MISC;
        }

        @Override
        public String getSolution() {
            return "Test solution";
        }

        @Override
        public String getReference() {
            return "Test reference";
        }

        @Override
        public void scan() {
            // Test implementation
        }

        @Override
        public List<Alert> getExampleAlerts() {
            if (shouldProvideExamples) {
                Alert exampleAlert = Alert.builder()
                        .setPluginId(getId())
                        .setName("Example Alert")
                        .setRisk(Alert.RISK_MEDIUM)
                        .setConfidence(Alert.CONFIDENCE_MEDIUM)
                        .setDescription("Example description")
                        .setUri("https://example.com/test")
                        .build();
                return Arrays.asList(exampleAlert);
            }
            return super.getExampleAlerts(); // Returns null by default
        }
    }

    @Test
    public void shouldImplementExampleAlertProvider() {
        // Given
        TestPlugin plugin = new TestPlugin(12345, false);

        // Then
        assertTrue(plugin instanceof ExampleAlertProvider);
    }

    @Test
    public void shouldReturnNullByDefault() {
        // Given
        TestPlugin plugin = new TestPlugin(12345, false);

        // When
        List<Alert> examples = plugin.getExampleAlerts();

        // Then
        assertNull(examples);
    }

    @Test
    public void shouldAllowOverrideOfGetExampleAlerts() {
        // Given
        TestPlugin plugin = new TestPlugin(12345, true);

        // When
        List<Alert> examples = plugin.getExampleAlerts();

        // Then
        assertNotNull(examples);
        assertTrue(examples.size() > 0);
        Alert alert = examples.get(0);
        assertNotNull(alert);
        assertEquals(12345, alert.getPluginId());
        assertEquals("Example Alert", alert.getName());
        assertEquals(Alert.RISK_MEDIUM, alert.getRisk());
    }

    @Test
    public void shouldNotPersistExampleAlerts() {
        // Given
        TestPlugin plugin = new TestPlugin(12345, true);

        // When
        List<Alert> examples = plugin.getExampleAlerts();

        // Then
        assertNotNull(examples);
        for (Alert alert : examples) {
            // Example alerts should not have database IDs
            assertEquals(-1, alert.getAlertId());
            // They should not be tied to actual HTTP messages
            assertNull(alert.getMessage());
        }
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private void assertEquals(String expected, String actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null || !expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }
}