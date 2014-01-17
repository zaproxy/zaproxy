/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.ZAP;

/**
 * Clickable helper class for actions
 */
public class ScanProgressActionIcon extends JLabel implements MouseListener {

    private static final ImageIcon completedIcon = new ImageIcon(ZAP.class.getResource("/resource/icon/10/102.png"));
    private static final ImageIcon skippedIcon = new ImageIcon(ZAP.class.getResource("/resource/icon/10/150.png"));
    private static final ImageIcon skipIcon = new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_16.png"));
    private static final ImageIcon focusedSkipIcon = new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_focused_16.png"));
    private static final ImageIcon pressedSkipIcon = new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_pressed_16.png"));
    
    public static final int CLICKABLE_ICON_WIDTH = 24;
    public static final int CLICKABLE_ICON_HEIGHT = 16;
    private static final int STATE_NORMAL = 0;
    private static final int STATE_FOCUSED = 1;
    private static final int STATE_PRESSED = 2;
    
    private int state;
    private ScanProgressItem item;

    /**
     *
     * @param plugin
     */
    public ScanProgressActionIcon(ScanProgressItem item) {
        this.item = item;
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(CLICKABLE_ICON_WIDTH, CLICKABLE_ICON_HEIGHT));
    }

    /**
     * 
     * @param item
     * @param container
     * @param sx
     * @param sy
     * @param width
     * @param height 
     */
    public void updatePluginStatus(ScanProgressItem item, Container container, int sx, int sy, int width, int height) {
        this.item = item;

        if (item.isRunning() && !item.isSkipped()) {

            Point point = container.getMousePosition(true);
            this.state = (point != null)
                    && (sx <= point.x) && (point.x < sx + width)
                    && (sy <= point.y) && (point.y < sy + height)
                    ? STATE_FOCUSED : STATE_NORMAL;

            addMouseListener(this);

        } else {
            removeMouseListener(this);
        }

        this.changeIcon();
    }

    /**
     *
     * @param container
     * @param sx
     * @param sy
     * @param width
     * @param height
     */
    public void initializeState(Container container, int sx, int sy, int width, int height) {

        if (item.isRunning()) {
            Point point = null;
            try { // workaround for issue #146185, getMousePosition() may throw NPE
                point = container.getMousePosition(true);

            } catch (NullPointerException e) {
            }

            this.state = (point != null)
                    && (sx <= point.x) && (point.x < sx + width)
                    && (sy <= point.y) && (point.y < sy + height)
                    ? STATE_FOCUSED : STATE_NORMAL;

            addMouseListener(this);
        }

        this.changeIcon();
    }

    /**
     * 
     */
    private void changeIcon() {

        if (item.isSkipped()) {
            setIcon(skippedIcon);
            setToolTipText(Constant.messages.getString("ascan.progress.label.skipped"));

        } else if (item.isRunning()) {
            ImageIcon icon = null;
            switch (state) {
                case STATE_NORMAL:
                    icon = skipIcon;
                    break;

                case STATE_FOCUSED:
                    icon = focusedSkipIcon;
                    break;

                case STATE_PRESSED:
                    icon = pressedSkipIcon;
                    break;
            }

            setIcon(icon);
            setToolTipText(Constant.messages.getString("ascan.progress.label.skipaction"));

        } else {
            setIcon(completedIcon);
            setToolTipText(Constant.messages.getString("ascan.progress.label.completed"));
        }
    }

    /**
     * 
     */
    private void invokeAction() {
        // do the Action
        item.skip();
    }

    // **************************************************************************
    // MouseListener
    // **************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        invokeAction();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        state = STATE_PRESSED;
        changeIcon();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (state == STATE_PRESSED) {
            state = STATE_FOCUSED;
            changeIcon();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            state = STATE_NORMAL;

        } else {
            state = STATE_FOCUSED;
        }

        //setFocusedPlugin();
        changeIcon();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        state = STATE_NORMAL;
        //setFocusedPlugin();
        changeIcon();
    }
}
