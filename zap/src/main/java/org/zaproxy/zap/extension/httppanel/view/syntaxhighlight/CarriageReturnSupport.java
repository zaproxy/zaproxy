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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import javax.swing.text.TabExpander;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenPainter;
import org.fife.ui.rsyntaxtextarea.VisibleWhitespaceTokenPainter;
import org.fife.util.SwingUtils;

/**
 * Paints a selectable marker for each carriage return without changing the document text.
 *
 * <p>The marker is always shown. It is not tied to the "show whitespace" option, because a hidden
 * CR is what lets an edit swallow half of a CRLF line ending.
 */
final class CarriageReturnSupport {

    static final char CARRIAGE_RETURN = '\r';

    /** Symbol drawn for a CR. It is not a translated label; the character itself is U+000D. */
    private static final String MARKER = "CR";

    private static final float MARKER_PADDING = 6f;

    private CarriageReturnSupport() {}

    static FontMetrics wrap(FontMetrics delegate) {
        if (delegate == null || delegate instanceof CrFontMetrics) {
            return delegate;
        }
        return new CrFontMetrics(delegate);
    }

    static TokenPainter newTokenPainter() {
        return new CarriageReturnTokenPainter();
    }

    /**
     * Font metrics that give {@code '\\r'} the width of the on-screen marker. Other characters keep
     * the delegate font's widths, so message text does not move except to make room for the marker.
     */
    private static final class CrFontMetrics extends FontMetrics {

        private static final long serialVersionUID = 1L;

        private final FontMetrics delegate;

        CrFontMetrics(FontMetrics delegate) {
            super(new CrFont(delegate));
            this.delegate = delegate;
        }

        @Override
        public FontRenderContext getFontRenderContext() {
            return delegate.getFontRenderContext();
        }

        @Override
        public int charWidth(char ch) {
            return charsWidth(new char[] {ch}, 0, 1);
        }

        @Override
        public int charWidth(int codePoint) {
            char[] chars = Character.toChars(codePoint);
            return charsWidth(chars, 0, chars.length);
        }

        @Override
        public int charsWidth(char[] data, int off, int len) {
            if (len <= 0) {
                return 0;
            }
            return Math.round((float) stringBounds(data, off, off + len).getWidth());
        }

        @Override
        public int getAscent() {
            return delegate.getAscent();
        }

        @Override
        public int getDescent() {
            return delegate.getDescent();
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public int getLeading() {
            return delegate.getLeading();
        }

        @Override
        public int getMaxAscent() {
            return delegate.getMaxAscent();
        }

        @Override
        public int getMaxDescent() {
            return delegate.getMaxDescent();
        }

        private Rectangle2D stringBounds(char[] data, int begin, int limit) {
            return getFont().getStringBounds(data, begin, limit, getFontRenderContext());
        }
    }

    /**
     * Reports the source font's bounds, then adds the marker width for each CR. Measurement of
     * ordinary characters stays on the original font so carets do not drift.
     */
    private static final class CrFont extends Font {

        private static final long serialVersionUID = 1L;

        private final Font source;
        private final float extraPerCarriageReturn;

        CrFont(FontMetrics delegate) {
            super(
                    delegate.getFont().getName(),
                    delegate.getFont().getStyle(),
                    Math.max(delegate.getFont().getSize(), 1));
            this.source = delegate.getFont();
            FontRenderContext context = delegate.getFontRenderContext();
            float natural =
                    (float)
                            source.getStringBounds(new char[] {CARRIAGE_RETURN}, 0, 1, context)
                                    .getWidth();
            float marker =
                    (float) source.getStringBounds(MARKER, context).getWidth() + MARKER_PADDING;
            this.extraPerCarriageReturn = Math.max(0f, marker - natural);
        }

        @Override
        public Rectangle2D getStringBounds(
                char[] chars, int beginIndex, int limit, FontRenderContext frc) {
            Rectangle2D base = source.getStringBounds(chars, beginIndex, limit, frc);
            int carriageReturns = 0;
            for (int i = beginIndex; i < limit; i++) {
                if (chars[i] == CARRIAGE_RETURN) {
                    carriageReturns++;
                }
            }
            if (carriageReturns == 0) {
                return base;
            }
            return new Rectangle2D.Double(
                    base.getX(),
                    base.getY(),
                    base.getWidth() + carriageReturns * extraPerCarriageReturn,
                    base.getHeight());
        }
    }

    /**
     * Paints CR markers. Tokens with no CR are delegated so syntax colours and optional whitespace
     * glyphs stay on RSyntaxTextArea's painters.
     */
    private static final class CarriageReturnTokenPainter implements TokenPainter {

        private final TokenPainter plainPainter = newPlainPainter();
        private final TokenPainter whitespacePainter = new VisibleWhitespaceTokenPainter();
        private final Rectangle2D.Float background = new Rectangle2D.Float();

