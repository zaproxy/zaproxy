/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 OWASP Zed Attack Proxy Project
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

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;

import org.parosproxy.paros.Constant;

public class SplashScreen extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private static final String image = Constant.FILE_PROGRAM_SPLASH;
	
	public void run() {
		setSize(128, 128);
		setLocationRelativeTo(null);
		setUndecorated(true);
		setVisible(true);
		
		try {
			// Show splash for two seconds
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			dispose();
		}
		dispose();
	}

	public void paint(Graphics g) {
		Image sImage = getToolkit().getImage(image);
		g.drawImage(sImage, 0, 0, this);
	}
}
