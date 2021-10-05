/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ScriptVars}. */
class ScriptVarsUnitTest {

    @BeforeEach
    void setUp() {
        ScriptVars.clear();
    }

    @Test
    void shouldNotAllowToModifyReturnedGlobalVariables() {
        // Given
        Map<String, String> vars = ScriptVars.getGlobalVars();
        // When / Then
        assertThrows(
                UnsupportedOperationException.class, () -> vars.put(createKey(), createValue()));
    }

    @Test
    void shouldSetGlobalVariable() {
        // Given
        String key = createKey();
        String value = createValue();
        // When
        ScriptVars.setGlobalVar(key, value);
        // Then
        assertThat(ScriptVars.getGlobalVars(), hasEntry(key, value));
        assertThat(ScriptVars.getGlobalVar(key), is(equalTo(value)));
    }

    @Test
    void shouldClearGlobalVariableWithNullValue() {
        // Given
        String key = createKey();
        String value = createValue();
        ScriptVars.setGlobalVar(key, value);
        // When
        ScriptVars.setGlobalVar(key, null);
        // Then
        assertThat(ScriptVars.getGlobalVars(), not(hasEntry(key, value)));
        assertThat(ScriptVars.getGlobalVar(key), is(nullValue()));
    }

    @Test
    void shouldNotSetGlobalVariableWithNullKey() {
        // Given
        String key = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> ScriptVars.setGlobalVar(key, createValue()));
    }

    @Test
    void shouldNotSetGlobalVariableIfMoreThanAllowed() {
        // Given
        for (int i = 0; i <= ScriptVars.MAX_GLOBAL_VARS; i++) {
            ScriptVars.setGlobalVar(createKey(), createValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setGlobalVar(createKey(), createValue()));
    }

    @Test
    void shouldHaveNoScriptVariablesByDefault() {
        // Given
        String scriptName = "ScriptName";
        // When
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // Then
        assertThat(vars, is(notNullValue()));
        assertThat(vars.size(), is(equalTo(0)));
    }

    @Test
    void shouldReturnNullForNoScriptVariableSet() {
        // Given
        String scriptName = "ScriptName";
        String key = createKey();
        // When
        String value = ScriptVars.getScriptVar(scriptName, key);
        // Then
        assertThat(value, is(nullValue()));
    }

    @Test
    void shouldNotAllowToModifyReturnedScriptVariables() {
        // Given
        String scriptName = "ScriptName";
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class, () -> vars.put(createKey(), createValue()));
    }

    @Test
    void shouldSetScriptVariableUsingScriptContext() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        // When
        ScriptVars.setScriptVar(scriptContext, key, value);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptVar(scriptContext, key), is(equalTo(value)));
    }

    @Test
    void shouldClearScriptVariableWithNullValueUsingScriptContext() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        ScriptVars.setScriptVar(scriptContext, key, value);
        // When
        ScriptVars.setScriptVar(scriptContext, key, null);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptVar(scriptContext, key), is(nullValue()));
    }

    @Test
    void shouldNotSetScriptVariableUsingNullScriptContext() {
        // Given
        ScriptContext scriptContext = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptContext, createKey(), createValue()));
    }

    @Test
    void shouldNotSetScriptVariableUsingNullScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(null);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptContext, createKey(), createValue()));
    }

    @Test
    void shouldNotSetScriptVariableUsingNonStringScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(10);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptContext, createKey(), createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithNullKeyUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptContext, null, createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithInvalidKeyLengthUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptVar(
                                scriptContext, createKeyWithInvalidLength(), createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithInvalidValueLengthUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptVar(
                                scriptContext, createKey(), createValueWithInvalidLength()));
    }

    @Test
    void shouldNotSetScriptVariableIfMoreThanAllowedUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptVar(scriptContext, createKey(), createValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptContext, createKey(), createValue()));
    }

    @Test
    void shouldNotReturnScriptVariablesFromOtherScriptsUsingScriptContext() {
        // Given
        ScriptContext scriptContext1 = createScriptContextWithName("ScriptName1");
        ScriptContext scriptContext2 = createScriptContextWithName("ScriptName2");
        String key = createKey();
        // When
        ScriptVars.setScriptVar(scriptContext1, key, createValue());
        // Then
        assertThat(ScriptVars.getScriptVar(scriptContext2, key), is(nullValue()));
    }

    @Test
    void shouldSetScriptVariableUsingScriptName() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        // When
        ScriptVars.setScriptVar(scriptName, key, value);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptVar(scriptName, key), is(equalTo(value)));
    }

    @Test
    void shouldClearScriptVariableWithNullValueUsingScriptName() {
        // Given
        String key = createKey();
        String value = createValue();
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, key, value);
        // When
        ScriptVars.setScriptVar(scriptName, key, null);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptVar(scriptName, key), is(nullValue()));
    }

    @Test
    void shouldNotSetScriptVariableUsingNullScriptName() {
        // Given
        String scriptName = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptName, createKey(), createValue()));
    }

    @Test
    void shouldNotAllowToModifyReturnedScriptVariablesSet() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        Map<String, String> vars = ScriptVars.getScriptVars(scriptName);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class, () -> vars.put(createKey(), createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithNullKeyUsingScriptName() {
        // Given
        String key = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar("ScriptName", key, createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithInvalidKeyLengthUsingScriptName() {
        // Given
        String key = createKeyWithInvalidLength();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar("ScriptName", key, createValue()));
    }

    @Test
    void shouldNotSetScriptVariableWithInvalidValueLengthUsingScriptName() {
        // Given
        String value = createValueWithInvalidLength();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar("ScriptName", createKey(), value));
    }

    @Test
    void shouldNotSetScriptVariableIfMoreThanAllowedUsingScriptName() {
        // Given
        String scriptName = "ScriptName";
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptVar(scriptName, createKey(), createValue()));
    }

    @Test
    void shouldNotReturnScriptVariablesFromOtherScriptsUsingScriptName() {
        // Given
        String scriptName1 = "ScriptName1";
        String scriptName2 = "ScriptName2";
        String key = createKey();
        // When
        ScriptVars.setScriptVar(scriptName1, key, createValue());
        // Then
        assertThat(ScriptVars.getScriptVar(scriptName2, key), is(nullValue()));
    }

    @Test
    void shouldClearGlobalAndScriptVariables() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        ScriptVars.setGlobalCustomVar(createKey(), createCustomValue());
        // When
        ScriptVars.clear();
        // Then
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getGlobalCustomVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptVars(scriptName).size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptCustomVars(scriptName).size(), is(equalTo(0)));
    }

    @Test
    void shouldClearGlobalVariables() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptVar(scriptName, createKey(), createValue());
        ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        ScriptVars.setGlobalCustomVar(createKey(), createCustomValue());
        // When
        ScriptVars.clearGlobalVars();
        // Then
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getGlobalCustomVars().size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptVars(scriptName).size(), is(equalTo(1)));
    }

    @Test
    void shouldClearScriptVariables() {
        // Given
        String scriptName1 = "ScriptName1";
        String scriptName2 = "ScriptName2";
        ScriptVars.setScriptVar(scriptName1, createKey(), createValue());
        ScriptVars.setScriptCustomVar(scriptName1, createKey(), createCustomValue());
        ScriptVars.setScriptVar(scriptName2, createKey(), createValue());
        ScriptVars.setScriptCustomVar(scriptName2, createKey(), createCustomValue());
        ScriptVars.setGlobalVar(createKey(), createValue());
        ScriptVars.setGlobalCustomVar(createKey(), createCustomValue());
        // When
        ScriptVars.clearScriptVars(scriptName1);
        // Then
        assertThat(ScriptVars.getScriptVars(scriptName1).size(), is(equalTo(0)));
        assertThat(ScriptVars.getScriptCustomVars(scriptName1).size(), is(equalTo(0)));
        assertThat(ScriptVars.getGlobalVars().size(), is(equalTo(1)));
        assertThat(ScriptVars.getGlobalCustomVars().size(), is(equalTo(1)));
        assertThat(ScriptVars.getScriptVars(scriptName2).size(), is(equalTo(1)));
        assertThat(ScriptVars.getScriptCustomVars(scriptName2).size(), is(equalTo(1)));
    }

    @Test
    void shouldNotAllowToModifyReturnedGlobalCustomVariables() {
        // Given
        Map<String, Object> vars = ScriptVars.getGlobalCustomVars();
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> vars.put(createKey(), createCustomValue()));
    }

    @Test
    void shouldSetGlobalCustomVariable() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        // When
        ScriptVars.setGlobalCustomVar(key, value);
        // Then
        assertThat(ScriptVars.getGlobalCustomVars(), hasEntry(key, value));
        assertThat(ScriptVars.getGlobalCustomVar(key), is(equalTo(value)));
    }

    @Test
    void shouldClearGlobalCustomVariableWithNullValue() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        ScriptVars.setGlobalCustomVar(key, value);
        // When
        ScriptVars.setGlobalCustomVar(key, null);
        // Then
        assertThat(ScriptVars.getGlobalCustomVars(), not(hasEntry(key, value)));
        assertThat(ScriptVars.getGlobalCustomVar(key), is(nullValue()));
    }

    @Test
    void shouldNotSetGlobalCustomVariableWithNullKey() {
        // Given
        String key = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setGlobalCustomVar(key, createCustomValue()));
    }

    @Test
    void shouldNotSetGlobalCustomVariableIfMoreThanAllowed() {
        // Given
        for (int i = 0; i <= ScriptVars.MAX_GLOBAL_VARS; i++) {
            ScriptVars.setGlobalCustomVar(createKey(), createCustomValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setGlobalCustomVar(createKey(), createCustomValue()));
    }

    @Test
    void shouldHaveNoScriptCustomVariablesByDefault() {
        // Given
        String scriptName = "ScriptName";
        // When
        Map<String, Object> vars = ScriptVars.getScriptCustomVars(scriptName);
        // Then
        assertThat(vars, is(notNullValue()));
        assertThat(vars.size(), is(equalTo(0)));
    }

    @Test
    void shouldReturnNullForNoScriptCustomVariableSet() {
        // Given
        String scriptName = "ScriptName";
        String key = createKey();
        // When
        Object value = ScriptVars.getScriptCustomVar(scriptName, key);
        // Then
        assertThat(value, is(nullValue()));
    }

    @Test
    void shouldNotAllowToModifyReturnedScriptCustomVariables() {
        // Given
        String scriptName = "ScriptName";
        Map<String, Object> vars = ScriptVars.getScriptCustomVars(scriptName);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> vars.put(createKey(), createCustomValue()));
    }

    @Test
    void shouldSetScriptCustomVariableUsingScriptContext() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        // When
        ScriptVars.setScriptCustomVar(scriptContext, key, value);
        // Then
        assertThat(ScriptVars.getScriptCustomVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptCustomVar(scriptContext, key), is(equalTo(value)));
    }

    @Test
    void shouldClearScriptCustomVariableWithNullValueUsingScriptContext() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        String scriptName = "ScriptName";
        ScriptContext scriptContext = createScriptContextWithName(scriptName);
        ScriptVars.setScriptCustomVar(scriptContext, key, value);
        // When
        ScriptVars.setScriptCustomVar(scriptContext, key, null);
        // Then
        assertThat(ScriptVars.getScriptCustomVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptCustomVar(scriptContext, key), is(nullValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableUsingNullScriptContext() {
        // Given
        ScriptContext scriptContext = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptCustomVar(
                                scriptContext, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableUsingNullScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(null);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptCustomVar(
                                scriptContext, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableUsingNonStringScriptNameInScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName(10);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptCustomVar(
                                scriptContext, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableWithNullKeyUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptCustomVar(scriptContext, null, createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableWithInvalidKeyLengthUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptCustomVar(
                                scriptContext, createKeyWithInvalidLength(), createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableIfMoreThanAllowedUsingScriptContext() {
        // Given
        ScriptContext scriptContext = createScriptContextWithName("ScriptName");
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptCustomVar(scriptContext, createKey(), createCustomValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        ScriptVars.setScriptCustomVar(
                                scriptContext, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotReturnScriptCustomVariablesFromOtherScriptsUsingScriptContext() {
        // Given
        ScriptContext scriptContext1 = createScriptContextWithName("ScriptName1");
        ScriptContext scriptContext2 = createScriptContextWithName("ScriptName2");
        String key = createKey();
        // When
        ScriptVars.setScriptCustomVar(scriptContext1, key, createCustomValue());
        // Then
        assertThat(ScriptVars.getScriptCustomVar(scriptContext2, key), is(nullValue()));
    }

    @Test
    void shouldSetScriptCustomVariableUsingScriptName() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        String scriptName = "ScriptName";
        // When
        ScriptVars.setScriptCustomVar(scriptName, key, value);
        // Then
        assertThat(ScriptVars.getScriptCustomVars(scriptName), hasEntry(key, value));
        assertThat(ScriptVars.getScriptCustomVar(scriptName, key), is(equalTo(value)));
    }

    @Test
    void shouldClearScriptCustomVariableWithNullValueUsingScriptName() {
        // Given
        String key = createKey();
        Object value = createCustomValue();
        String scriptName = "ScriptName";
        ScriptVars.setScriptCustomVar(scriptName, key, value);
        // When
        ScriptVars.setScriptCustomVar(scriptName, key, null);
        // Then
        assertThat(ScriptVars.getScriptCustomVars(scriptName), not(hasEntry(key, value)));
        assertThat(ScriptVars.getScriptCustomVar(scriptName, key), is(nullValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableUsingNullScriptName() {
        // Given
        String scriptName = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotAllowToModifyReturnedScriptCustomVariablesSet() {
        // Given
        String scriptName = "ScriptName";
        ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue());
        Map<String, Object> vars = ScriptVars.getScriptCustomVars(scriptName);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> vars.put(createKey(), createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableWithNullKeyUsingScriptName() {
        // Given
        String key = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptCustomVar("ScriptName", key, createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableWithInvalidKeyLengthUsingScriptName() {
        // Given
        String key = createKeyWithInvalidLength();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptCustomVar("ScriptName", key, createCustomValue()));
    }

    @Test
    void shouldNotSetScriptCustomVariableIfMoreThanAllowedUsingScriptName() {
        // Given
        String scriptName = "ScriptName";
        for (int i = 0; i <= ScriptVars.MAX_SCRIPT_VARS; i++) {
            ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue());
        }
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> ScriptVars.setScriptCustomVar(scriptName, createKey(), createCustomValue()));
    }

    @Test
    void shouldNotReturnScriptCustomVariablesFromOtherScriptsUsingScriptName() {
        // Given
        String scriptName1 = "ScriptName1";
        String scriptName2 = "ScriptName2";
        String key = createKey();
        // When
        ScriptVars.setScriptCustomVar(scriptName1, key, createCustomValue());
        // Then
        assertThat(ScriptVars.getScriptCustomVar(scriptName2, key), is(nullValue()));
    }

    private static String createKey() {
        return "Key-" + Math.random();
    }

    private static String createValue() {
        return "Value-" + Math.random();
    }

    private static Object createCustomValue() {
        return Arrays.asList(createValue());
    }

    private static String createKeyWithInvalidLength() {
        return StringUtils.repeat("A", ScriptVars.MAX_KEY_SIZE + 1);
    }

    private static String createValueWithInvalidLength() {
        return StringUtils.repeat("A", ScriptVars.MAX_VALUE_SIZE + 1);
    }

    private static ScriptContext createScriptContextWithName(Object scriptName) {
        ScriptContext context = new SimpleScriptContext();
        context.setAttribute(
                ExtensionScript.SCRIPT_NAME_ATT, scriptName, ScriptContext.ENGINE_SCOPE);
        return context;
    }
}
