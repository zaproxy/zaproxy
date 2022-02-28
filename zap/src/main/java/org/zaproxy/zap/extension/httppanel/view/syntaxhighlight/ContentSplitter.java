/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight;

import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.DisplayUtils;

public class ContentSplitter {

    private static final int DISABLE_WRAP_LENGTH = 250_000;
    private static final int SPLIT_LINE_LENGTH = 15_000;
    private static final char NEWLINE = '\n';

    private enum SplitState {
        UNCHANGED,
        SINGLE_LINE,
        LONG_LINES,
        LONG_CONTENT;
    }

    private static Icon warnIcon;
    private static Icon infoIcon;

    private final JPanel panel;
    private HttpPanelSyntaxHighlightTextArea textArea;
    private Boolean lineWrap;
    private JLabel messageLabel;
    private SplitState state;
    private String originalData;

    private int[] highlightOffsets;

    public ContentSplitter(JPanel panel) {
        this.panel = panel;

        highlightOffsets = new int[2];
        state = SplitState.UNCHANGED;
        messageLabel = new JLabel();
    }

    private static Icon getWarnIcon() {
        if (warnIcon == null) {
            warnIcon = createIcon("/resource/icon/16/050.png");
        }
        return warnIcon;
    }

    private static Icon createIcon(String name) {
        return DisplayUtils.getScaledIcon(ContentSplitter.class.getResource(name));
    }

    private static Icon getInfoIcon() {
        if (infoIcon == null) {
            infoIcon = createIcon("/resource/icon/fugue/information-white.png");
        }
        return infoIcon;
    }

    public void setTextArea(HttpPanelSyntaxHighlightTextArea textArea) {
        this.textArea = textArea;
    }

    public String process(String data) {
        String newData = splitContent(data);
        if (state == SplitState.UNCHANGED) {
            restoreLineWrapState();
            return data;
        }

        messageLabel.setText(state.toString());
        if (lineWrap == null) {
            lineWrap = textArea.getLineWrap();
            textArea.setLineWrap(false);
            textArea.setLineWrapDisabled(true);
            panel.add(BorderLayout.PAGE_END, messageLabel);
            panel.revalidate();
            panel.repaint();
        }

        if (state == SplitState.LONG_CONTENT) {
            messageLabel.setText(
                    Constant.messages.getString("http.panel.response.content.wrapdisabled"));
            messageLabel.setIcon(getInfoIcon());
            messageLabel.setToolTipText(
                    Constant.messages.getString(
                            "http.panel.response.content.wrapdisabled.tooltip"));
            originalData = null;
            return data;
        }

        messageLabel.setText(Constant.messages.getString("http.panel.response.content.modified"));
        messageLabel.setIcon(getWarnIcon());
        messageLabel.setToolTipText(
                Constant.messages.getString("http.panel.response.content.modified.tooltip"));
        originalData = data;
        return newData;
    }

    private void restoreLineWrapState() {
        if (lineWrap != null) {
            originalData = null;
            textArea.setLineWrap(lineWrap);
            textArea.setLineWrapDisabled(false);
            lineWrap = null;
            panel.remove(messageLabel);
            panel.revalidate();
            panel.repaint();
        }
    }

    private String splitContent(String data) {
        int length = data.length();
        if (length < SPLIT_LINE_LENGTH) {
            state = SplitState.UNCHANGED;
            return data;
        }

        int pos = data.indexOf(NEWLINE);
        if (pos == -1) {
            state = SplitState.SINGLE_LINE;
            StringBuilder strBuilder = createStringBuilder(length);
            splitInto(data, strBuilder, 0, length);
            return strBuilder.toString();
        }

        int curr = 0;
        int maxLineLen = 0;
        StringBuilder strBuilder = null;
        do {
            int lineLenth;
            if ((lineLenth = pos - curr) > SPLIT_LINE_LENGTH) {
                if (strBuilder == null) {
                    strBuilder = createStringBuilder(length);
                    strBuilder.append(data, 0, curr);
                    if (data.charAt(curr) == NEWLINE) {
                        curr += 1;
                    }
                }
                splitInto(data, strBuilder, curr, pos);
            } else if (strBuilder != null) {
                strBuilder.append(data, curr, pos);
            }
            maxLineLen = Math.max(lineLenth, maxLineLen);

            curr = pos;
            pos = data.indexOf(NEWLINE, curr + 1);
            if (pos == -1) {
                pos = length;
            }

        } while (curr < length);

        if (strBuilder == null) {
            state = length >= DISABLE_WRAP_LENGTH ? SplitState.LONG_CONTENT : SplitState.UNCHANGED;
            return data;
        }

        state = SplitState.LONG_LINES;
        return strBuilder.toString();
    }

    private static StringBuilder createStringBuilder(int length) {
        return new StringBuilder(length + length % SPLIT_LINE_LENGTH);
    }

    private static void splitInto(String data, StringBuilder strBuilder, int start, int end) {
        int pos = start;
        do {
            if (strBuilder.length() != 0) {
                strBuilder.append(NEWLINE);
            }
            int splitEnd = Math.min(pos + SPLIT_LINE_LENGTH, end);
            strBuilder.append(data, pos, splitEnd);
            pos = splitEnd;
        } while (pos < end);
    }

    public int[] highlightOffsets(int start, int end) {
        highlightOffsets[0] = start;
        highlightOffsets[1] = end;

        if (originalData != null) {
            try {
                int excessChars = 0;
                int pos = 0;
                while ((pos = originalData.indexOf(NEWLINE, pos)) != -1 && pos < end) {
                    pos += 1;
                    ++excessChars;
                }

                int offset = textArea.getLineOfOffset(start) - excessChars;
                highlightOffsets[0] += offset;
                highlightOffsets[1] += offset;

            } catch (BadLocationException ignore) {
            }
        }

        return highlightOffsets;
    }
}
