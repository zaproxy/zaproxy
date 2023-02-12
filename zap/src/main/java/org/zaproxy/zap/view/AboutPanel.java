/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;

public class AboutPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String PRODUCT = Constant.PROGRAM_NAME;
    private static final String VERSION = "Version " + Constant.PROGRAM_VERSION;
    private static final String YEAR_TOKEN = "<<YEAR>>";
    private static final String COPYRIGHT =
            "Copyright (C) 2010-" + YEAR_TOKEN + " ZAP Development Team";
    private static final String LICENSE_DETAIL =
            "<html><body><p>This program is free software; "
                    + "you can redistribute it and/or modify it under the terms of the Apache License, "
                    + "Version 2.0.  This program is distributed in the hope that it will be useful, but "
                    + "WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS "
                    + "FOR A PARTICULAR PURPOSE.  See the Apache License, Version 2.0 License for more details."
                    + "</p></body></html>";

    private static final String OTHER_LICENSE =
            "<html><body><p>"
                    + PRODUCT
                    + " is a fork of the "
                    + "open source Paros Proxy product developed by Chinotec Technologies Company.</p>"
                    + "<p>The Paros Proxy code is Copyright (C) 2003-2005 Chinotec Technologies Company and is "
                    + "licenced under the Clarified Artistic License as published by the Free Software Foundation.</p>"
                    + "<p>This product includes softwares developed by the Apache Software Foundation "
                    + "<a>http://www.apache.org</a> licensed under Apache License 2.0.  HSQLDB is licensed under BSD "
                    + "license.  JDIC is licensed by Sun Microsystems, Inc under the LGPL license.  "
                    + PRODUCT
                    + " also contains BeanShell, which is licensed under LGPL.  The Copyrights of these softwares "
                    + "belong to their respective owners.</p></body></html>";

    private static final String HOMEPAGE =
            "<html><body><p><a>https://www.zaproxy.org/</a></p></body></html>";

    /** Constructs an {@code AboutPanel}. */
    public AboutPanel() {
        super(new GridBagLayout(), true);

        GridBagConstraints gbcOtherCopyright = new GridBagConstraints();
        GridBagConstraints gbcCopyrightDetail = new GridBagConstraints();
        GridBagConstraints gbcDisclaimer = new GridBagConstraints();
        GridBagConstraints gbcHomepage = new GridBagConstraints();
        GridBagConstraints gbcCopyright = new GridBagConstraints();
        GridBagConstraints gbcVersion = new GridBagConstraints();
        GridBagConstraints gbcProgramName = new GridBagConstraints();
        GridBagConstraints gbcLogo = new GridBagConstraints();

        Color backgroundColor = new Color(UIManager.getColor("TextField.background").getRGB());
        this.setPreferredSize(DisplayUtils.getScaledDimension(420, 460));
        this.setBackground(backgroundColor);
        this.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        JLabel lblDisclaimer = new ZapHtmlLabel();
        JLabel lblCopyright = new JLabel();
        JLabel lblOtherCopyright = new ZapHtmlLabel();
        JLabel lblVersion = new JLabel();
        JLabel lblProgramName = new JLabel();
        JLabel lblCopyrightDetail = new ZapHtmlLabel();
        JLabel lblLogo = new JLabel();
        JLabel lblHomepage = new ZapHtmlLabel();

        lblDisclaimer.setText(
                "<html><body><p>Disclaimer: You should only use this software to test "
                        + "the security of your own web application or those you are authorized to do so.  "
                        + "The authors of this product take no responsibility for any problems in relation to "
                        + "running "
                        + PRODUCT
                        + " against any applications or machines.<p></body></html>");
        // lblDisclaimer.setFont(FontUtils.getFont(FontUtils.Size.smaller));
        lblDisclaimer.setName("lblDisclaimer");
        lblDisclaimer.setBackground(backgroundColor);

        lblLogo.setText("");
        lblLogo.setIcon(new ImageIcon(AboutPanel.class.getResource("/resource/zap64x64.png")));
        lblLogo.setName("lblLogo");

        lblCopyrightDetail.setText(LICENSE_DETAIL);
        // lblCopyrightDetail.setFont(FontUtils.getFont(FontUtils.Size.smaller));
        lblCopyrightDetail.setName("lblCopyrightDetail");
        lblCopyrightDetail.setBackground(backgroundColor);

        lblProgramName.setText(PRODUCT);
        lblProgramName.setFont(FontUtils.getFont(FontUtils.Size.huge));
        lblProgramName.setVisible(true);
        lblProgramName.setName("lblProgramName");

        lblVersion.setText(VERSION);
        lblVersion.setFont(FontUtils.getFont(FontUtils.Size.larger));
        lblVersion.setName("lblVersion");
        lblVersion.setBackground(backgroundColor);

        lblHomepage.setText(HOMEPAGE);

        lblOtherCopyright.setText(OTHER_LICENSE);
        lblOtherCopyright.setFont(FontUtils.getFont(FontUtils.Size.smaller));
        lblOtherCopyright.setName("lblOtherCopyright");
        lblOtherCopyright.setBackground(backgroundColor);

        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        // lblCopyright.setFont(FontUtils.getFont(FontUtils.Size.smaller));
        lblCopyright.setText(COPYRIGHT.replace(YEAR_TOKEN, year));
        lblCopyright.setName("lblCopyright");
        lblCopyright.setBackground(backgroundColor);

        gbcLogo.gridx = 0;
        gbcLogo.gridy = 0;
        gbcLogo.ipadx = 0;
        gbcLogo.ipady = 0;
        gbcLogo.gridheight = 2;
        gbcLogo.anchor = GridBagConstraints.NORTHWEST;
        gbcLogo.insets = new Insets(5, 15, 5, 15);

        gbcProgramName.gridx = 1;
        gbcProgramName.gridy = 0;
        gbcProgramName.ipadx = 0;
        gbcProgramName.ipady = 0;
        gbcProgramName.anchor = GridBagConstraints.NORTHWEST;
        gbcProgramName.insets = new Insets(2, 2, 2, 2);

        gbcVersion.gridx = 1;
        gbcVersion.gridy = 1;
        gbcVersion.ipadx = 0;
        gbcVersion.ipady = 0;
        gbcVersion.anchor = GridBagConstraints.NORTHWEST;
        gbcVersion.insets = new Insets(0, 5, 0, 5);

        gbcCopyright.gridx = 1;
        gbcCopyright.fill = GridBagConstraints.HORIZONTAL;
        gbcCopyright.weightx = 1.0D;
        gbcCopyright.gridy = 3;
        gbcCopyright.ipadx = 0;
        gbcCopyright.ipady = 0;
        gbcCopyright.insets = new Insets(2, 5, 2, 5);
        gbcCopyright.anchor = GridBagConstraints.NORTHWEST;

        gbcHomepage.gridx = 0;
        gbcHomepage.gridy = 4;
        gbcHomepage.ipady = 0;
        gbcHomepage.ipadx = 0;
        gbcHomepage.anchor = GridBagConstraints.NORTHWEST;
        gbcHomepage.fill = GridBagConstraints.HORIZONTAL;
        gbcHomepage.gridwidth = 2;
        gbcHomepage.insets = new Insets(3, 5, 5, 10);
        gbcHomepage.anchor = GridBagConstraints.NORTHWEST;

        gbcDisclaimer.gridx = 0;
        gbcDisclaimer.gridy = 5;
        gbcDisclaimer.ipadx = 0;
        gbcDisclaimer.ipady = 0;
        gbcDisclaimer.anchor = GridBagConstraints.NORTHWEST;
        gbcDisclaimer.fill = GridBagConstraints.HORIZONTAL;
        gbcDisclaimer.insets = new Insets(2, 5, 2, 5);
        gbcDisclaimer.weightx = 1.0D;
        gbcDisclaimer.gridwidth = 2;

        gbcCopyrightDetail.gridx = 0;
        gbcCopyrightDetail.gridy = 6;
        gbcCopyrightDetail.ipadx = 0;
        gbcCopyrightDetail.ipady = 0;
        gbcCopyrightDetail.insets = new Insets(2, 5, 2, 5);
        gbcCopyrightDetail.weightx = 1.0D;
        gbcCopyrightDetail.gridwidth = 2;
        gbcCopyrightDetail.anchor = GridBagConstraints.NORTHWEST;
        gbcCopyrightDetail.fill = GridBagConstraints.HORIZONTAL;

        gbcOtherCopyright.gridx = 0;
        gbcOtherCopyright.gridy = 7;
        gbcOtherCopyright.ipadx = 0;
        gbcOtherCopyright.ipady = 0;
        gbcOtherCopyright.fill = GridBagConstraints.HORIZONTAL;
        gbcOtherCopyright.anchor = GridBagConstraints.NORTHWEST;
        gbcOtherCopyright.gridwidth = 2;
        gbcOtherCopyright.insets = new Insets(2, 5, 2, 5);

        this.add(lblProgramName, gbcProgramName);
        this.add(lblVersion, gbcVersion);
        this.add(lblLogo, gbcLogo);
        this.add(lblCopyright, gbcCopyright);
        this.add(lblHomepage, gbcHomepage);
        this.add(lblDisclaimer, gbcDisclaimer);
        this.add(lblCopyrightDetail, gbcCopyrightDetail);
        this.add(lblOtherCopyright, gbcOtherCopyright);
    }
}
