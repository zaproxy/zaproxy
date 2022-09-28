/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapTextArea;

@SuppressWarnings("serial")
public class SplashScreen extends JFrame {

    private static final String TIPS_PREFIX = "tips";
    private static final String TIPS_TIP_PREFIX = TIPS_PREFIX + ".tip.";

    private static final Logger LOGGER = LogManager.getLogger(SplashScreen.class);

    private static final long serialVersionUID = 1L;

    private JScrollPane logScrollPane = null;
    private JScrollPane tipsScrollPane = null;
    private JProgressBar loadProgressBar = null;
    private ZapTextArea logPanel = null;
    private ZapTextArea tipsPanel = null;

    // Tips and Tricks related variables
    private List<String> tipsAndTricks = null;
    private final Random random = new Random();
    private boolean tipsLoaded = false;
    private double currentPerc;
    private SplashScreenAppender splashScreenAppender;

    public SplashScreen() {
        super();

        setSize(DisplayUtils.getScaledDimension(430, 430));
        setLocationRelativeTo(null);
        setTitle(Constant.PROGRAM_NAME);
        setIconImages(DisplayUtils.getZapIconImages());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(DisplayUtils.getScaledDimension(430, 430));
        if (!DisplayUtils.isDarkLookAndFeel()) {
            panel.setBackground(Color.decode("#F4FAFF"));
        }

        Border margin = BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED);
        Border padding = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        panel.setBorder(BorderFactory.createCompoundBorder(margin, padding));

        JLabel lblVersion = new JLabel();
        JLabel lblProgramName = new JLabel();

        lblProgramName.setText(Constant.PROGRAM_NAME);
        lblProgramName.setFont(
                FontUtils.getFont(FontUtils.getQuicksandBoldFont(), FontUtils.Size.much_larger));
        lblProgramName.setVisible(true);
        lblProgramName.setName("lblProgramName");

        lblVersion.setText(Constant.PROGRAM_VERSION);
        lblVersion.setFont(
                FontUtils.getFont(FontUtils.getQuicksandBoldFont(), FontUtils.Size.larger));
        lblVersion.setName("lblVersion");

        // ProgramName is at the beginning of the panel (0,0)
        panel.add(
                lblProgramName,
                LayoutHelper.getGBC(0, 0, 1, 1, DisplayUtils.getScaledInsets(40, 30, 0, 1)));
        // Version is +8 horizontally respect to the other components
        panel.add(
                lblVersion,
                LayoutHelper.getGBC(0, 1, 1, 1, DisplayUtils.getScaledInsets(0, 30, 0, 1)));
        // Progress bar (height 12) is +56 and then +24
        // vertically respect the other elements (tot + 92)
        panel.add(
                getLoadingJProgressBar(),
                LayoutHelper.getGBC(
                        0,
                        2,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.HORIZONTAL,
                        DisplayUtils.getScaledInsets(20, 30, 64, 0)));

        panel.add(
                new JLabel(createSplashScreenImage()),
                LayoutHelper.getGBC(1, 0, 1, 3, 0.0, DisplayUtils.getScaledInsets(0, 0, 0, 15)));

        // Panels should be with different heights for a good view
        panel.add(getTipsJScrollPane(), LayoutHelper.getGBC(0, 3, 2, 1.0, 1.0));
        panel.add(getLogJScrollPane(), LayoutHelper.getGBC(0, 4, 2, 1.0, 0.5));

        setContentPane(panel);
        this.pack();

        splashScreenAppender = new SplashScreenAppender(this::appendMsg);
        LoggerContext.getContext()
                .getConfiguration()
                .getRootLogger()
                .addAppender(splashScreenAppender, null, null);

