/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Class ripped off, er, I mean inspired by https://www.java.net//node/678566
 *
 * @author psiinon
 */
@SuppressWarnings("serial")
public class OverlayIcon extends ImageIcon {
    private static final long serialVersionUID = 1L;
    private ImageIcon base;
    private List<ImageIcon> overlays;

    public OverlayIcon(ImageIcon base) {
        super(base.getImage());
        this.base = base;
        this.overlays = new ArrayList<>();
    }

    public void add(ImageIcon overlay) {
        overlays.add(overlay);
    }

    public OverlayIcon getScaledInstance(int width, int height, int hints) {
        if (base.getIconWidth() >= width && base.getIconHeight() >= height) {
            return this;
        }
        OverlayIcon scaledIcon = new OverlayIcon(getScaledIcon(base, width, height, hints));
        overlays.forEach(o -> scaledIcon.add(getScaledIcon(o, width, height, hints)));
        return scaledIcon;
    }

    private static ImageIcon getScaledIcon(ImageIcon icon, int width, int height, int hints) {
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, hints));
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        base.paintIcon(c, g, x, y);
        for (ImageIcon icon : overlays) {
            icon.paintIcon(c, g, x, y);
        }
    }
}
