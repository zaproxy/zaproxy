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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.apache.commons.configuration.XMLConfiguration;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.extension.httppanel.component.all.request.RequestAllComponent;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestBodyStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestHeaderStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.all.request.HttpRequestAllPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request.HttpRequestBodyPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.components.split.request.HttpRequestHeaderPanelSyntaxHighlightTextView;
import org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.menus.SyntaxMenu;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.FontUtils.FontType;
import org.zaproxy.zap.utils.I18N;

/**
 * Paints the request components the desktop shows, in Split and Combined layouts.
 *
 * <p>Header view models replace CRLF with LF, so a normal GET has no carriage return to mark. A
 * multipart body keeps its CRLF bytes, and those carriage returns must be painted.
 */
class HttpPanelRequestCarriageReturnMarkerGuiTest extends AbstractGuiTest {

    private static final String SCREENSHOT_DIR_PROPERTY = "zap.cr.screenshots";

    private final List<JFrame> frames = new ArrayList<>();

    @BeforeAll
    static void setupMessages() {
        Constant.messages = new I18N(Locale.ENGLISH);
        FontUtils.setDefaultFont(FontType.general, "", 12);
        FontUtils.setDefaultFont(FontType.workPanels, "", 12);
        HttpPanelSyntaxHighlightTextArea.setSyntaxMenu(mock(SyntaxMenu.class));
    }

    @AfterEach
    void disposeFrames() {
        executeInEdt(
                () -> {
                    for (JFrame frame : frames) {
                        frame.dispose();
                    }
                    frames.clear();
                });
    }

    @Test
    void shouldShowNoCarriageReturnsForPlainGetInSplitAndCombinedLayouts()
            throws HttpMalformedHeaderException {
        HttpMessage message = new HttpMessage();
        message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.test\r\n\r\n");
        executeInEdt(
                () -> {
                    List<HttpPanelSyntaxHighlightTextArea> split =
                            showSplit(
                                    "Main Request tab and Manual Request Editor - Split - GET",
                                    message);
                    List<HttpPanelSyntaxHighlightTextArea> combined =
                            showCombined(
                                    "Main Request tab and Manual Request Editor - Combined - GET",
                                    message);

                    assertThat(split, is(not(empty())));
                    assertThat(combined, is(not(empty())));
                    for (HttpPanelSyntaxHighlightTextArea textArea : split) {
                        assertThat(
                                visible(textArea.getText()),
                                textArea.getText().indexOf('\r'),
                                is(-1));
                    }
                    for (HttpPanelSyntaxHighlightTextArea textArea : combined) {
                        assertThat(
                                visible(textArea.getText()),
                                textArea.getText().indexOf('\r'),
                                is(-1));
                    }
                });
        captureFrames();
    }

    @Test
    void shouldPaintCarriageReturnMarkersInMultipartBodyForSplitAndCombinedLayouts()
            throws HttpMalformedHeaderException {
        String body =
                "------bound\r\n"
                        + "Content-Disposition: form-data; name=\"field\"\r\n"
                        + "\r\n"
                        + "hello\r\n"
                        + "------bound--\r\n";
        HttpMessage message = new HttpMessage();
        message.setRequestHeader(
                "POST /upload HTTP/1.1\r\n"
                        + "Host: example.test\r\n"
                        + "Content-Type: multipart/form-data; boundary=----bound\r\n"
                        + "\r\n");
        message.setRequestBody(body);

        executeInEdt(
                () -> {
                    List<HttpPanelSyntaxHighlightTextArea> split =
                            showSplit(
                                    "Main Request tab and Manual Request Editor - Split - multipart",
                                    message);
                    HttpPanelSyntaxHighlightTextArea headerArea = split.get(0);
                    HttpPanelSyntaxHighlightTextArea bodyArea = split.get(1);
                    assertThat(
                            visible(headerArea.getText()),
                            headerArea.getText().indexOf('\r'),
                            is(-1));
                    assertThat(
                            headerArea.getText(),
                            headerArea.getText().contains("multipart/form-data"),
                            is(true));
                    assertThat(bodyArea.getText().indexOf('\r'), is(greaterThan(-1)));
                    assertMarkersPainted(bodyArea, "split-body");
                    saveImage(paint(headerArea), "split-header-textarea");

                    List<HttpPanelSyntaxHighlightTextArea> combined =
                            showCombined(
                                    "Main Request tab and Manual Request Editor - Combined - multipart",
                                    message);
                    HttpPanelSyntaxHighlightTextArea combinedArea = combined.get(0);
                    String combinedText = combinedArea.getText();
                    int separator = combinedText.indexOf("\n\n");
                    assertThat(visible(combinedText), separator, is(greaterThan(0)));
                    assertThat(
                            visible(combinedText.substring(0, separator)),
                            combinedText.substring(0, separator).indexOf('\r'),
                            is(-1));
                    assertThat(
                            combinedText.substring(separator).indexOf('\r'), is(greaterThan(-1)));
                    assertMarkersPainted(combinedArea, "combined");
                });
        captureFrames();
    }