        private TokenPainter delegate(RSyntaxTextArea host) {
            return host.isWhitespaceVisible() ? whitespacePainter : plainPainter;
        }

        @Override
        public float nextX(
                Token token, int charCount, float x, RSyntaxTextArea host, TabExpander e) {
            if (!containsCarriageReturn(token, charCount)) {
                return delegate(host).nextX(token, charCount, x, host, e);
            }
            return render(token, null, x, 0, host, e, 0, false, false, false, charCount);
        }

        @Override
        public float paint(
                Token token, Graphics2D g, float x, float y, RSyntaxTextArea host, TabExpander e) {
            return paint(token, g, x, y, host, e, 0);
        }

        @Override
        public float paint(
                Token token,
                Graphics2D g,
                float x,
                float y,
                RSyntaxTextArea host,
                TabExpander e,
                float clipStart) {
            if (!containsCarriageReturn(token, token.length())) {
                return delegate(host).paint(token, g, x, y, host, e, clipStart);
            }
            return render(token, g, x, y, host, e, clipStart, false, false, true, token.length());
        }

        @Override
        public float paint(
                Token token,
                Graphics2D g,
                float x,
                float y,
                RSyntaxTextArea host,
                TabExpander e,
                float clipStart,
                boolean paintBackground) {
            if (!containsCarriageReturn(token, token.length())) {
                return delegate(host).paint(token, g, x, y, host, e, clipStart, paintBackground);
            }
            return render(
                    token,
                    g,
                    x,
                    y,
                    host,
                    e,
                    clipStart,
                    !paintBackground,
                    false,
                    true,
                    token.length());
        }

        @Override
        public float paintSelected(
                Token token,
                Graphics2D g,
                float x,
                float y,
                RSyntaxTextArea host,
                TabExpander e,
                boolean useSelectedTextColor) {
            return paintSelected(token, g, x, y, host, e, 0, useSelectedTextColor);
        }

        @Override
        public float paintSelected(
                Token token,
                Graphics2D g,
                float x,
                float y,
                RSyntaxTextArea host,
                TabExpander e,
                float clipStart,
                boolean useSelectedTextColor) {
            if (!containsCarriageReturn(token, token.length())) {
                return delegate(host)
                        .paintSelected(token, g, x, y, host, e, clipStart, useSelectedTextColor);
            }
            return render(
                    token,
                    g,
                    x,
                    y,
                    host,
                    e,
                    clipStart,
                    true,
                    useSelectedTextColor,
                    true,
                    token.length());
        }

        private float render(
                Token token,
                Graphics2D g,
                float x,
                float y,
                RSyntaxTextArea host,
                TabExpander e,
                float clipStart,
                boolean selected,
                boolean useSelectedTextColor,
                boolean paint,
                int charCount) {
            FontMetrics metrics = host.getFontMetricsForTokenType(token.getType());
            if (metrics == null) {
                return x;
            }
            char[] text = token.getTextArray();
            int start = token.getTextOffset();
            int end = start + charCount;
            Color foreground = null;
            Color backgroundColor = null;
            Font font = null;
            boolean showWhitespace = host.isWhitespaceVisible();
            if (paint) {
                foreground =
                        useSelectedTextColor
                                ? host.getSelectedTextColor()
                                : host.getForegroundForToken(token);
                backgroundColor = selected ? null : host.getBackgroundForToken(token);
                Style style = host.getSyntaxScheme().getStyle(token.getType());
                font = style != null && style.font != null ? style.font : host.getFont();
                g.setFont(font);
            }

            int flushIndex = start;
            int flushLength = 0;
            float origin = x;
            for (int i = start; i < end; i++) {
                char ch = text[i];
                if (ch == '\t') {
                    float runEnd =
                            x + SwingUtils.charsWidth(metrics, text, flushIndex, flushLength);
                    float tabEnd = e.nextTabStop(runEnd, 0);
                    if (paint) {
                        fill(g, x, y, tabEnd - x, metrics, backgroundColor);
                        drawRun(
                                g,
                                x,
                                y,
                                text,
                                flushIndex,
                                flushLength,
                                foreground,
                                clipStart,
                                tabEnd);
                        if (showWhitespace) {
                            g.setColor(foreground);
                            paintTab(g, runEnd, y, tabEnd, metrics);
                        }
                    }
                    flushLength = 0;
                    flushIndex = i + 1;
                    x = tabEnd;
                } else if (ch == CARRIAGE_RETURN) {
                    float runWidth = SwingUtils.charsWidth(metrics, text, flushIndex, flushLength);
                    float markerWidth = SwingUtils.charWidth(metrics, CARRIAGE_RETURN);
                    float markerEnd = x + runWidth + markerWidth;
                    if (paint) {
                        fill(g, x, y, markerEnd - x, metrics, backgroundColor);
                        drawRun(
                                g,
                                x,
                                y,
                                text,
                                flushIndex,
                                flushLength,
                                foreground,
                                clipStart,
                                markerEnd);
                        if (markerEnd >= clipStart) {
                            Color marker =
                                    markerColor(
                                            selected
                                                    ? host.getSelectionColor()
                                                    : host.getBackground());
                            g.setColor(marker);
                            paintMarker(g, x + runWidth, y, markerWidth, metrics);
                            g.setFont(font);
                        }
                    }
                    flushLength = 0;
                    flushIndex = i + 1;
                    x = markerEnd;
                } else if (showWhitespace && ch == ' ') {
                    float runEnd =
                            x + SwingUtils.charsWidth(metrics, text, flushIndex, flushLength + 1);
                    if (paint) {
                        fill(g, x, y, runEnd - x, metrics, backgroundColor);
                        drawRun(
                                g,
                                x,
                                y,
                                text,
                                flushIndex,
                                flushLength,
                                foreground,
                                clipStart,
                                runEnd);
                        if (runEnd >= clipStart) {
                            g.setColor(foreground);
                            paintSpace(g, runEnd, y, metrics);
                        }
                    }
                    flushLength = 0;
                    flushIndex = i + 1;
                    x = runEnd;
                } else {
                    flushLength++;
                }
            }

            float runEnd = x + SwingUtils.charsWidth(metrics, text, flushIndex, flushLength);
            if (paint && flushLength > 0) {
                fill(g, x, y, runEnd - x, metrics, backgroundColor);
                drawRun(g, x, y, text, flushIndex, flushLength, foreground, clipStart, runEnd);
            }
            if (paint && host.getUnderlineForToken(token)) {
                g.setColor(foreground);
                SwingUtils.drawLine(g, origin, y + 1, runEnd, y + 1);
            }
            return runEnd;
        }

