/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.alert;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.parosproxy.paros.core.scanner.Alert;

public class ExtensionAlertUnitTest {
    
    private static final String ORIGINAL_NAME = "Original Name";
    private static final String ORIGINAL_DESC = "Original Desc";
    private static final String ORIGINAL_SOLN = "Original Solution";
    private static final String ORIGINAL_OTHER = "Original Other";
    private static final String ORIGINAL_REF = "Original Ref";

    private static final String NEW_NAME = "New Name";
    private static final String NEW_DESC = "New Desc";
    private static final String NEW_SOLN = "New Solution";
    private static final String NEW_OTHER = "New Other";
    private static final String NEW_REF = "New Ref";

    private ExtensionAlert extAlert;

    @Before
    public void setUp() throws Exception {
        extAlert = new ExtensionAlert();
    }
    
    private Alert newAlert (int pluginId) {
        Alert alert = new Alert(pluginId);
        alert.setName(ORIGINAL_NAME);
        alert.setDescription(ORIGINAL_DESC);
        alert.setSolution(ORIGINAL_SOLN);
        alert.setOtherInfo(ORIGINAL_OTHER);
        alert.setReference(ORIGINAL_REF);
        return alert;
    }

    @Test
    public void shouldReplaceAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", NEW_NAME);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(NEW_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldAppendAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "+" + NEW_NAME);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME + NEW_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldPrependAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "-" + NEW_NAME);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(NEW_NAME + ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldReplaceAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", NEW_DESC);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(NEW_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldAppendAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "+" + NEW_DESC);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC + NEW_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldPrependAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "-" + NEW_DESC);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(NEW_DESC + ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }
    
    @Test
    public void shouldReplaceAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", NEW_SOLN);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(NEW_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldAppendAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "+" + NEW_SOLN);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN + NEW_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldPrependAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "-" + NEW_SOLN);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(NEW_SOLN + ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }
    @Test
    public void shouldReplaceAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", NEW_OTHER);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(NEW_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldAppendAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "+" + NEW_OTHER);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER + NEW_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldPrependAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "-" + NEW_OTHER);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(NEW_OTHER + ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldReplaceAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", NEW_REF);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(NEW_REF, alert1.getReference());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldAppendAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "+" + NEW_REF);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF + NEW_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

    @Test
    public void shouldPrependAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "-" + NEW_REF);
        
        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(NEW_REF + ORIGINAL_REF, alert1.getReference());
        
        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
    }

}