    private List<HttpPanelSyntaxHighlightTextArea> showSplit(String title, HttpMessage message) {
        RequestSplitComponent<HttpMessage> component = new RequestSplitComponent<>();
        component.setEditable(true);
        XMLConfiguration configuration = new XMLConfiguration();
        component.addView(
                new HttpRequestHeaderPanelSyntaxHighlightTextView(
                        new RequestHeaderStringHttpPanelViewModel()),
                RequestSplitComponent.ViewComponent.HEADER,
                configuration);
        component.addView(
                new HttpRequestBodyPanelSyntaxHighlightTextView(
                        new RequestBodyStringHttpPanelViewModel()),
                RequestSplitComponent.ViewComponent.BODY,
                configuration);
        component.setMessage(message);
        JFrame frame = show(title, component.getMainPanel());
        JSplitPane splitPane = findSplitPane(component.getMainPanel());
        if (splitPane != null) {
            splitPane.setDividerLocation(240);
            frame.validate();
        }
        return findTextAreas(component.getMainPanel());
    }

    private List<HttpPanelSyntaxHighlightTextArea> showCombined(String title, HttpMessage message) {
        RequestAllComponent component = new RequestAllComponent();
        component.setEditable(true);
        component.addView(
                new HttpRequestAllPanelSyntaxHighlightTextView(
                        new RequestStringHttpPanelViewModel()),
                null,
                new XMLConfiguration());
        component.setMessage(message);
        show(title, component.getMainPanel());
        return findTextAreas(component.getMainPanel());
    }

    private JFrame show(String title, Component contents) {
        JFrame frame = new JFrame(title);
        frames.add(frame);
        frame.getContentPane().add(contents);
        frame.setSize(980, 680);
        frame.setLocation(20, 20);
        frame.setVisible(true);
        frame.validate();
        return frame;
    }

    private void captureFrames() {
        if (screenshotDirectory() == null) {
            return;
        }
        try {
            Robot robot = new Robot();
            robot.waitForIdle();
            robot.delay(400);
            for (JFrame frame : frames) {
                SwingUtilities.invokeAndWait(
                        () -> {
                            frame.setLocation(30, 30);
                            frame.toFront();
                            frame.requestFocus();
                        });
                robot.waitForIdle();
                robot.delay(300);
                BufferedImage image =
                        robot.createScreenCapture(
                                new Rectangle(
                                        frame.getLocationOnScreen().x,
                                        frame.getLocationOnScreen().y,
                                        frame.getWidth(),
                                        frame.getHeight()));
                saveImage(image, frame.getTitle());
            }
        } catch (Exception e) {
            throw new AssertionError("Could not capture the request editor", e);
        }
    }

