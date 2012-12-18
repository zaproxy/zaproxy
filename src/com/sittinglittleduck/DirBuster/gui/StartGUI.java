/*
 * StartGUI.java
 *
 * Created on 11 November 2005, 20:32
 *
 * Copyright 2007 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */
package com.sittinglittleduck.DirBuster.gui;

import java.awt.Dimension;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
import com.sittinglittleduck.DirBuster.Config;
import com.sittinglittleduck.DirBuster.ImageCreator;
import com.sittinglittleduck.DirBuster.Manager;

import edu.stanford.ejalbert.BrowserLauncher;

public class StartGUI extends javax.swing.JFrame
{

    Manager manager = null;
    HelpSet hs;
    HelpBroker hb;

    /** Creates new form StartGUI */
    public StartGUI()
    {
        try
        {



            System.out.println("Starting OWASP DirBuster " + Config.version);

            /*
             * Set the look and feel
             */
            Options.setTabIconsEnabled(true);
            Options.setDefaultIconSize(new Dimension(18, 18));

            PlasticXPLookAndFeel.setPlasticTheme(new Silver());

            UIManager.setLookAndFeel(Options.PLASTICXP_NAME);



            initComponents();

            /*
             * Detect if the OS is vista, as we need to rsize the gui, as the buttons are not visable
             */
            String os = System.getProperty("os.name");


            if(os.contains("Vista"))
            {

                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                setBounds((screenSize.width-771)/2, (screenSize.height-590)/2, 771, 590);

            }



            this.setTitle("OWASP DirBuster " + Config.version + " - Web Application Brute Forcing");
            jPanelSetup = new JPanelSetup(this);
            jPanelRunning = new JPanelRunning(this);
            jPanelReport = new JPanelReport(this);
            getContentPane().add(jPanelSetup, java.awt.BorderLayout.CENTER);
            //setIconImage(new ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/images/duck.gif")).getImage());
            setIconImage(ImageCreator.OWASP_IMAGE.getImage());
            manager = Manager.getInstance();
            //set the checkbox for following redirects
            jCheckBoxMenuFollowRedirets.setSelected(Config.followRedirects);
            jCheckBoxMenuDebug.setSelected(Config.debug);
            jCheckBoxMenuParseHTML.setSelected(Config.parseHTML);
            jCheckBoxMenuItemCaseInsensativeMode.setSelected(Config.caseInsensativeMode);

            /*
             * populate the setting with the values from user prefs
             */
            jPanelSetup.jTextFieldFile.setText(manager.getDefaultList());
            jPanelSetup.jTextFieldFileExtention.setText(manager.getDefaultExts());
            jPanelSetup.jSliderThreads.setValue(manager.getDefaultNoThreads());
            jPanelSetup.jLabelThreadsDisplay.setText(manager.getDefaultNoThreads() + " Threads");

            /*
             * load the help
             */
            loadHelp();

            /*
             * check for an update
             */
            checkForUpdate();



        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex)
        {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelStatus = new javax.swing.JPanel();
        jLabelStatus = new javax.swing.JLabel();
        jLabelCurrentWork = new javax.swing.JLabel();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenuFile1 = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jCheckBoxMenuFollowRedirets = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuDebug = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemCaseInsensativeMode = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuParseHTML = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemAdvanced = new javax.swing.JMenuItem();
        jMenuAbout1 = new javax.swing.JMenu();
        jMenuItemLicence = new javax.swing.JMenuItem();
        jMenuItemVersion = new javax.swing.JMenuItem();
        jMenuHelp1 = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jMenuItemFAQ = new javax.swing.JMenuItem();
        jMenuItemhome = new javax.swing.JMenuItem();
        jMenuItemCheck = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemReport = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Arial", 0, 12));

        jPanelStatus.setLayout(new java.awt.GridBagLayout());

        jLabelStatus.setText("Please complete the test details");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanelStatus.add(jLabelStatus, gridBagConstraints);

        jLabelCurrentWork.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        jPanelStatus.add(jLabelCurrentWork, gridBagConstraints);

        getContentPane().add(jPanelStatus, java.awt.BorderLayout.SOUTH);

        jMenuBar2.setFont(new java.awt.Font("Arial", 1, 12));

        jMenuFile1.setText("File");

        jMenuItemNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/window-new.png"))); // NOI18N
        jMenuItemNew.setText("New");
        jMenuItemNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewActionPerformed(evt);
            }
        });
        jMenuFile1.add(jMenuItemNew);

        jMenuItemExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/door_out.png"))); // NOI18N
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile1.add(jMenuItemExit);

        jMenuBar2.add(jMenuFile1);

        jMenuOptions.setText("Options");

        jCheckBoxMenuFollowRedirets.setText("Follow Redirects");
        jCheckBoxMenuFollowRedirets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuFollowRediretsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jCheckBoxMenuFollowRedirets);

        jCheckBoxMenuDebug.setText("Debug Mode");
        jCheckBoxMenuDebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuDebugActionPerformed(evt);
            }
        });
        jMenuOptions.add(jCheckBoxMenuDebug);

        jCheckBoxMenuItemCaseInsensativeMode.setText("Case Insensative Mode");
        jCheckBoxMenuItemCaseInsensativeMode.setEnabled(false);
        jCheckBoxMenuItemCaseInsensativeMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemCaseInsensativeModeActionPerformed(evt);
            }
        });
        jMenuOptions.add(jCheckBoxMenuItemCaseInsensativeMode);

        jCheckBoxMenuParseHTML.setText("Parse HTML");
        jCheckBoxMenuParseHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuParseHTMLActionPerformed(evt);
            }
        });
        jMenuOptions.add(jCheckBoxMenuParseHTML);
        jMenuOptions.add(jSeparator2);

        jMenuItemAdvanced.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/preferences-system.png"))); // NOI18N
        jMenuItemAdvanced.setText("Advanced Options");
        jMenuItemAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAdvancedActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemAdvanced);

        jMenuBar2.add(jMenuOptions);

        jMenuAbout1.setText("About");

        jMenuItemLicence.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/information.png"))); // NOI18N
        jMenuItemLicence.setText("Licence");
        jMenuItemLicence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLicenceActionPerformed(evt);
            }
        });
        jMenuAbout1.add(jMenuItemLicence);

        jMenuItemVersion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/information.png"))); // NOI18N
        jMenuItemVersion.setText("Version");
        jMenuItemVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemVersionActionPerformed(evt);
            }
        });
        jMenuAbout1.add(jMenuItemVersion);

        jMenuBar2.add(jMenuAbout1);

        jMenuHelp1.setText("Help");

        jMenuItemHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/help.png"))); // NOI18N
        jMenuItemHelp.setText("Help");
        jMenuItemHelp.setEnabled(false);
        jMenuHelp1.add(jMenuItemHelp);

        jMenuItemFAQ.setText("FAQ");
        jMenuItemFAQ.setEnabled(false);
        jMenuHelp1.add(jMenuItemFAQ);

        jMenuItemhome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/internet-web-browser.png"))); // NOI18N
        jMenuItemhome.setText("Home Page");
        jMenuItemhome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemhomeActionPerformed(evt);
            }
        });
        jMenuHelp1.add(jMenuItemhome);

        jMenuItemCheck.setText("Check for updates");
        jMenuItemCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCheckActionPerformed(evt);
            }
        });
        jMenuHelp1.add(jMenuItemCheck);
        jMenuHelp1.add(jSeparator1);

        jMenuItemReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/bug.png"))); // NOI18N
        jMenuItemReport.setText("Report a bug");
        jMenuItemReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReportActionPerformed(evt);
            }
        });
        jMenuHelp1.add(jMenuItemReport);

        jMenuBar2.add(jMenuHelp1);

        setJMenuBar(jMenuBar2);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-771)/2, (screenSize.height-547)/2, 771, 547);
    }// </editor-fold>//GEN-END:initComponents
    private void jCheckBoxMenuParseHTMLActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuParseHTMLActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuParseHTMLActionPerformed
        Config.parseHTML = jCheckBoxMenuParseHTML.isSelected();
    }//GEN-LAST:event_jCheckBoxMenuParseHTMLActionPerformed

    private void jMenuItemReportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemReportActionPerformed
    {//GEN-HEADEREND:event_jMenuItemReportActionPerformed
        try
        {
            BrowserLauncher launcher = new BrowserLauncher(null);
            launcher.openURLinBrowser("https://sourceforge.net/tracker/?func=add&group_id=199126&atid=968238");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItemReportActionPerformed

    private void jMenuItemLicenceActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLicenceActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLicenceActionPerformed
        new JDialogViewLicence(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItemLicenceActionPerformed

    private void jMenuItemAdvancedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemAdvancedActionPerformed
    {//GEN-HEADEREND:event_jMenuItemAdvancedActionPerformed
        new JDialogAdvSetup(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItemAdvancedActionPerformed

    private void jCheckBoxMenuDebugActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuDebugActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuDebugActionPerformed
        if (jCheckBoxMenuDebug.isSelected())
        {
            Config.debug = true;
        }
        else
        {
            Config.debug = false;
        }
    }//GEN-LAST:event_jCheckBoxMenuDebugActionPerformed

    private void jCheckBoxMenuFollowRediretsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuFollowRediretsActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuFollowRediretsActionPerformed
        if (jCheckBoxMenuFollowRedirets.isSelected())
        {
            Config.followRedirects = true;
        }
        else
        {
            Config.followRedirects = false;
        }
    }//GEN-LAST:event_jCheckBoxMenuFollowRediretsActionPerformed

    private void jMenuItemCheckActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCheckActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCheckActionPerformed
        manager.checkForUpdates(true);
    }//GEN-LAST:event_jMenuItemCheckActionPerformed

    private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemNewActionPerformed
    {//GEN-HEADEREND:event_jMenuItemNewActionPerformed
        int n = JOptionPane.showConfirmDialog(
                this,
                "Ae you sure you wish to cancel the current test and start a new one?",
                "Are you sure?",
                JOptionPane.YES_NO_OPTION);
        //if the anwser is yes
        if (n == 0)
        {
            manager.youAreFinished();
            showSetup();
            jPanelRunning.resultsTableModel.clearData();
        }
    }//GEN-LAST:event_jMenuItemNewActionPerformed

    private void jMenuItemVersionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemVersionActionPerformed
    {//GEN-HEADEREND:event_jMenuItemVersionActionPerformed
        //new JDialogVersion(new javax.swing.JFrame(), true).setVisible(true);
        String versionText = "DirBuster " + Config.version + "\n\n" +
                Config.versionDate + "\n" +
                "Written by: James Fisher\n" +
                "Help supplied by: John Anderson (john@ev6.net)\n" +
                "\n" +
                "Contact: DirBuster@sittinglittleduck.com\n" +
                "Home: http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project";


        JOptionPane.showMessageDialog(this, versionText, "About DirBuster-" + Config.version, 1, ImageCreator.OWASP_IMAGE);
    }//GEN-LAST:event_jMenuItemVersionActionPerformed

    private void jMenuItemhomeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemhomeActionPerformed
    {//GEN-HEADEREND:event_jMenuItemhomeActionPerformed
        try
        {
            BrowserLauncher launcher = new BrowserLauncher(null);
            launcher.openURLinBrowser("http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItemhomeActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jCheckBoxMenuItemCaseInsensativeModeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuItemCaseInsensativeModeActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuItemCaseInsensativeModeActionPerformed
        if (jCheckBoxMenuItemCaseInsensativeMode.isSelected())
        {
            Config.caseInsensativeMode = true;
        }
        else
        {
            Config.caseInsensativeMode = false;
        }
    }//GEN-LAST:event_jCheckBoxMenuItemCaseInsensativeModeActionPerformed

    /*
    Display the help
    JHelp help = new JHelp(hs);
    help.setNavigatorDisplayed(true);
    JFrameHelp helpFrame = new JFrameHelp(help);
    helpFrame.setVisible(true);
     */
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                new StartGUI().setVisible(true);


            }
        });
    }

    public void showSetup()
    {
        getContentPane().remove(jPanelRunning);
        getContentPane().remove(jPanelReport);
        getContentPane().add(jPanelSetup, java.awt.BorderLayout.CENTER);
        this.invalidate();
        this.validate();
        this.repaint();
    }

    public void showRunning()
    {
        getContentPane().remove(jPanelSetup);
        getContentPane().remove(jPanelReport);
        getContentPane().add(jPanelRunning, java.awt.BorderLayout.CENTER);
        this.invalidate();
        this.validate();
        this.repaint();
    }

    public void showReporting()
    {
        getContentPane().remove(jPanelSetup);
        getContentPane().remove(jPanelRunning);
        getContentPane().add(jPanelReport, java.awt.BorderLayout.CENTER);
        this.invalidate();
        this.validate();
        this.repaint();
    }

    public void addResult(ResultsTableObject row)
    {
        jPanelRunning.addResult(row);
    }

    public void updateTable(String finished, String started)
    {
        jPanelRunning.upDateResult(finished, started);
    }

    public void updateProgress(String current, String average, String total, int numberOfThreads, String timeLeft, String parseQueueLength)
    {
        jPanelRunning.setCurrentSpeed(current);
        jPanelRunning.setAverageSpeed(average);
        jPanelRunning.setTotalRequests(total);
        jPanelRunning.jLabelNumThreads.setText("Current number of running threads: " + numberOfThreads);
        jPanelRunning.jLabelTimeLeft.setText(timeLeft);
        jPanelRunning.jLabelParseQueueLength.setText("Parse Queue Size: " + parseQueueLength);

    }

    public void pause()
    {
        if (manager != null)
        {
            manager.pause();
        }
    }

    public void unPause()
    {
        if (manager != null)
        {
            manager.unPause();
        }
    }

    public void setStatus(String status)
    {

        jLabelStatus.setText(status);
    }

    public void startBruteForceFile(String dirToStartWith,
            String fileToRead,
            String protocol,
            String host,
            int port,
            String fileExtention,
            int threadsNumber,
            boolean doDirs,
            boolean doFiles,
            boolean recursive,
            boolean auto,
            boolean useBlankExt,
            Vector extToUse)
    {
        manager.setupManager(dirToStartWith, fileToRead, protocol, host, port, fileExtention, this, threadsNumber, doDirs, doFiles, recursive, useBlankExt, extToUse);
        jPanelRunning.resultsTableModel.setManager(manager);
        manager.setAuto(auto);
        manager.start();
    }

    public void startBruteForcePure(String dirToStartWith, String[] charSet, int minLen, int maxLen, String protocol, String host, int port, String fileExtention, int threadsNumber, boolean doDirs, boolean doFiles, boolean recursive, boolean auto, boolean useBlankExt)
    {
        manager.setupManager(dirToStartWith, charSet, minLen, maxLen, protocol, host, port, fileExtention, this, threadsNumber, doDirs, doFiles, recursive, useBlankExt);
        jPanelRunning.resultsTableModel.setManager(manager);
        manager.setAuto(auto);
        manager.start();
    }

    /*
     * start bruteforce fuzzing
     */
    public void startBruteForceFuzz(String[] charSet, int minLen, int maxLen, String protocol, String host, int port, int threadsNumber, boolean auto, String urlFuzzStart, String urlFuzzEnd)
    {
        manager.setUpManager(charSet, minLen, maxLen, protocol, host, port, this, threadsNumber, urlFuzzStart, urlFuzzEnd);
        jPanelRunning.resultsTableModel.setManager(manager);
        manager.setAuto(auto);
        manager.start();
    }

    /*
    public void setUpManager(String inputFile,
    String protocol,
    String host,
    int port,
    StartGUI gui,
    int ThreadNumber,
    String urlFuzzStart,
    String urlFuzzEnd)
     */
    public void startListBasedFuzz(String inputFile,
            String protocol,
            String host,
            int port,
            int threadNumber,
            String urlFuzzStart,
            String urlFuzzEnd,
            boolean auto)
    {
        manager.setUpManager(inputFile, protocol, host, port, this, threadNumber, urlFuzzStart, urlFuzzEnd);
        jPanelRunning.resultsTableModel.setManager(manager);
        manager.setAuto(auto);
        manager.start();
    }

    public void finished()
    {
        manager.youAreFinished();
    }

    public void enableReport()
    {
        jPanelRunning.enableReport();
    }

    private void loadHelp()
    {
        try
        {
            URL url = this.getClass().getResource("/com/sittinglittleduck/DirBuster/Help/help.hs");
            ClassLoader loader = this.getClass().getClassLoader();
            hs = new HelpSet(loader, url);
            hs.setTitle("DirBuster Help Viewer");
            hb = hs.createHelpBroker("Main_Window");
            jMenuItemHelp.addActionListener(new CSH.DisplayHelpFromSource(hb));
            hb.enableHelpKey(this.getRootPane(), "item1", hs);

            /*
            System.out.println(UIManager.getLookAndFeel().getDescription());
            System.out.println(UIManager.getLookAndFeel().getID());
            System.out.println(UIManager.getLookAndFeel().isNativeLookAndFeel());
            System.out.println(UIManager.getLookAndFeel().isSupportedLookAndFeel());
             */


        }
        catch (HelpSetException ex)
        {
            Logger.getLogger(StartGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * used to check for an update
     */
    private void checkForUpdate()
    {

        /*
         * get the date now
         */
        manager.checkForUpdates(false);
    }

    public void setURL(String URL)
    {
        jPanelSetup.jTextFieldTarget.setText(URL);
    }

    public JPanelSetup jPanelSetup;
    public JPanelRunning jPanelRunning;
    public JPanelReport jPanelReport;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuDebug;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuFollowRedirets;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemCaseInsensativeMode;
    public javax.swing.JCheckBoxMenuItem jCheckBoxMenuParseHTML;
    public javax.swing.JLabel jLabelCurrentWork;
    public javax.swing.JLabel jLabelStatus;
    public javax.swing.JMenu jMenuAbout1;
    public javax.swing.JMenuBar jMenuBar2;
    public javax.swing.JMenu jMenuFile1;
    public javax.swing.JMenu jMenuHelp1;
    public javax.swing.JMenuItem jMenuItemAdvanced;
    public javax.swing.JMenuItem jMenuItemCheck;
    public javax.swing.JMenuItem jMenuItemExit;
    public javax.swing.JMenuItem jMenuItemFAQ;
    public javax.swing.JMenuItem jMenuItemHelp;
    public javax.swing.JMenuItem jMenuItemLicence;
    public javax.swing.JMenuItem jMenuItemNew;
    public javax.swing.JMenuItem jMenuItemReport;
    public javax.swing.JMenuItem jMenuItemVersion;
    public javax.swing.JMenuItem jMenuItemhome;
    public javax.swing.JMenu jMenuOptions;
    public javax.swing.JPanel jPanelStatus;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}
