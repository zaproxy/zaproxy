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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextArea;

public class SplashScreen extends JFrame implements Runnable {
	
	private static final String TIPS_PREFIX = "tips";
	private static final String TIPS_TIP_PREFIX = TIPS_PREFIX + ".tip.";

	private static final long serialVersionUID = 1L;
	private final static char NEWLINE = '\n';
	private boolean close = false;
	
	private JScrollPane logScrollPane = null;
	private JScrollPane tipsScrollPane = null;
	private ZapTextArea logPanel = null;
	private ZapTextArea tipsPanel = null;
	private Stack<String> stack = new Stack<String>();
	private static Thread thread = null;
	private List<Image> icons;

	// Tips and Tricks related variables
    private List<String> tipsAndTricks = null;
    private Random random = new Random();
    private boolean tipsLoaded = false;

	public void run() {
		thread = Thread.currentThread();
		
		setSize(420, 460);
		setLocationRelativeTo(null);
		setUndecorated(true);
		setTitle(Constant.PROGRAM_NAME);
		setIconImages(loadIconImages());
		
		JPanel panel = new JPanel();
		
		panel.setPreferredSize(new Dimension(420, 560));
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		
		JLabel lblVersion = new JLabel();
		JLabel lblProgramName = new JLabel();
		JLabel lblLogo = new JLabel();

		lblProgramName.setText(Constant.PROGRAM_NAME);
		lblProgramName.setFont(new Font("Default", Font.BOLD, 36));
		lblProgramName.setVisible(true);
		lblProgramName.setName("lblProgramName");
		
		lblVersion.setText(Constant.PROGRAM_VERSION);
		lblVersion.setFont(new Font("Default", Font.PLAIN, 24));
		lblVersion.setName("lblVersion");
		lblVersion.setBackground(Color.white);

		lblLogo.setText("");
		lblLogo.setIcon(new ImageIcon(AboutPanel.class.getResource("/resource/zap256x256.png")));
		lblLogo.setName("lblLogo");

		panel.add(new JLabel(""), LayoutHelper.getGBC(0, 0, 1, 1.0D));	// Spacer
		panel.add(lblLogo, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D));
		panel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 1.0D));	// Spacer

		panel.add(lblProgramName,  LayoutHelper.getGBC(1, 1, 3, 1));
		panel.add(lblVersion,  LayoutHelper.getGBC(1, 2, 3, 1));
		panel.add(getTipsJScrollPane(), LayoutHelper.getGBC(0, 3, 3, 1.0, 1.0));
		panel.add(getLogJScrollPane(), LayoutHelper.getGBC(0, 4, 3, 1.0, 0.5));
		
		this.add(panel);

		this.pack();
		setVisible(true);

        Logger.getRootLogger().addAppender(new SplashOutputWriter());
		
		try {
			// Show INFO and ERROR log messages until the UI is ready
			while (! close) {
				try {
					if (stack.isEmpty()) {
						Thread.sleep(100);
					} else {
						EventQueue.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								if (! tipsLoaded && getTipsAndTricks() != null) {
									displayRandomTip();
								}
								while (!stack.isEmpty()) {
									
									getLogPanel().append(stack.pop());
									JScrollBar vertical = getLogJScrollPane().getVerticalScrollBar();
									vertical.setValue( vertical.getMaximum() );
								}
							}
						});
					}
				} catch (InterruptedException e) {
					// New message to display
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		dispose();
	}
	
	private List<Image> loadIconImages() {
		if (icons == null) {
			icons = new ArrayList<>(4);
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap16x16.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap32x32.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap48x48.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap64x64.png")));
		}
		return icons;
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
			logPanel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
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
			tipsPanel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
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
		if (this.getTipsAndTricks().size() == 0) {
			// No tips :(
			this.getTipsPanel().setText(Constant.messages.getString("start.splash.tips.none"));
			this.tipsLoaded = true;
			return;
		}
		this.getTipsPanel().setText(Constant.messages.getString("start.splash.tips.title"));
		this.getTipsPanel().append(this.getRandomTip());
		this.tipsLoaded = true;
	}

	public void close() {
		close = true;
	}
	
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
			tipsAndTricks = new ArrayList<String>();
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