    private static void assertMarkersPainted(
            HttpPanelSyntaxHighlightTextArea textArea, String name) {
        BufferedImage image = paint(textArea);
        saveImage(image, name + "-textarea");
        String shown = visible(textArea.getText());
        int carriageReturns = 0;
        for (int i = 0; i < textArea.getText().length(); i++) {
            if (textArea.getText().charAt(i) != '\r') {
                continue;
            }
            carriageReturns++;
            try {
                Rectangle cell = textArea.modelToView2D(i).getBounds();
                int marks = paintedMarkerPixels(image, cell);
                assertThat(
                        name
                                + " CR at "
                                + i
                                + " width="
                                + cell.width
                                + " marks="
                                + marks
                                + " text="
                                + shown
                                + " tokens="
                                + tokens(textArea),
                        marks,
                        is(greaterThan(4)));
                assertThat(cell.width, is(greaterThan(8)));
            } catch (Exception e) {
                throw new AssertionError(name + " could not map CR at " + i + " in " + shown, e);
            }
        }
        assertThat(carriageReturns, is(greaterThan(0)));
    }

    private static String tokens(HttpPanelSyntaxHighlightTextArea textArea) {
        StringBuilder builder = new StringBuilder();
        RSyntaxDocument document = (RSyntaxDocument) textArea.getDocument();
        for (int line = 0; line < textArea.getLineCount(); line++) {
            builder.append("\nL").append(line).append(':');
            Token token = document.getTokenListForLine(line);
            while (token != null && token.isPaintable()) {
                builder.append(" [")
                        .append(token.getType())
                        .append(' ')
                        .append(visible(token.getLexeme()))
                        .append(']');
                token = token.getNextToken();
            }
        }
        return builder.toString();
    }

    private static String visible(String text) {
        return text.replace("\r", "<CR>").replace("\n", "<LF>");
    }

    private static BufferedImage paint(HttpPanelSyntaxHighlightTextArea textArea) {
        BufferedImage image =
                new BufferedImage(
                        Math.max(textArea.getWidth(), 1),
                        Math.max(textArea.getHeight(), 1),
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            textArea.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int paintedMarkerPixels(BufferedImage image, Rectangle cell) {
        int background =
                image.getRGB(Math.min(2, image.getWidth() - 1), Math.min(2, image.getHeight() - 1));
        int x0 = Math.max(0, cell.x + 1);
        int y0 = Math.max(0, cell.y + 1);
        int x1 = Math.min(image.getWidth(), cell.x + cell.width - 1);
        int y1 = Math.min(image.getHeight(), cell.y + cell.height - 1);
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

    private static void saveImage(BufferedImage image, String title) {
        String directory = screenshotDirectory();
        if (directory == null) {
            return;
        }
        try {
            Path dir = Path.of(directory);
            Files.createDirectories(dir);
            String fileName = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + ".png";
            ImageIO.write(image, "png", dir.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new AssertionError("Could not save screenshot " + title, e);
        }
    }

    private static String screenshotDirectory() {
        String directory = System.getProperty(SCREENSHOT_DIR_PROPERTY);
        if (directory == null || directory.isEmpty()) {
            directory = System.getenv("ZAP_CR_SCREENSHOTS");
        }
        if (directory == null || directory.isEmpty()) {
            return null;
        }
        return directory;
    }

    private static JSplitPane findSplitPane(Component component) {
        if (component instanceof JSplitPane) {
            return (JSplitPane) component;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                JSplitPane found = findSplitPane(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static List<HttpPanelSyntaxHighlightTextArea> findTextAreas(Component component) {
        List<HttpPanelSyntaxHighlightTextArea> areas = new ArrayList<>();
        collectTextAreas(component, areas);
        return areas;
    }

    private static void collectTextAreas(
            Component component, List<HttpPanelSyntaxHighlightTextArea> areas) {
        if (component instanceof HttpPanelSyntaxHighlightTextArea) {
            areas.add((HttpPanelSyntaxHighlightTextArea) component);
            return;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                collectTextAreas(child, areas);
            }
        }
    }
}
