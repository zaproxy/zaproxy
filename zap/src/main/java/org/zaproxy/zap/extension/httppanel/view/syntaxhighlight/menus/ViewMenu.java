/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.menus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.HttpPanelSyntaxHighlightTextArea;

public class ViewMenu extends ExtensionPopupMenu {

    private static final long serialVersionUID = -6295434374221271825L;

    private static final String MENU_LABEL =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.label");
    private static final String ANTI_ALIASING =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.antiAliasing");
    private static final String SHOW_LINE_NUMBERS =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.showLineNumbers");
    private static final String CODE_FOLDING =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.codeFolding");
    private static final String WORD_WRAP =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.wordWrap");
    private static final String HIGHLIGHT_CURRENT_LINE =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.highlightCurrentLine");
    private static final String FADE_CURRENT_HIGHLIGHT_LINE =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.fadeCurrentHighlightLine");
    private static final String SHOW_WHITESPACE_CHARACTERS =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.showWhitespaceCharacters");
    private static final String SHOW_NEWLINE_CHARACTERS =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.showNewlineCharacters");
    private static final String MARK_OCCURRENCES =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.markOccurrences");
    private static final String ROUNDED_SELECTION_EDGES =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.roundedSelectionEdges");
    private static final String BRACKET_MATCHING =
            Constant.messages.getString("http.panel.view.syntaxtext.popup.view.bracketMatching");
    private static final String ANIMATED_BRACKET_MATCHING =
            Constant.messages.getString(
                    "http.panel.view.syntaxtext.popup.view.animatedBracketMatching");

    private JCheckBoxMenuItem antiAliasingOption;
    private JCheckBoxMenuItem lineNumbersOption;
    private JCheckBoxMenuItem codeFoldingAction;
    private JCheckBoxMenuItem wordWrapOption;

    private JCheckBoxMenuItem highlightCurrentLineOption;
    private JCheckBoxMenuItem fadeCurrentHighlightLineOption;

    private JCheckBoxMenuItem showWhitespacesOption;
    private JCheckBoxMenuItem showNewlinesOption;

    private JCheckBoxMenuItem markOccurrencesOption;

    private JCheckBoxMenuItem roundedSelectionEdgesOption;

    private JCheckBoxMenuItem bracketMatchingOption;
    private JCheckBoxMenuItem animatedBracketMatchingOption;

