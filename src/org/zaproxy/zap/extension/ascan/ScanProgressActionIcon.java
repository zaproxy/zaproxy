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

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.ZAP;

/**
 * Clickable helper class for actions
 */
public class ScanProgressActionIcon extends JLabel {

    private static final long serialVersionUID = 1L;
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
        changeIcon();
    }

    /**
     * 
     * @param item 
     */
    public void updateStatus(ScanProgressItem item) {
        this.item = item;
        this.changeIcon();
    }

    private void changeIcon() {

        if (item.isSkipped()) {
            setIcon(skippedIcon);
            setToolTipText(getSkipText());

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
     * Gets the text that should be shown when the plugin is/was skipped.
     *
     * @return the text to show when the plugin is skipped.
     */
    private String getSkipText(){
        String reason = item.getSkippedReason();
        if (reason != null) {
            return Constant.messages.getString("ascan.progress.label.skippedWithReason", reason);
        }
        return Constant.messages.getString("ascan.progress.label.skipped");
    }

    public void invokeAction() {
        // do the Action
        item.skip();
    }

    public void setPressed() {
        state = STATE_PRESSED;
        changeIcon();
    }

    public void setReleased() {
        if (state == STATE_PRESSED) {
            state = STATE_FOCUSED;
            changeIcon();
        }
    }

    public void setOver() {
        if (state == STATE_NORMAL) {
            state = STATE_FOCUSED;
            changeIcon();
        }
    }

    public void setNormal() {
        state = STATE_NORMAL;
        changeIcon();
    }

    @Override
    public String toString() {
        if (item.isSkipped()) {
            return getSkipText();
        }
        return item.getStatusLabel();
    }
}
