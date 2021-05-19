/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.junit.jupiter.api.Test;

class LayoutHelperUnitTest {

    private static final int X = 1;
    private static final int Y = 2;
    private static final int WIDTH = 3;
    private static final int HEIGHT = 4;
    private static final double WEIGHT_X = 4.5;
    private static final double WEIGHT_Y = 6.7;
    private static final int FILL = 8;
    private static final int ANCHOR = 9;
    private static final Insets INSETS = new Insets(10, 11, 12, 13);

    @Test
    void shouldResizeHorizontallyAndVerticallyByDefault() {
        // given
        // when
        GridBagConstraints constraints = LayoutHelper.getGBC(X, Y, WIDTH, WEIGHT_X);
        // then
        assertThat(constraints.fill, is(GridBagConstraints.BOTH));
    }

    @Test
    void shouldUseNorthWestAnchorByDefault() {
        // given
        // when
        GridBagConstraints constraints = LayoutHelper.getGBC(X, Y, WIDTH, WEIGHT_X);
        // then
        assertThat(constraints.anchor, is(GridBagConstraints.NORTHWEST));
    }

    @Test
    void shouldKeepDefaultInsetsOnGivenNullParameter() {
        // given
        // when
        GridBagConstraints constraints = LayoutHelper.getGBC(X, Y, WIDTH, WEIGHT_X, null);
        // then
        assertThat(constraints.insets, is(new Insets(0, 0, 0, 0)));
    }

    @Test
    void shouldSetAllGivenParameters() {
        // given
        // when
        GridBagConstraints constraints =
                LayoutHelper.getGBC(X, Y, WIDTH, HEIGHT, WEIGHT_X, WEIGHT_Y, FILL, ANCHOR, INSETS);
        // then
        assertThat(constraints.gridx, is(X));
        assertThat(constraints.gridy, is(Y));
        assertThat(constraints.gridwidth, is(WIDTH));
        assertThat(constraints.gridheight, is(HEIGHT));
        assertThat(constraints.weightx, is(WEIGHT_X));
        assertThat(constraints.weighty, is(WEIGHT_Y));
        assertThat(constraints.fill, is(FILL));
        assertThat(constraints.anchor, is(ANCHOR));
        assertThat(constraints.insets, is(INSETS));
    }
}
