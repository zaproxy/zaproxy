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
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.extension.httppanel.view.AbstractStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.response.ResponseBodyStringHttpPanelViewModel;
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
    void shouldSaveConvertedBodyBytesThroughBothRequestModels() {
        executeInEdt(
                () -> {
                    for (var model :
                            List.of(
                                    new RequestBodyStringHttpPanelViewModel(),
                                    new RequestStringHttpPanelViewModel())) {
                        var message = new HttpMessage();
                        model.setMessage(message);
                        var view = new TestRequestView(model);
                        String body =
                                "--boundary\r\nContent-Type: text/plain\n\r\nexample\r\n--boundary--\r\n";
                        String header =
                                model instanceof RequestStringHttpPanelViewModel
                                        ? "POST http://localhost/ HTTP/1.1\nHost: localhost\n\n"
                                        : "";
                        view.setModelData(header + body);
                        view.save();
                        assertThat(
                                message.getRequestBody().getBytes(),
                                is(body.getBytes(StandardCharsets.UTF_8)));
                        view.createConvertBodyToCrlfMenuItem(true).doClick();
                        view.save();
                        assertThat(
                                message.getRequestBody().getBytes(),
                                is(
                                        body.replace("plain\n", "plain\r\n")
                                                .getBytes(StandardCharsets.UTF_8)));
                    }
                });
    }

    @Test
    void shouldConvertOnlyBodyAndUndoInOneStep() {
        executeInEdt(
                () -> {
                    var view = new TestRequestView(new RequestStringHttpPanelViewModel());
                    String header = "POST / HTTP/1.1\nHost: localhost\n\n";
                    String original = header + "first\r\nsecond\nthird\rfourth\n";
                    view.setModelData(original);
                    view.getHttpPanelTextArea().discardAllEdits();
                    JMenuItem action = view.createConvertBodyToCrlfMenuItem(true);
                    assertThat(action.isEnabled(), is(true));
                    action.doClick();
                    String expected = header + "first\r\nsecond\r\nthird\rfourth\r\n";
                    assertThat(view.getHttpPanelTextArea().getText(), is(expected));
                    assertThat(view.createConvertBodyToCrlfMenuItem(true).isEnabled(), is(false));
                    view.getHttpPanelTextArea().undoLastAction();
                    assertThat(view.getHttpPanelTextArea().getText(), is(original));
                    assertThat(view.getHttpPanelTextArea().canUndo(), is(false));
                    view.getHttpPanelTextArea().redoLastAction();
                    assertThat(view.getHttpPanelTextArea().getText(), is(expected));
                });
    }

    @Test
    void shouldConvertBodyViewAndRespectReadOnlyAndIncompleteContent() {
        executeInEdt(
                () -> {
                    var view = new TestRequestView(new RequestBodyStringHttpPanelViewModel());
                    view.setModelData("first\nsecond");
                    assertThat(view.createConvertBodyToCrlfMenuItem(false).isEnabled(), is(false));
                    view.getHttpPanelTextArea().setEditable(false);
                    assertThat(view.createConvertBodyToCrlfMenuItem(true).isEnabled(), is(false));
                    view.getHttpPanelTextArea().setEditable(true);
                    view.createConvertBodyToCrlfMenuItem(true).doClick();
                    assertThat(view.getHttpPanelTextArea().getText(), is("first\r\nsecond"));
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "plain", "first\r\nsecond", "first\rsecond"})
    void shouldNotOfferConversionWhenBodyDoesNotHaveBareLf(String body) {
        executeInEdt(
                () -> {
                    var view = new TestRequestView(new RequestBodyStringHttpPanelViewModel());
                    view.setModelData(body);
                    assertThat(view.createConvertBodyToCrlfMenuItem(true).isEnabled(), is(false));
                });
    }

    @Test
    void shouldNotConvertResponseOrRequestWithoutBodySeparator() {
        executeInEdt(
                () -> {
                    for (var model :
                            List.of(
                                    new ResponseBodyStringHttpPanelViewModel(),
                                    new RequestStringHttpPanelViewModel())) {
                        var view = new TestRequestView(model);
                        view.setModelData("first\nsecond");
                        assertThat(
                                view.createConvertBodyToCrlfMenuItem(true).isEnabled(), is(false));
                    }
                });
    }

    private static class TestRequestView extends HttpPanelSyntaxHighlightTextView {
        TestRequestView(AbstractStringHttpPanelViewModel model) {
            super(model);
            getHttpPanelTextArea().setEditable(true);
        }

        @Override
        protected HttpPanelSyntaxHighlightTextArea createHttpPanelTextArea() {
            return new TestHttpPanelSyntaxHighlightTextArea();
        }
    }

    @Test
    void shouldInsertBareLfWhenEditingCrlfBody() {
        executeInEdt(
                () -> {
                    var textArea = new TestHttpPanelSyntaxHighlightTextArea();
                    textArea.setText("first\r\nsecond");
                    textArea.setCaretPosition(textArea.getText().length());
                    textArea.getActionMap()
                            .get(DefaultEditorKit.insertBreakAction)
                            .actionPerformed(
                                    new ActionEvent(textArea, ActionEvent.ACTION_PERFORMED, ""));
                    assertThat(textArea.getText(), is(equalTo("first\r\nsecond\n")));
                });
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
