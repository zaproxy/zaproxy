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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit test for {@link Tech}. */
class TechUnitTest {

    private static final String TOP_LEVEL = "TopLevel";

    private static final String SUB_LEVEL = "SubLevel";

    private static final String A_TOP_LEVEL = "aTopLevel";
    private static final String A_SECOND_LEVEL = "aSecondLevel";
    private static final String A_THIRD_LEVEL = "aThirdLevel";

    private static final Tech newTopLevelTech = new Tech(TOP_LEVEL);
    private static final Tech newTech = new Tech(Tech.Db, SUB_LEVEL);

    @Test
    void allShouldHaveTech() {
        assertThat(Tech.getAll()).isNotNull();
        assertThat(Tech.getAll()).isNotEmpty();
    }

    @Test
    void topLevelShouldHaveTech() {
        assertThat(Tech.getTopLevel()).isNotNull();
        assertThat(Tech.getTopLevel()).isNotEmpty();
    }

    @Test
    void allShouldHaveMoreTech() {
        assertThat(Tech.getAll().size()).isGreaterThan(Tech.getTopLevel().size());
    }

    @Test
    void allShouldContainTopLevel() {
        for (Tech top : Tech.getTopLevel()) {
            assertThat(Tech.getAll()).contains(top);
        }
    }

    @Test
    void addRemoveTopLevelTech() {
        int before = Tech.getAll().size();
        int beforeTop = Tech.getTopLevel().size();

        Tech.add(newTopLevelTech);
        assertThat(Tech.getAll().size()).isGreaterThan(before);
        assertThat(Tech.getTopLevel().size()).isGreaterThan(beforeTop);
        assertThat(Tech.getTopLevel()).contains(newTopLevelTech);
        assertThat(Tech.getAll()).contains(newTopLevelTech);

        Tech.remove(newTopLevelTech);
        assertThat(Tech.getTopLevel()).doesNotContain(newTopLevelTech);
        assertThat(Tech.getAll()).doesNotContain(newTopLevelTech);
        assertThat(Tech.getAll()).hasSize(before);
        assertThat(Tech.getTopLevel()).hasSize(beforeTop);
    }

    @Test
    void addRemoveTech() {
        int before = Tech.getAll().size();

        Tech.add(newTech);
        assertThat(Tech.getAll().size()).isGreaterThan(before);
        assertThat(Tech.getAll()).contains(newTech);
        assertThat(Tech.getTopLevel()).doesNotContain(newTech);

        Tech.remove(newTech);
        assertThat(Tech.getAll()).doesNotContain(newTech);
        assertThat(Tech.getTopLevel()).doesNotContain(newTech);
        assertThat(Tech.getAll()).hasSize(before);
    }

    @Test
    void getTech() {
        Tech.add(newTopLevelTech);
        Tech.add(newTech);

        assertThat(Tech.get(TOP_LEVEL)).isEqualTo(newTopLevelTech);
        assertThat(Tech.get(TOP_LEVEL.toLowerCase())).isEqualTo(newTopLevelTech);
        assertThat(Tech.get(TOP_LEVEL.toUpperCase())).isEqualTo(newTopLevelTech);
        assertThat(Tech.get(TOP_LEVEL + "  ")).isEqualTo(newTopLevelTech);
        assertThat(Tech.get("  " + TOP_LEVEL + "  ")).isEqualTo(newTopLevelTech);

        assertThat(Tech.get("Db." + SUB_LEVEL)).isEqualTo(newTech);
        assertThat(Tech.get("Db." + SUB_LEVEL.toLowerCase())).isEqualTo(newTech);
        assertThat(Tech.get("Db." + SUB_LEVEL.toUpperCase())).isEqualTo(newTech);
        assertThat(Tech.get("Db." + SUB_LEVEL + "  ")).isEqualTo(newTech);
        assertThat(Tech.get("  " + "Db." + SUB_LEVEL + "  ")).isEqualTo(newTech);

        // cleanup
        Tech.remove(newTopLevelTech);
        Tech.remove(newTech);
    }

    @Test
    void isShouldMatchVariousLevels() {
        // Given
        setupThreeLevels();
        Tech topLevel = Tech.get(A_TOP_LEVEL);
        Tech secondLevel = Tech.get(String.join(".", A_TOP_LEVEL, A_SECOND_LEVEL));
        Tech thirdLevel = Tech.get(String.join(".", A_TOP_LEVEL, A_SECOND_LEVEL, A_THIRD_LEVEL));
        // When
        boolean topIsTop = topLevel.is(topLevel);
        boolean secondIsTop = secondLevel.is(topLevel);
        boolean secondIsSecond = secondLevel.is(secondLevel);
        boolean thirdIsTop = thirdLevel.is(topLevel);
        boolean thirdIsSecond = thirdLevel.is(secondLevel);
        boolean thirdIsThird = thirdLevel.is(thirdLevel);

        boolean topIsNotOtherTop = topLevel.is(Tech.Db);
        boolean thirdIsNotOtherTop = thirdLevel.is(Tech.Db);
        // Then
        assertThat(topIsTop).isTrue();
        assertThat(secondIsTop).isTrue();
        assertThat(secondIsSecond).isTrue();
        assertThat(thirdIsTop).isTrue();
        assertThat(thirdIsSecond).isTrue();
        assertThat(thirdIsThird).isTrue();

        assertThat(topIsNotOtherTop).isFalse();
        assertThat(thirdIsNotOtherTop).isFalse();
    }

    private void setupThreeLevels() {
        Tech newTopLevel = new Tech(A_TOP_LEVEL);
        Tech newTech = new Tech(newTopLevel, A_SECOND_LEVEL);
        Tech otherTech = new Tech(newTech, A_THIRD_LEVEL);
        Tech.add(newTopLevel);
        Tech.add(newTech);
        Tech.add(otherTech);
    }
}
