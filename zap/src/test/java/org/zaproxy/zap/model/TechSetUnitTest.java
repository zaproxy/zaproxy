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
package org.zaproxy.zap.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test for {@link TechSet}. */
class TechSetUnitTest {

    @Test
    void getAllTechShouldHaveTech() {
        assertThat(TechSet.getAllTech().getIncludeTech(), contains(Tech.getTopLevel().toArray()));
        assertThat(TechSet.getAllTech().getExcludeTech(), empty());
    }

    @ParameterizedTest
    @MethodSource("shouldReturnTestableTechs")
    void shouldExcludeTechsFromTechSetBasedOnSubset(Tech tech) {
        // Given
        TechSet full = new TechSet();
        Set<Tech> techsToExclude = TechSet.getAll(tech);
        // When
        full.excludeAll(tech);
        // Then
        Set<Tech> excludedSet = full.getExcludeTech();
        int langSize = techsToExclude.size();
        assertThat(excludedSet.size(), is(equalTo(langSize)));
        assertTrue(excludedSet.containsAll(techsToExclude));
    }

    @ParameterizedTest
    @MethodSource("shouldReturnTestableTechs")
    void shouldIncludeTechsInTechSetBasedOnSubset(Tech tech) {
        // Given
        TechSet full = new TechSet();
        Set<Tech> techsToInclude = TechSet.getAll(tech);
        // When
        full.includeAll(tech);
        // Then
        Set<Tech> includeSet = full.getIncludeTech();
        int langSize = techsToInclude.size();
        assertThat(includeSet.size(), is(equalTo(langSize)));
        assertTrue(includeSet.containsAll(techsToInclude));
    }

    private static Stream<Arguments> shouldReturnTestableTechs() {
        return Stream.of(
                Arguments.of(Tech.Lang), Arguments.of(Tech.JAVA), Arguments.of(Tech.SPRING));
    }
}
