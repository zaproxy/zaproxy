/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.menus.SyntaxMenu;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.FontUtils.FontType;
import org.zaproxy.zap.utils.I18N;

/** GUI test for {@link HttpPanelSyntaxHighlightTextArea}. */
class HttpPanelSyntaxHighlightTextAreaGuiTest extends AbstractGuiTest {

    @BeforeAll
    static void setup() {
        Constant.messages = mock(I18N.class);
        FontUtils.setDefaultFont(FontType.general, "", 12);
        FontUtils.setDefaultFont(FontType.workPanels, "", 12);
        HttpPanelSyntaxHighlightTextArea.setSyntaxMenu(mock(SyntaxMenu.class));
    }

    @Test
    void shouldUndoProgrammaticSetTextAsSingleEdit() {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText("old");
                    textArea.setText("new");
                    // When
                    textArea.undoLastAction();
                    // Then
                    assertThat(textArea.getText(), is(equalTo("old")));
                });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "same")
    void shouldNotCreateUndoEntryWhenSetTextDoesNotChangeText(String text) {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText(text);
                    textArea.setText(text);
                    // When
                    textArea.undoLastAction();
                    // Then
                    assertThat(textArea.getText(), is(equalTo("")));
                    assertThat(textArea.canUndo(), is(equalTo(false)));
                });
    }

    @Test
    void shouldKeepMessageBytesWhenCarriageReturnsAreOnlyDisplayed() {
        executeInEdt(
                () -> {
                    // Given
                    String message = "GET / HTTP/1.1\r\nHost: example.test\r\n\r\nline\nstill\r\n";
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText(message);
                    JFrame frame = show(textArea);
                    try {
                        // When
                        paint(textArea);
                        // Then
                        assertThat(textArea.getText(), is(equalTo(message)));
                    } finally {
                        frame.dispose();
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPaintSelectableCarriageReturnMarker(boolean showWhitespace) {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setWhitespaceVisible(showWhitespace);
                    textArea.setText("alpha\r\nbeta");
                    JFrame frame = show(textArea);
                    try {
                        int crOffset = textArea.getText().indexOf('\r');
                        // When
                        Rectangle2D cell = textArea.modelToView2D(crOffset);
                        BufferedImage image = paint(textArea);
                        float midY = (float) (cell.getY() + cell.getHeight() / 2.0);
                        int leftHit =
                                textArea.viewToModel2D(
                                        new Point2D.Float(
                                                (float) (cell.getX() + cell.getWidth() * 0.25),
                                                midY));
                        int rightHit =
                                textArea.viewToModel2D(
                                        new Point2D.Float(
                                                (float) (cell.getX() + cell.getWidth() * 0.75),
                                                midY));
                        // Then
                        assertThat(cell.getWidth(), is(greaterThan(6.0)));
                        assertThat(cell.getWidth(), is(lessThan(80.0)));
                        assertThat(leftHit, is(equalTo(crOffset)));
                        assertThat(rightHit, is(equalTo(crOffset + 1)));
                        assertThat(paintedMarkerPixels(image, cell), is(greaterThan(4)));
                        assertThat(textArea.getText(), is(equalTo("alpha\r\nbeta")));
                    } finally {
                        frame.dispose();
                    }
                });
    }

    @Test
    void shouldInsertCrlfWhenEnterPressedOnCrlfLine() {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    String message = "name=value\nsecond=line\r\n";
                    textArea.setText(message);
                    textArea.setCaretPosition(message.indexOf("second"));
                    // When
                    pressEnter(textArea);
                    // Then
                    assertThat(textArea.getText(), is(equalTo("name=value\n\r\nsecond=line\r\n")));
                });
    }

    @Test
    void shouldInsertCrlfWhenEnterPressedAfterVisibleCarriageReturn() {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText("alpha\r\nbeta");
                    textArea.setCaretPosition("alpha\r".length());
                    // When
                    pressEnter(textArea);
                    // Then
                    assertThat(textArea.getText(), is(equalTo("alpha\r\n\r\nbeta")));
                    textArea.undoLastAction();
                    assertThat(textArea.getText(), is(equalTo("alpha\r\nbeta")));
                });
    }

    @Test
    void shouldInsertCrlfWhenEnterPressedInMiddleOfCrlfLine() {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText("alpha\r\nbeta\n");
                    textArea.setCaretPosition(2);
                    // When
                    pressEnter(textArea);
                    // Then
                    assertThat(textArea.getText(), is(equalTo("al\r\npha\r\nbeta\n")));
                });
    }

    @Test
    void shouldInsertLfWhenEnterPressedOnLfLine() {
        executeInEdt(
                () -> {
                    // Given
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    String message = "name=value\nsecond=line\r\n";
                    textArea.setText(message);
                    textArea.setCaretPosition("name=value".length());
                    // When
                    pressEnter(textArea);
                    // Then
                    assertThat(textArea.getText(), is(equalTo("name=value\n\nsecond=line\r\n")));
                });
    }

    private static void pressEnter(HttpPanelSyntaxHighlightTextArea textArea) {
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        Object binding = textArea.getInputMap().get(enter);
        assertThat(binding, is(notNullValue()));
        Action action = textArea.getActionMap().get(binding);
        assertThat(action, is(notNullValue()));
        action.actionPerformed(new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, "\n"));
    }

    private static JFrame show(HttpPanelSyntaxHighlightTextArea textArea) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(textArea);
        frame.setSize(640, 240);
        frame.setVisible(true);
        frame.validate();
        textArea.setSize(620, 200);
        textArea.validate();
        return frame;
    }

    private static BufferedImage paint(HttpPanelSyntaxHighlightTextArea textArea) {
        BufferedImage image =
                new BufferedImage(
                        Math.max(textArea.getWidth(), 1),
                        Math.max(textArea.getHeight(), 1),
                        BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        try {
            textArea.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int paintedMarkerPixels(BufferedImage image, Rectangle2D cell) {
        int background = image.getRGB(0, 0);
        int x0 = Math.max(0, (int) Math.floor(cell.getX()) + 1);
        int y0 = Math.max(0, (int) Math.floor(cell.getY()) + 1);
        int x1 = Math.min(image.getWidth(), (int) Math.ceil(cell.getX() + cell.getWidth()) - 1);
        int y1 = Math.min(image.getHeight(), (int) Math.ceil(cell.getY() + cell.getHeight()) - 1);
        int marks = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if (image.getRGB(x, y) != background) {
                    marks++;
                }
            }
        }
        return marks;
    }

    @SuppressWarnings("serial")
    private static class TestHttpPanelSyntaxHighlightTextArea
            extends HttpPanelSyntaxHighlightTextArea {
        @Override
        public void search(Pattern p, List<SearchMatch> matches) {}

        @Override
        public void highlight(SearchMatch sm) {}

        @Override
        protected synchronized CustomTokenMakerFactory getTokenMakerFactory() {
            return new CustomTokenMakerFactory();
        }
    }
}
