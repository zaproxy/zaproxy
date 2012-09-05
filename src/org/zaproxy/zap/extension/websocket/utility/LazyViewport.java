/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.websocket.utility;

import javax.swing.JViewport;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Point;

/**
 * A subclass of JViewport that overrides a method in order not to repaint its
 * component many times during a vertical drag.
 * 
 * @author Brian Cole
 */
public class LazyViewport extends JViewport {
    private static final long serialVersionUID = 2006L;

    /**
     * equivalent to <b>new JScrollPane(view)</b> except uses a LazyViewport
     */
    public static JScrollPane createLazyScrollPaneFor(Component view) {
        LazyViewport vp = new LazyViewport();
        vp.setView(view);
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewport(vp);
        return scrollpane;
    }

    /**
     * overridden to not repaint during during a vertical drag
     */
    @Override
    public void setViewPosition(Point p) {
        Component parent = getParent();
        if ( parent instanceof JScrollPane &&
             ((JScrollPane)parent).getVerticalScrollBar().getValueIsAdjusting() ) {
            // value is adjusting, skip repaint
            return;
        }
        super.setViewPosition(p);
    }
}