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
package org.zaproxy.zap.view.panelsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class SearchUnitTest {

    @Test
    void shouldFindButtonByText() {
        String btnText = "myCusTomText";
        String panelName = "xyu";
        JPanel panel = new JPanel();
        panel.setName(panelName);
        panel.add(new JButton(btnText));

        Search search = new Search(Search.DefaultComponentSearchItems);
        SearchQuery query = new InStringSearchQuery("stom");
        ArrayList<FoundComponent> findings = search.searchFor(panel, query);

        FoundComponent foundComponent = findings.get(0);
        JPanel foundPanel = foundComponent.getParentAsCasted(0);
        assertThat(foundPanel.getName(), is(panelName));

        JButton foundButton = foundComponent.getComponentCasted();
        assertThat(foundButton.getText(), is(btnText));
    }
}
