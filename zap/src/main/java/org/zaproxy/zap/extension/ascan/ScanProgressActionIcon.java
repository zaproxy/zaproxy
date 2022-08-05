/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.ZAP;

/** Clickable helper class for actions */
@SuppressWarnings("serial")
public class ScanProgressActionIcon extends JLabel {

    private static final long serialVersionUID = 1L;
    private static final ImageIcon completedIcon =
            new ImageIcon(ZAP.class.getResource("/resource/icon/10/102.png"));
    private static final ImageIcon skippedIcon =
            new ImageIcon(ZAP.class.getResource("/resource/icon/10/150.png"));
    private static final ImageIcon skipIcon =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_16.png"));
    private static final ImageIcon focusedSkipIcon =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_focused_16.png"));
    private static final ImageIcon pressedSkipIcon =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip1_pressed_16.png"));
    private static final ImageIcon SKIP_PENDING_ICON =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip-pending.png"));
    private static final ImageIcon SKIP_PENDING_FOCUSED_ICON =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip-pending-focused.png"));
    private static final ImageIcon SKIP_PENDING_PRESSED_ICON =
            new ImageIcon(ZAP.class.getResource("/resource/icon/16/skip-pending-pressed.png"));

    public static final int CLICKABLE_ICON_WIDTH = 24;
    public static final int CLICKABLE_ICON_HEIGHT = 16;
    private static final int STATE_NORMAL = 0;
    private static final int STATE_FOCUSED = 1;
    private static final int STATE_PRESSED = 2;

    private int state;
    private ScanProgressItem item;

    /**
     * Constructs a {@code ScanProgressActionIcon} for the given scan progress item.
     *
     * @param item the scan progress item
     */
    public ScanProgressActionIcon(ScanProgressItem item) {
        this.item = item;
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(CLICKABLE_ICON_WIDTH, CLICKABLE_ICON_HEIGHT));
        changeIcon();
    }

    /**
     * Updates this action icon with the given scan progress item.
     *
     * @param item new the scan progress item
     */
    public void updateStatus(ScanProgressItem item) {
        this.item = item;
        this.changeIcon();
    }

    private void changeIcon() {

        if (item.isSkipped()) {
            setIcon(skippedIcon);
            setToolTipText(getSkipText());

        } else if (item.isCompleted()) {
            setIcon(completedIcon);
            setToolTipText(Constant.messages.getString("ascan.progress.label.completed"));
        } else if (item.isStopped()) {
            setIcon(null);
            setToolTipText(null);
        } else if (item.isRunning() || item.isPending()) {
            boolean running = item.isRunning();
            ImageIcon icon = null;
            switch (state) {
                case STATE_NORMAL:
                    icon = running ? skipIcon : SKIP_PENDING_ICON;
                    break;

                case STATE_FOCUSED:
                    icon = running ? focusedSkipIcon : SKIP_PENDING_FOCUSED_ICON;
                    break;

                case STATE_PRESSED:
                    icon = running ? pressedSkipIcon : SKIP_PENDING_PRESSED_ICON;
                    break;
            }

            setIcon(icon);
            setToolTipText(Constant.messages.getString("ascan.progress.label.skipaction"));

        } else {
            setIcon(null);
            setToolTipText(null);
        }
    }

    /**
     * Gets the text that should be shown when the plugin is/was skipped.
     *
     * @return the text to show when the plugin is skipped.
     */
    private String getSkipText() {
        String reason = item.getSkippedReason();
        if (reason != null) {
            return Constant.messages.getString("ascan.progress.label.skippedWithReason", reason);
        }
        return Constant.messages.getString("ascan.progress.label.skipped");
    }

    public void invokeAction() {
        // do the Action
        item.skip();
        changeIcon();
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
