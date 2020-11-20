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
package org.zaproxy.testutils;

import java.awt.GraphicsEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An {@link ExecutionCondition} to run or not GUI tests.
 *
 * <p>Tests are not enabled if the environment is headless.
 *
 * @see GraphicsEnvironment#isHeadless()
 */
public class GuiCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
            ConditionEvaluationResult.enabled("Environment not headless");
    private static final ConditionEvaluationResult DISABLED =
            ConditionEvaluationResult.disabled("Environment is headless");

    private static Boolean canRunTests;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (!canRunGuiTests()) {
            return DISABLED;
        }
        return ENABLED;
    }

    private static boolean canRunGuiTests() {
        if (canRunTests == null) {
            if (GraphicsEnvironment.isHeadless()) {
                canRunTests = false;
            } else {
                try {
                    // Check that the GraphicsEnvironment is actually available.
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
                    canRunTests = true;
                } catch (Throwable e) {
                    canRunTests = false;
                }
            }
        }
        return canRunTests;
    }
}