        setVisible(true);
    }

    private static ImageIcon createSplashScreenImage() {
        ImageIcon image =
                new ImageIcon(SplashScreen.class.getResource("/resource/zap-splash-screen.png"));
        int width = DisplayUtils.getScaledSize((int) (image.getIconWidth() * 0.5));
        int height = DisplayUtils.getScaledSize((int) (image.getIconHeight() * 0.5));
        return new ImageIcon(image.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Set the completion percentage value
     *
     * @param percentage the percentage of completion
     */
    public void setLoadingCompletion(double percentage) {
        currentPerc = percentage;
        updateLoadingJProgressBar();
    }

    public void addLoadingCompletion(double value) {
        currentPerc += value;
        updateLoadingJProgressBar();
    }

    private void updateLoadingJProgressBar() {
        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {

                            @Override
                            public void run() {
                                updateLoadingJProgressBar();
                            }
                        });
            } catch (InvocationTargetException e) {
                LOGGER.error("Failed to update progress bar: ", e);
            } catch (InterruptedException ignore) {
            }
            return;
        }

        if (currentPerc > 100) {
            currentPerc = 100;

        } else if (currentPerc < 0) {
            currentPerc = 0;
        }

        getLoadingJProgressBar().setValue((int) currentPerc);
    }

    private JProgressBar getLoadingJProgressBar() {
        if (loadProgressBar == null) {
            loadProgressBar = new JProgressBar();
            loadProgressBar.setPreferredSize(DisplayUtils.getScaledDimension(100, 12));
            loadProgressBar.setMinimum(0);
            loadProgressBar.setMaximum(100);
            loadProgressBar.setValue(50);
            loadProgressBar.setBorder(BorderFactory.createLineBorder(Color.black));
            loadProgressBar.setUI(new CustomProgressBarUI());
            loadProgressBar.setForeground(Color.decode("#4389FF"));
            setLoadingCompletion(0.0D);
        }

        return loadProgressBar;
    }

    private JScrollPane getLogJScrollPane() {
        if (logScrollPane == null) {
            logScrollPane = new JScrollPane();
            logScrollPane.setViewportView(getLogPanel());
        }

        return logScrollPane;
    }

    private JScrollPane getTipsJScrollPane() {
        if (tipsScrollPane == null) {
            tipsScrollPane = new JScrollPane();
            tipsScrollPane.setViewportView(getTipsPanel());
        }

        return tipsScrollPane;
    }

    private ZapTextArea getLogPanel() {
        if (logPanel == null) {
            logPanel = new ZapTextArea();
            logPanel.setEditable(false);
            logPanel.setLineWrap(true);
            logPanel.setName("");

            logPanel.append(Constant.messages.getString("start.splash.start"));
        }

        return logPanel;
    }

    private ZapTextArea getTipsPanel() {
        if (tipsPanel == null) {
            tipsPanel = new ZapTextArea();
            tipsPanel.setEditable(false);
            tipsPanel.setLineWrap(true);
            tipsPanel.setWrapStyleWord(true);
            tipsPanel.setName("");

            tipsPanel.append(Constant.messages.getString("start.splash.tips.loading"));

            displayRandomTip();
        }
        return tipsPanel;
    }

    private void displayRandomTip() {
        if (tipsLoaded || this.getTipsAndTricks() == null) {
            // Already shown or not loaded yet
            return;
        }

        if (this.getTipsAndTricks().isEmpty()) {
            // No tips :(
            this.getTipsPanel().setText(Constant.messages.getString("start.splash.tips.none"));
            this.tipsLoaded = true;
            return;
        }

        this.getTipsPanel().setText(Constant.messages.getString("start.splash.tips.title"));
        this.getTipsPanel().append(this.getRandomTip());
        this.tipsLoaded = true;
    }

    /** Close current splash screen */
    public void close() {
        splashScreenAppender.stop();
        LoggerContext.getContext()
                .getConfiguration()
                .getRootLogger()
                .removeAppender(splashScreenAppender.getName());
        dispose();
    }

    /**
     * Append a message to the output window of this splash screen
     *
     * @param msg the message that should be appended
     */
    public void appendMsg(final String msg) {
        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {

                            @Override
                            public void run() {
                                appendMsg(msg);
                            }
                        });
            } catch (InvocationTargetException e) {
                LOGGER.error("Failed to append message: ", e);
            } catch (InterruptedException ignore) {
            }
            return;
        }

        displayRandomTip();
        getLogPanel().append(msg);
        JScrollBar vertical = getLogJScrollPane().getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    static class SplashScreenAppender extends AbstractAppender {

        private static final Property[] NO_PROPERTIES = {};

        private final Consumer<String> logConsumer;

        SplashScreenAppender(Consumer<String> logConsumer) {
            super(
                    "ZAP-SplashScreenAppender",
                    new LevelFilter(),
                    PatternLayout.newBuilder().withPattern("%p: %m%n").build(),
                    true,
                    NO_PROPERTIES);
            this.logConsumer = logConsumer;
            start();
        }

        @Override
        public void append(LogEvent event) {
            logConsumer.accept(((StringLayout) getLayout()).toSerializable(event));
        }

        private static class LevelFilter extends AbstractFilter {

            @Override
            public Filter.Result filter(LogEvent event) {
                Level level = event.getLevel();
                return level == Level.INFO || level == Level.ERROR ? Result.ACCEPT : Result.DENY;
            }
        }
    }

    private List<String> getTipsAndTricks() {
        if (tipsAndTricks == null) {
            // Need to load them in
            ResourceBundle rb = Constant.messages.getMessageBundle(TIPS_PREFIX);
            if (rb == null) {
                return null;
            }

            tipsAndTricks = new ArrayList<>();
            Enumeration<String> enm = rb.getKeys();
            while (enm.hasMoreElements()) {
                String key = enm.nextElement();
                if (key.startsWith(TIPS_TIP_PREFIX)) {
                    tipsAndTricks.add(rb.getString(key));
                }
            }
        }

        return this.tipsAndTricks;
    }

    private String getRandomTip() {
        return this.getTipsAndTricks().get(random.nextInt(this.getTipsAndTricks().size()));
    }

    public class CustomProgressBarUI extends BasicProgressBarUI {
        // Based on
        // www.java2s.com/Tutorials/Java/Swing_How_to/JProgressBar/Change_Progress_Bar_color_using_BasicProgressBarUI.htm

        public CustomProgressBarUI() {
            super();
        }

        @Override
        public void paintDeterminate(Graphics g, JComponent c) {
            if (!(g instanceof Graphics2D)) {
                return;
            }
            Insets b = progressBar.getInsets(); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
                g.setColor(progressBar.getForeground());
                g.fillRect(b.left, b.top, amountFull, barRectHeight);

            } else { // VERTICAL

            }
            if (progressBar.isStringPainted()) {
                paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
            }
        }
    }
}
