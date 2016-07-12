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
package org.zaproxy.zap.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

/**
 * A {@code JPanel} containing two {@code JTree} shown one above the other, used to display the contexts and sites trees in the
 * same panel.
 *
 * @since TODO add version
 */
public class ContextsSitesPanel extends JPanel {

    private static final long serialVersionUID = -3325400144404304335L;

    /**
     * Constructs a {@code ContextsSitesPanel} with the given contexts and sites trees.
     *
     * @param contextsTree the contexts tree
     * @param sitesTree the sites tree
     * @throws IllegalArgumentException if any of the given parameters is {@code null}.
     */
    public ContextsSitesPanel(JTree contextsTree, JTree sitesTree) {
        this(contextsTree, sitesTree, null);
    }

    /**
     * Constructs a {@code ContextsSitesPanel} with the given contexts and sites trees and with the given name for the
     * {@code JScrollPane}.
     *
     * @param contextsTree the contexts tree
     * @param sitesTree the sites tree
     * @param scrollPaneName the name of the {@code JScrollPane}, might be {@code null}
     * @throws IllegalArgumentException if any of the trees is {@code null}.
     */
    public ContextsSitesPanel(JTree contextsTree, JTree sitesTree, String scrollPaneName) {
        super(new BorderLayout());
        validateNonNull(contextsTree, "contextsTree");
        validateNonNull(sitesTree, "sitesTree");

        JScrollPane scrollPane = new JScrollPane();
        if (scrollPaneName != null) {
            scrollPane.setName(scrollPaneName);
        }

        JPanel panel = new ScrollableTreesPanel(contextsTree, sitesTree);
        scrollPane.setViewportView(panel);

        add(scrollPane);
    }

    private static void validateNonNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("The " + parameterName + " must not be null.");
        }
    }

    private static class ScrollableTreesPanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 2709986817434976954L;

        private final JTree contextsTree;
        private final JTree sitesTree;

        public ScrollableTreesPanel(JTree contextsTree, JTree sitesTree) {
            super(new BorderLayout());

            this.contextsTree = contextsTree;
            add(contextsTree, BorderLayout.NORTH);

            this.sitesTree = sitesTree;
            add(sitesTree, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension dNT = contextsTree.getPreferredScrollableViewportSize();
            Dimension dCT = sitesTree.getPreferredScrollableViewportSize();
            dCT.setSize(Math.max(dNT.getWidth(), dCT.getWidth()), dNT.getHeight() + dCT.getHeight());
            return dCT;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            if (visibleRect.getY() < sitesTree.getBounds().getY()) {
                return contextsTree.getScrollableUnitIncrement(visibleRect, orientation, direction);
            }
            return sitesTree.getScrollableUnitIncrement(visibleRect, orientation, direction);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            // Same behaviour for both trees.
            return sitesTree.getScrollableBlockIncrement(visibleRect, orientation, direction);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            int width = Math.max(sitesTree.getPreferredSize().width, contextsTree.getPreferredSize().width);
            return SwingUtilities.getUnwrappedParent(this).getWidth() > width;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return SwingUtilities.getUnwrappedParent(this)
                    .getHeight() > (sitesTree.getPreferredSize().height + contextsTree.getPreferredSize().height);
        }
    }
}