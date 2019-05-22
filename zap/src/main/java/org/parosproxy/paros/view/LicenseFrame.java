/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

public class LicenseFrame extends AbstractFrame {

	private static final long serialVersionUID = -8463527680384145499L;

	private JPanel jPanel = null;
	private JTextPane txtLicense = null;
	private JPanel jPanel1 = null;
	private JButton btnAccept = null;
	private JButton btnDecline = null;
	private JScrollPane jScrollPane = null;

	private int currentPage = 0;
	private boolean accepted = false;

	private JPanel jPanel2 = null;

    public LicenseFrame() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJPanel());
        this.setPreferredSize(new Dimension(500, 375));
        this.addWindowListener(new java.awt.event.WindowAdapter() {

        	@Override
			public void windowClosing(java.awt.event.WindowEvent e) {

        	    btnDecline.doClick();
        	}
        });

        showLicense(currentPage);
	}
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			final java.awt.GridBagConstraints gridBagConstraints12 = new GridBagConstraints();

			final java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

			final javax.swing.JLabel jLabel = new JLabel();

			final java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.weighty = 1.0D;
			jLabel.setText("<html><body><font size=+1><p>Paros License Agreement - the Clarified Artistic License. </p></font><p></p><p>For the other libraries included in Paros, please refer to respective licenses of the libraries enclosed with this package.</p></body></html>");
			gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.gridwidth = 1;
			gridBagConstraints11.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 2;
			jPanel.add(jLabel, gridBagConstraints11);
			jPanel.add(getJPanel1(), gridBagConstraints1);
			jPanel.add(getJPanel2(), gridBagConstraints12);
		}
		return jPanel;
	}
	/**
	 * This method initializes txtLicense
	 *
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getTxtLicense() {
		if (txtLicense == null) {
			txtLicense = new JTextPane();
			txtLicense.setName("txtLicense");
			txtLicense.setEditable(false);
		}
		return txtLicense;
	}
	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new CardLayout());
			jPanel1.add(getJScrollPane(), getJScrollPane().getName());
		}
		return jPanel1;
	}
	/**
	 * This method initializes btnAccept
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnAccept() {
		if (btnAccept == null) {
			btnAccept = new JButton();
			btnAccept.setText("Accept");
			btnAccept.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    accepted = true;
			        LicenseFrame.this.dispose();

				}
			});

		}
		return btnAccept;
	}
	/**
	 * This method initializes btnDecline
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnDecline() {
		if (btnDecline == null) {
			btnDecline = new JButton();
			btnDecline.setText("Decline");
			btnDecline.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    accepted = false;
				    System.exit(1);

				}
			});

		}
		return btnDecline;
	}
	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtLicense());
			jScrollPane.setName("jScrollPane");
			jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void showLicense(int page) {

	    String localUrl = null;
	    switch (page) {
	//String remoteUrl = "http://www.statistica.unimib.it/utenti/dellavedova/software/artistic2.html";
	    	case 0:
	    	    localUrl = "file:" + System.getProperty("user.dir") + System.getProperty("file.separator") + "license/TheClarifiedArtisticLicense.htm";
	    	    break;
	    }
	    try{
	        txtLicense.setPage(localUrl);
	    } catch (final IOException e){
			e.printStackTrace();
      		JOptionPane.showMessageDialog(new JFrame(), "Error: setting file is missing. Program will exit.");
      		System.exit(0);
    	}
    }

	@Override
	public void setVisible(boolean show) {
	    centerFrame();
	    super.setVisible(show);
	}

	public boolean isAccepted() {
	    return accepted;
	}
	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getBtnAccept(), null);
			jPanel2.add(getBtnDecline(), null);
		}
		return jPanel2;
	}
  }


