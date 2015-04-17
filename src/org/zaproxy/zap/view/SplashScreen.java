/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright the ZAP Dev team
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
package org.zaproxy.zap.view;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractFrame;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapTextArea;

public class SplashScreen extends JFrame implements Runnable {

    private static final String TIPS_PREFIX = "tips";
    private static final String TIPS_TIP_PREFIX = TIPS_PREFIX + ".tip.";

    private static final long serialVersionUID = 1L;
    private final static char NEWLINE = '\n';
    private boolean close = false;

    private JScrollPane logScrollPane = null;
    private JScrollPane tipsScrollPane = null;
    private JProgressBar loadProgressBar = null;
    private ZapTextArea logPanel = null;
    private ZapTextArea tipsPanel = null;
    private final Stack<String> stack = new Stack<>();
    private static Thread thread = null;
    private List<Image> icons;

    // Tips and Tricks related variables
    private List<String> tipsAndTricks = null;
    private final Random random = new Random();
    private boolean tipsLoaded = false;
    private double currentPerc;

    /**
     * Main thread for a correct visualization of the panel
     * regarding component updates
     */
    @Override
    public void run() {
        thread = Thread.currentThread();

        setSize(DisplayUtils.getScaledDimension(420, 420));
        setLocationRelativeTo(null);
        setUndecorated(true);
        setTitle(Constant.PROGRAM_NAME);
        setIconImages(loadIconImages());

        BackgroundImagePanel panel = new BackgroundImagePanel();
        panel.setPreferredSize(DisplayUtils.getScaledDimension(420, 420));    //420x560
        panel.setLayout(new GridBagLayout());
        panel.setBackgroundImage(SplashScreen.class.getResource("/resource/zap-splash-background.png"));
        
        Border margin = BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED);
        Border padding = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        panel.setBorder(BorderFactory.createCompoundBorder(margin, padding));

        JLabel lblVersion = new JLabel();
        JLabel lblProgramName = new JLabel();

        lblProgramName.setText(Constant.PROGRAM_NAME);
        lblProgramName.setFont(FontUtils.getFont(Font.BOLD, FontUtils.Size.huge));
        lblProgramName.setVisible(true);
        lblProgramName.setName("lblProgramName");

        lblVersion.setText(Constant.PROGRAM_VERSION);
        lblVersion.setFont(FontUtils.getFont(FontUtils.Size.much_larger));
        lblVersion.setName("lblVersion");

        // ProgramName is at the beginning of the panel (0,0)
        panel.add(lblProgramName, LayoutHelper.getGBC(0, 0, 2, 1));
        // Version is +8 horizontally respect to the other components
        panel.add(lblVersion, LayoutHelper.getGBC(0, 1, 2, 1, new Insets(0, 8, 0, 8)));
        // Progress bar (height 12) is +56 and then +24 
        // vertically respect the other elements (tot + 92)
        panel.add(getLoadingJProgressBar(), LayoutHelper.getGBC(0, 2, 1, 1.0, new Insets(56, 0, 24, 0))); 
        panel.add(Box.createHorizontalGlue(), LayoutHelper.getGBC(1, 2, 1, 1.0));
        // Panels should be with different heights for a good view
        panel.add(getTipsJScrollPane(), LayoutHelper.getGBC(0, 3, 2, 1.0, 1.0));
        panel.add(getLogJScrollPane(), LayoutHelper.getGBC(0, 4, 2, 1.0, 0.5));

        this.add(panel);
        this.pack();
        this.setVisible(true);

        SplashOutputWriter splashOutputWriter = new SplashOutputWriter();
        Logger.getRootLogger().addAppender(splashOutputWriter);

        try {
            // Show INFO and ERROR log messages until the UI is ready
            while (!close) {
                try {
                    if (stack.isEmpty()) {
                        Thread.sleep(100);
                        
                    } else {
                        if (!tipsLoaded && getTipsAndTricks() != null) {
                        	SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
                                    displayRandomTip();
								}});
                        }
                        
                    	SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								while (!stack.isEmpty()) {
									// Need to double check as another thread could have grabved the message
                                    getLogPanel().append(stack.pop());
                                    JScrollBar vertical = getLogJScrollPane().getVerticalScrollBar();
                                    vertical.setValue(vertical.getMaximum());
								}
							}});
                    }
                    
                } catch (InterruptedException e) {
                    // New message to display
                    Thread.interrupted();
                }
            }
        
        } catch (Exception e) {
            // Ignore
        }

        Logger.getRootLogger().removeAppender(splashOutputWriter);
        dispose();
    }

    private List<Image> loadIconImages() {
        if (icons == null) {
            icons = new ArrayList<>(8);
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap16x16.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap32x32.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap48x48.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap64x64.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap128x128.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap256x256.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap512x512.png")));
            icons.add(Toolkit.getDefaultToolkit().getImage(AbstractFrame.class.getResource("/resource/zap1024x1024.png")));
        }
        
        return icons;
    }

    /**
     * Set the completion percentage value
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
        if (currentPerc > 100) {
            currentPerc = 100;

        } else if (currentPerc < 0) {
            currentPerc = 0;
        }

        getLoadingJProgressBar().setValue((int)currentPerc);        
    }
    
    private JProgressBar getLoadingJProgressBar() {
        if (loadProgressBar == null) {
            loadProgressBar = new JProgressBar();
            loadProgressBar.setPreferredSize(DisplayUtils.getScaledDimension(100, 12));
            loadProgressBar.setMinimum(0);
            loadProgressBar.setMaximum(100);
            loadProgressBar.setValue(50);
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

            // Dont use appendMsg as the interupt wont be handled at this stage
            stack.push(Constant.messages.getString("start.splash.start"));
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
        if (this.getTipsAndTricks() == null) {
            // Not loaded yet
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

    /**
     * Close current splash screen
     */
    public void close() {
        close = true;
    }

    /**
     * Append a message to the output window of this splash screen
     * @param msg the message that should be appended
     */
    public void appendMsg(final String msg) {
        stack.push(msg);
        thread.interrupt();
    }

    /* This can apparently cause lockups, probably due to a bug in log4j1
     * Log4j2 might be ok, but upgrading is really painful :/

     public void appendMsg(final String msg) {
     if (EventQueue.isDispatchThread()) {
     getTxtOutput().append(msg);
     JScrollBar vertical = getJScrollPane().getVerticalScrollBar();
     vertical.setValue( vertical.getMaximum() );
     return;
     }
     try {
     EventQueue.invokeAndWait(new Runnable() {
     @Override
     public void run() {
     getTxtOutput().append(msg);
     JScrollBar vertical = getJScrollPane().getVerticalScrollBar();
     vertical.setValue( vertical.getMaximum() );
     }
     });
     } catch (Exception e) {
     // Ignore
     }
     }
     */
    private class SplashOutputWriter extends WriterAppender {

        @Override
        public void append(LoggingEvent event) {
            if (event.getLevel().equals(Level.INFO)) {
                String renderedmessage = event.getRenderedMessage();
                if (renderedmessage != null) {
                    appendMsg(new StringBuilder("INFO: ").append(renderedmessage).append(NEWLINE).toString());
                }
            } else if (event.getLevel().equals(Level.ERROR)) {
                String renderedmessage = event.getRenderedMessage();
                if (renderedmessage != null) {
                    appendMsg(new StringBuilder("ERROR: ").append(renderedmessage).append(NEWLINE).toString());
                }
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
    
}