        private void fill(
                Graphics2D g, float x, float y, float width, FontMetrics metrics, Color color) {
            if (color == null || width <= 0) {
                return;
            }
            background.setRect(x, y - metrics.getAscent(), width, metrics.getHeight());
            g.setColor(color);
            g.fill(background);
        }

        private static void drawRun(
                Graphics2D g,
                float x,
                float y,
                char[] text,
                int index,
                int length,
                Color color,
                float clipStart,
                float runEnd) {
            if (length <= 0 || runEnd < clipStart) {
                return;
            }
            g.setColor(color);
            SwingUtils.drawChars(g, x, y, text, index, length);
        }

        private void paintMarker(Graphics2D g, float x, float y, float width, FontMetrics metrics) {
            float top = y - metrics.getAscent() + 1f;
            float height = Math.max(1f, metrics.getHeight() - 3f);
            background.setRect(x + 0.5f, top, Math.max(1f, width - 1.5f), height);
            g.draw(background);
            float labelWidth =
                    SwingUtils.charsWidth(metrics, MARKER.toCharArray(), 0, MARKER.length());
            g.drawString(MARKER, x + Math.max(0f, (width - labelWidth) / 2f), y);
        }

        private static void paintSpace(Graphics2D g, float x, float y, FontMetrics metrics) {
            float width = SwingUtils.charWidth(metrics, ' ');
            float dotX = x - width / 2f;
            float dotY = y - metrics.getAscent() + metrics.getHeight() / 2f;
            SwingUtils.drawLine(g, dotX, dotY, dotX, dotY);
        }

        private static void paintTab(
                Graphics2D g, float x, float y, float tabEnd, FontMetrics metrics) {
            float halfHeight = metrics.getHeight() / 2f;
            float quarter = halfHeight / 2f;
            float mid = y - metrics.getAscent() + halfHeight;
            SwingUtils.drawLine(g, x, mid, tabEnd, mid);
            SwingUtils.drawLine(g, tabEnd, mid, tabEnd - 4, mid - quarter);
            SwingUtils.drawLine(g, tabEnd, mid, tabEnd - 4, mid + quarter);
        }

        private static Color markerColor(Color background) {
            if (background == null) {
                return new Color(0, 70, 160);
            }
            int luminance =
                    (background.getRed() * 299
                                    + background.getGreen() * 587
                                    + background.getBlue() * 114)
                            / 1000;
            if (luminance > 140) {
                return new Color(0, 70, 160);
            }
            return new Color(150, 205, 255);
        }

        private static boolean containsCarriageReturn(Token token, int charCount) {
            if (token == null || charCount <= 0 || token.getTextArray() == null) {
                return false;
            }
            char[] text = token.getTextArray();
            int start = token.getTextOffset();
            int end = Math.min(text.length, start + charCount);
            for (int i = start; i < end; i++) {
                if (text[i] == CARRIAGE_RETURN) {
                    return true;
                }
            }
            return false;
        }

        private static TokenPainter newPlainPainter() {
            try {
                Constructor<?> constructor =
                        Class.forName("org.fife.ui.rsyntaxtextarea.DefaultTokenPainter")
                                .getDeclaredConstructor();
                constructor.setAccessible(true);
                return (TokenPainter) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not create the plain token painter", e);
            }
        }
    }
}
