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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

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
        assertThat(Tech.getAll(), notNullValue());
        assertThat(Tech.getAll(), not(empty()));
    }

    @Test
    void topLevelShouldHaveTech() {
        assertThat(Tech.getTopLevel(), notNullValue());
        assertThat(Tech.getTopLevel(), not(empty()));
    }

    @Test
    void allShouldHaveMoreTech() {
        assertThat(Tech.getAll().size(), greaterThan(Tech.getTopLevel().size()));
    }

    @Test
    void allShouldContainTopLevel() {
        for (Tech top : Tech.getTopLevel()) {
            assertThat(Tech.getAll(), hasItem(top));
        }
    }

    @Test
    void addRemoveTopLevelTech() {
        int before = Tech.getAll().size();
        int beforeTop = Tech.getTopLevel().size();

        Tech.add(newTopLevelTech);
        assertThat(Tech.getAll().size(), greaterThan(before));
        assertThat(Tech.getTopLevel().size(), greaterThan(beforeTop));
        assertThat(Tech.getTopLevel(), hasItem(newTopLevelTech));
        assertThat(Tech.getAll(), hasItem(newTopLevelTech));

        Tech.remove(newTopLevelTech);
        assertThat(Tech.getTopLevel(), not(hasItem(newTopLevelTech)));
        assertThat(Tech.getAll(), not(hasItem(newTopLevelTech)));
        assertThat(Tech.getAll().size(), is(before));
        assertThat(Tech.getTopLevel().size(), is(beforeTop));
    }

    @Test
    void addRemoveTech() {
        int before = Tech.getAll().size();

        Tech.add(newTech);
        assertThat(Tech.getAll().size(), greaterThan(before));
        assertThat(Tech.getAll(), hasItem(newTech));
        assertThat(Tech.getTopLevel(), not(hasItem(newTech)));

        Tech.remove(newTech);
        assertThat(Tech.getAll(), not(hasItem(newTech)));
        assertThat(Tech.getTopLevel(), not(hasItem(newTech)));
        assertThat(Tech.getAll().size(), is(before));
    }

    @Test
    void getTech() {
        Tech.add(newTopLevelTech);
        Tech.add(newTech);

        assertThat(Tech.get(TOP_LEVEL), is(newTopLevelTech));
        assertThat(Tech.get(TOP_LEVEL.toLowerCase()), is(newTopLevelTech));
        assertThat(Tech.get(TOP_LEVEL.toUpperCase()), is(newTopLevelTech));
        assertThat(Tech.get(TOP_LEVEL + "  "), is(newTopLevelTech));
        assertThat(Tech.get("  " + TOP_LEVEL + "  "), is(newTopLevelTech));

        assertThat(Tech.get("Db." + SUB_LEVEL), is(newTech));
        assertThat(Tech.get("Db." + SUB_LEVEL.toLowerCase()), is(newTech));
        assertThat(Tech.get("Db." + SUB_LEVEL.toUpperCase()), is(newTech));
        assertThat(Tech.get("Db." + SUB_LEVEL + "  "), is(newTech));
        assertThat(Tech.get("  " + "Db." + SUB_LEVEL + "  "), is(newTech));

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
        assertThat(topIsTop, is(equalTo(true)));
        assertThat(secondIsTop, is(equalTo(true)));
        assertThat(secondIsSecond, is(equalTo(true)));
        assertThat(thirdIsTop, is(equalTo(true)));
        assertThat(thirdIsSecond, is(equalTo(true)));
        assertThat(thirdIsThird, is(equalTo(true)));

        assertThat(topIsNotOtherTop, is(equalTo(false)));
        assertThat(thirdIsNotOtherTop, is(equalTo(false)));
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