    public ViewMenu() {
        super(MENU_LABEL);

        antiAliasingOption = createAndAddOption(new ChangeAntiAliasingAction(ANTI_ALIASING), this);
        lineNumbersOption =
                createAndAddOption(new ChangeLineNumbersAction(SHOW_LINE_NUMBERS), this);
        codeFoldingAction = createAndAddOption(new ChangeCodeFoldingAction(CODE_FOLDING), this);
        wordWrapOption = createAndAddOption(new ChangeWordWrapAction(WORD_WRAP), this);
        addSeparator();
        highlightCurrentLineOption =
                createAndAddOption(
                        new ChangeHighlightCurrentLineAction(HIGHLIGHT_CURRENT_LINE), this);
        fadeCurrentHighlightLineOption =
                createAndAddOption(
                        new ChangeFadeCurrentHighlightLineAction(FADE_CURRENT_HIGHLIGHT_LINE),
                        this);
        addSeparator();
        showWhitespacesOption =
                createAndAddOption(
                        new ChangeShowWhitespacesAction(SHOW_WHITESPACE_CHARACTERS), this);
        showNewlinesOption =
                createAndAddOption(new ChangeShowNewlinesAction(SHOW_NEWLINE_CHARACTERS), this);
        addSeparator();
        markOccurrencesOption =
                createAndAddOption(new ChangeMarkOccurrencesAction(MARK_OCCURRENCES), this);
        addSeparator();
        roundedSelectionEdgesOption =
                createAndAddOption(
                        new ChangeRoundedSelectionEdgesAction(ROUNDED_SELECTION_EDGES), this);
        addSeparator();
        bracketMatchingOption =
                createAndAddOption(new ChangeBracketMatchingAction(BRACKET_MATCHING), this);
        animatedBracketMatchingOption =
                createAndAddOption(
                        new ChangeAnimatedBracketMatchingAction(ANIMATED_BRACKET_MATCHING), this);
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof HttpPanelSyntaxHighlightTextArea) {
            HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                    (HttpPanelSyntaxHighlightTextArea) invoker;

            updateState(httpPanelTextArea);
            return true;
        }
        return false;
    }

    public void updateState(HttpPanelSyntaxHighlightTextArea httpPanelTextArea) {

        antiAliasingOption.setSelected(httpPanelTextArea.getAntiAliasingEnabled());

        boolean selected = false;
        boolean enabled = false;
        Component c = httpPanelTextArea.getParent();
        if (c instanceof JViewport) {
            c = c.getParent();
            if (c instanceof RTextScrollPane) {
                enabled = true;
                final RTextScrollPane scrollPane = (RTextScrollPane) c;
                selected = scrollPane.getLineNumbersEnabled();
            }
        }

        lineNumbersOption.setVisible(enabled);
        lineNumbersOption.setSelected(selected);

        wordWrapOption.setSelected(httpPanelTextArea.getLineWrap());

        codeFoldingAction.setVisible(enabled && httpPanelTextArea.isCodeFoldingAllowed());
        codeFoldingAction.setSelected(httpPanelTextArea.isCodeFoldingEnabled());

        highlightCurrentLineOption.setSelected(httpPanelTextArea.getHighlightCurrentLine());
        fadeCurrentHighlightLineOption.setSelected(httpPanelTextArea.getFadeCurrentLineHighlight());

        showWhitespacesOption.setSelected(httpPanelTextArea.isWhitespaceVisible());
        showNewlinesOption.setSelected(httpPanelTextArea.getEOLMarkersVisible());

        markOccurrencesOption.setSelected(httpPanelTextArea.getMarkOccurrences());

        roundedSelectionEdgesOption.setSelected(httpPanelTextArea.getRoundedSelectionEdges());

        bracketMatchingOption.setSelected(httpPanelTextArea.isBracketMatchingEnabled());
        animatedBracketMatchingOption.setSelected(httpPanelTextArea.getAnimateBracketMatching());
    }

    public void setWordWrapEnabled(boolean enabled) {
        wordWrapOption.setEnabled(enabled);
    }

    private JCheckBoxMenuItem createAndAddOption(TextAction action, JMenu menu) {
        JCheckBoxMenuItem option = new JCheckBoxMenuItem(action);
        option.setSelected(false);
        menu.add(option);
        return option;
    }

    private static class ChangeAntiAliasingAction extends TextAction {

        private static final long serialVersionUID = 5408392183841188837L;

        public ChangeAntiAliasingAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setAntiAliasingEnabled(
                        !httpPanelTextArea.getAntiAliasingEnabled());
            }
        }
    }

    private static class ChangeLineNumbersAction extends TextAction {

        private static final long serialVersionUID = 7253500343033234417L;

        public ChangeLineNumbersAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                Component c = httpPanelTextArea.getParent();
                if (c instanceof JViewport) {
                    c = c.getParent();
                    if (c instanceof RTextScrollPane) {
                        final RTextScrollPane scrollPane = (RTextScrollPane) c;
                        scrollPane.setLineNumbersEnabled(!scrollPane.getLineNumbersEnabled());
                    }
                }
            }
        }
    }

    private static class ChangeCodeFoldingAction extends TextAction {

        private static final long serialVersionUID = 8169545961043587586L;

        public ChangeCodeFoldingAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                Component c = httpPanelTextArea.getParent();
                if (c instanceof JViewport) {
                    c = c.getParent();
                    if (c instanceof RTextScrollPane) {
                        final RTextScrollPane scrollPane = (RTextScrollPane) c;
                        scrollPane.setFoldIndicatorEnabled(
                                !httpPanelTextArea.isCodeFoldingEnabled());
                        httpPanelTextArea.setCodeFoldingEnabled(
                                !httpPanelTextArea.isCodeFoldingEnabled());
                    }
                }
            }
        }
    }

    private static class ChangeWordWrapAction extends TextAction {

        private static final long serialVersionUID = -8654200226170189435L;

        public ChangeWordWrapAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setLineWrap(!httpPanelTextArea.getLineWrap());
            }
        }
    }

    private static class ChangeHighlightCurrentLineAction extends TextAction {

        private static final long serialVersionUID = -1464472023983865781L;

        public ChangeHighlightCurrentLineAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setHighlightCurrentLine(
                        !httpPanelTextArea.getHighlightCurrentLine());
            }
        }
    }

    private static class ChangeFadeCurrentHighlightLineAction extends TextAction {

        private static final long serialVersionUID = -1430399597611245037L;

        public ChangeFadeCurrentHighlightLineAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setFadeCurrentLineHighlight(
                        !httpPanelTextArea.getFadeCurrentLineHighlight());
            }
        }
    }

    private static class ChangeShowWhitespacesAction extends TextAction {

        private static final long serialVersionUID = 1658351146973083837L;

        public ChangeShowWhitespacesAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setWhitespaceVisible(!httpPanelTextArea.isWhitespaceVisible());
            }
        }
    }

    private static class ChangeShowNewlinesAction extends TextAction {

        private static final long serialVersionUID = -4769295127472728093L;

        public ChangeShowNewlinesAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setEOLMarkersVisible(!httpPanelTextArea.getEOLMarkersVisible());
            }
        }
    }

    private static class ChangeMarkOccurrencesAction extends TextAction {

        private static final long serialVersionUID = 4264667901630036382L;

        public ChangeMarkOccurrencesAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setMarkOccurrences(!httpPanelTextArea.getMarkOccurrences());
            }
        }
    }

    private static class ChangeRoundedSelectionEdgesAction extends TextAction {

        private static final long serialVersionUID = 6117454234529550001L;

        public ChangeRoundedSelectionEdgesAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setRoundedSelectionEdges(
                        !httpPanelTextArea.getRoundedSelectionEdges());
            }
        }
    }

    private static class ChangeBracketMatchingAction extends TextAction {

        private static final long serialVersionUID = 7913147909802331579L;

        public ChangeBracketMatchingAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setBracketMatchingEnabled(
                        !httpPanelTextArea.isBracketMatchingEnabled());
            }
        }
    }

    private static class ChangeAnimatedBracketMatchingAction extends TextAction {

        private static final long serialVersionUID = -4394901099442863189L;

        public ChangeAnimatedBracketMatchingAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if (textComponent instanceof HttpPanelSyntaxHighlightTextArea) {
                HttpPanelSyntaxHighlightTextArea httpPanelTextArea =
                        (HttpPanelSyntaxHighlightTextArea) textComponent;
                httpPanelTextArea.setAnimateBracketMatching(
                        !httpPanelTextArea.getAnimateBracketMatching());
            }
        }
    }
}
