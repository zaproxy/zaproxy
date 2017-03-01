/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.zaproxy.zap.utils.DisplayUtils;

/**
 *
 * @author yhawke (2014)
 */
public class BackgroundImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage img;

    /**
     * Default constructor 
     */
    public BackgroundImagePanel() {
        img = null;
    }
        
    /**
     * Set the background image
     * @param imageUrl the url of the image
     */
    public BackgroundImagePanel(URL imageUrl) {
        setBackgroundImage(imageUrl);
    }

    /**
     * set the current Background image
     * @param imageUrl the url of the image that need to be set
     */
    public final void setBackgroundImage(URL imageUrl) {
        if (imageUrl != null) {
            try {
                img = ImageIO.read(imageUrl);
                
            } catch (IOException ioe) {
                // do nothing
            }
        }        
    }

    /**
     * Overridden method to paint a background before the rest
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (img != null) {
            setOpaque(false);
            g.drawImage(img, 0, 0, DisplayUtils.getScaledSize(img.getWidth()), DisplayUtils.getScaledSize(img.getHeight()), null);
        }
        
        super.paintComponent(g);
    }    
}
