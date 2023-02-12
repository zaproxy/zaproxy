/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 mawoki@ymail.com
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
package org.zaproxy.zap.extension.dynssl;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Locale;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

@Deprecated
@SuppressWarnings("serial")
public class DynamicSSLPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private static final int MIN_CERT_LENGTH = 10;

    private static final String OWASP_ZAP_ROOT_CA_NAME = "owasp_zap_root_ca";
    private static final String OWASP_ZAP_ROOT_CA_FILE_EXT = ".cer";
    private static final String OWASP_ZAP_ROOT_CA_FILENAME =
            OWASP_ZAP_ROOT_CA_NAME + OWASP_ZAP_ROOT_CA_FILE_EXT;

    private static final String CONFIGURATION_FILENAME = Constant.FILE_CONFIG_NAME;

    private ZapTextArea txt_PubCert;
    private JButton bt_view;
    private JButton bt_save;

    private KeyStore rootca;
    private ExtensionDynSSL extension;

    private static final Logger logger = LogManager.getLogger(DynamicSSLPanel.class);

    /** Create the panel. */
    public DynamicSSLPanel(ExtensionDynSSL extension) {
        super();
        this.extension = extension;

        setName(Constant.messages.getString("dynssl.options.name"));
        setLayout(new BorderLayout(0, 0));

        final JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));
        add(panel);

        final JLabel lbl_Cert = new JLabel(Constant.messages.getString("dynssl.label.rootca"));

        txt_PubCert = new ZapTextArea();
        txt_PubCert.setFont(FontUtils.getFont("Monospaced"));
        txt_PubCert.setEditable(false);
        txt_PubCert
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                checkAndEnableButtons();
                            }

                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                checkAndEnableButtons();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                checkAndEnableButtons();
                            }

                            private void checkAndEnableButtons() {
                                checkAndEnableViewButton();
                                checkAndEnableSaveButton();
                            }
                        });

        final JScrollPane pubCertScrollPane = new JScrollPane(txt_PubCert);

        final JButton bt_generate =
                new JButton(Constant.messages.getString("dynssl.button.generate"));
        bt_generate.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doGenerate();
                    }
                });
        bt_generate.setIcon(
                new ImageIcon(DynamicSSLPanel.class.getResource("/resource/icon/16/041.png")));

        bt_save = new JButton(Constant.messages.getString("menu.file.save"));
        checkAndEnableSaveButton();
        bt_save.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doSave();
                    }
                });
        bt_save.setIcon(
                new ImageIcon(DynamicSSLPanel.class.getResource("/resource/icon/16/096.png")));

        bt_view = new JButton(Constant.messages.getString("menu.view"));
        checkAndEnableViewButton();
        bt_view.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doView();
                    }
                });
        bt_view.setIcon(
                new ImageIcon(DynamicSSLPanel.class.getResource("/resource/icon/16/049.png")));

        final JButton bt_import = new JButton(Constant.messages.getString("dynssl.button.import"));
        bt_import.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doImport();
                    }
                });
        bt_import.setIcon(
                new ImageIcon(DynamicSSLPanel.class.getResource("/resource/icon/16/047.png")));

        final GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_panel.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_panel.createParallelGroup(
                                                                Alignment.LEADING, false)
                                                        .addGroup(
                                                                gl_panel.createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_panel.createParallelGroup(
                                                                                                Alignment
                                                                                                        .LEADING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                lbl_Cert,
                                                                                                GroupLayout
                                                                                                        .PREFERRED_SIZE,
                                                                                                115,
                                                                                                GroupLayout
                                                                                                        .PREFERRED_SIZE)
                                                                                        .addGroup(
                                                                                                gl_panel.createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement
                                                                                                                        .RELATED)
                                                                                                        .addComponent(
                                                                                                                bt_generate)))
                                                                        .addGap(6))
                                                        .addGroup(
                                                                gl_panel.createSequentialGroup()
                                                                        .addComponent(bt_import)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement
                                                                                        .RELATED)))
                                        .addGroup(
                                                gl_panel.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(
                                                                gl_panel.createSequentialGroup()
                                                                        .addComponent(bt_view)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement
                                                                                        .RELATED)
                                                                        .addComponent(bt_save))
                                                        .addComponent(
                                                                pubCertScrollPane,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                369,
                                                                Short.MAX_VALUE))
                                        .addContainerGap()));
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_panel.createSequentialGroup()
                                        .addGap(10)
                                        .addGroup(
                                                gl_panel.createParallelGroup(Alignment.BASELINE)
                                                        .addGroup(
                                                                gl_panel.createSequentialGroup()
                                                                        .addComponent(lbl_Cert)
                                                                        .addGap(10)
                                                                        .addComponent(
                                                                                bt_generate,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                25,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement
                                                                                        .RELATED)
                                                                        .addComponent(
                                                                                bt_import,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE,
                                                                                25,
                                                                                GroupLayout
                                                                                        .PREFERRED_SIZE))
                                                        .addComponent(
                                                                pubCertScrollPane,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                400,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_panel.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(
                                                                bt_save,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                25,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                bt_view,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                25,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 29, Short.MAX_VALUE)));
        panel.setLayout(gl_panel);
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final DynSSLParam param = options.getParamSet(DynSSLParam.class);
        setRootca(param.getRootca());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final DynSSLParam param = options.getParamSet(DynSSLParam.class);
        param.setRootca(rootca);
        extension.setRootCa(rootca);
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.dynsslcert";
    }

    private void setRootca(KeyStore rootca) {}

    /**
     * Viewing is only allowed, if (a) when java.Desktop#open() works (b) there's a certificate
     * (text) in the text area
     */
    private void checkAndEnableViewButton() {
        boolean enabled = true;
        enabled &= Desktop.isDesktopSupported();
        enabled &= txt_PubCert.getDocument().getLength() > MIN_CERT_LENGTH;
        bt_view.setEnabled(enabled);
    }

    /** Saving is only allowed, if (a) there's a certificate (text) in the text area */
    private void checkAndEnableSaveButton() {
        boolean enabled = true;
        enabled &= txt_PubCert.getDocument().getLength() > MIN_CERT_LENGTH;
        bt_save.setEnabled(enabled);
    }

    /** Import Root CA certificate from other ZAP configuration files. */
    private void doImport() {
        if (checkExistingCertificate()) {
            // prevent overwriting
            return;
        }
        final JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setSelectedFile(new File(CONFIGURATION_FILENAME));
        fc.setFileFilter(
                new FileFilter() {
                    @Override
                    public String getDescription() {
                        // config.xml or *.pem files
                        return Constant.messages.getString("dynssl.filter.file");
                    }

                    @Override
                    public boolean accept(File f) {
                        String lcFileName = f.getName().toLowerCase(Locale.ROOT);
                        return lcFileName.endsWith(CONFIGURATION_FILENAME)
                                || lcFileName.endsWith("pem")
                                || f.isDirectory();
                    }
                });
        final int result = fc.showOpenDialog(this);
        final File f = fc.getSelectedFile();
        if (result == JFileChooser.APPROVE_OPTION && f.exists()) {
            logger.info("Loading Root CA certificate from {}", f);
            KeyStore ks = null;
            if (f.getName().toLowerCase().endsWith("pem")) {
                ks = convertPemFileToKeyStore(f.toPath());
            } else {
                try {
                    final ZapXmlConfiguration conf = new ZapXmlConfiguration(f);
                    final String rootcastr = conf.getString(DynSSLParam.PARAM_ROOT_CA);
                    if (rootcastr == null || rootcastr.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this,
                                Constant.messages.getString("dynssl.message.nocertinconf"),
                                Constant.messages.getString("dynssl.message.nocertinconf.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ks = SslCertificateUtils.string2Keystore(rootcastr);
                } catch (final Exception e) {
                    logger.error("Error importing Root CA cert from config file:", e);
                    JOptionPane.showMessageDialog(
                            this,
                            Constant.messages.getString("dynssl.message1.filecouldntloaded"),
                            Constant.messages.getString("dynssl.message1.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (ks != null) {
                setRootca(ks);
            }
        }
    }

    /**
     * Converts the given {@code .pem} file into a {@link KeyStore}.
     *
     * @param pemFile the {@code .pem} file that contains the certificate and the private key.
     * @return the {@code KeyStore} with the certificate, or {@code null} if the conversion failed.
     */
    private KeyStore convertPemFileToKeyStore(Path pemFile) {
        String pem;
        try {
            pem = FileUtils.readFileToString(pemFile.toFile(), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            logger.warn("Failed to read .pem file:", e);
            JOptionPane.showMessageDialog(
                    this,
                    Constant.messages.getString(
                            "dynssl.importpem.failedreadfile", e.getLocalizedMessage()),
                    Constant.messages.getString("dynssl.importpem.failed.title"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        byte[] cert;
        try {
            cert = SslCertificateUtils.extractCertificate(pem);
            if (cert.length == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        Constant.messages.getString(
                                "dynssl.importpem.nocertsection",
                                SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN,
                                SslCertificateUtils.END_CERTIFICATE_TOKEN),
                        Constant.messages.getString("dynssl.importpem.failed.title"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to base64 decode the certificate from .pem file:", e);
            JOptionPane.showMessageDialog(
                    this,
                    Constant.messages.getString("dynssl.importpem.certnobase64"),
                    Constant.messages.getString("dynssl.importpem.failed.title"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        byte[] key;
        try {
            key = SslCertificateUtils.extractPrivateKey(pem);
            if (key.length == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        Constant.messages.getString(
                                "dynssl.importpem.noprivkeysection",
                                SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN,
                                SslCertificateUtils.END_PRIVATE_KEY_TOKEN),
                        Constant.messages.getString("dynssl.importpem.failed.title"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to base64 decode the private key from .pem file:", e);
            JOptionPane.showMessageDialog(
                    this,
                    Constant.messages.getString("dynssl.importpem.privkeynobase64"),
                    Constant.messages.getString("dynssl.importpem.failed.title"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            return SslCertificateUtils.pem2KeyStore(cert, key);
        } catch (Exception e) {
            logger.error("Error creating KeyStore for Root CA cert from .pem file:", e);
            JOptionPane.showMessageDialog(
                    this,
                    Constant.messages.getString(
                            "dynssl.importpem.failedkeystore", e.getLocalizedMessage()),
                    Constant.messages.getString("dynssl.importpem.failed.title"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Saving Root CA certificate to disk. */
    private void doSave() {
        if (txt_PubCert.getDocument().getLength() < MIN_CERT_LENGTH) {
            logger.error("Illegal state! There seems to be no certificate available.");
            bt_save.setEnabled(false);
        }
        final JFileChooser fc = new WritableFileChooser(new File(System.getProperty("user.home")));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setSelectedFile(new File(OWASP_ZAP_ROOT_CA_FILENAME));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File f = fc.getSelectedFile();
            logger.info("Saving Root CA certificate to {}", f);
            try {
                writePubCertificateToFile(f);
            } catch (final Exception e) {
                logger.error("Error while writing certificate data to file {}", f, e);
            }
        }
    }

    private void writePubCertificateToFile(File file) throws IOException {
        try (BufferedWriter bw =
                Files.newBufferedWriter(file.toPath(), StandardCharsets.US_ASCII)) {
            bw.write(txt_PubCert.getText());
        }
    }

    /** Generates a new Root CA certificate ... */
    private void doGenerate() {
        if (checkExistingCertificate()) {
            // prevent overwriting
            return;
        }
        try {
            final KeyStore newrootca = SslCertificateUtils.createRootCA();
            setRootca(newrootca);
        } catch (final Exception e) {
            logger.error("Error while generating Root CA certificate", e);
        }
    }

    /**
     * Check if certificate already exists. It will ask the user to overwrite.
     *
     * @return True, if certificate exists OR it should be overwritten.
     */
    private boolean checkExistingCertificate() {
        boolean alreadyexists = txt_PubCert.getDocument().getLength() > MIN_CERT_LENGTH;
        if (alreadyexists) {
            final int result =
                    JOptionPane.showConfirmDialog(
                            this,
                            Constant.messages.getString("dynssl.message2.caalreadyexists")
                                    + "\n"
                                    + Constant.messages.getString("dynssl.message2.willreplace")
                                    + "\n\n"
                                    + Constant.messages.getString(
                                            "dynssl.message2.wanttooverwrite"),
                            Constant.messages.getString("dynssl.message2.title"),
                            JOptionPane.YES_NO_OPTION);
            alreadyexists = !(result == JOptionPane.YES_OPTION);
        }
        return alreadyexists;
    }

    /**
     * writes the certificate to a temporary file and tells the OS to open it using
     * java.awrt.Desktop#open()
     */
    private void doView() {
        if (txt_PubCert.getDocument().getLength() < MIN_CERT_LENGTH) {
            logger.error("Illegal state! There seems to be no certificate available.");
            bt_view.setEnabled(false);
        }
        boolean written = false;
        File tmpfile = null;
        try {
            tmpfile = File.createTempFile(OWASP_ZAP_ROOT_CA_NAME, OWASP_ZAP_ROOT_CA_FILE_EXT);
            writePubCertificateToFile(tmpfile);
            written = true;
        } catch (final Exception e) {
            logger.error("Error while writing certificate data into temporary file.", e);
        }
        if (tmpfile != null && written) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(tmpfile);
                } catch (final IOException e) {
                    logger.error("Error while telling the Operating System to open {}", tmpfile, e);
                }
            }
        }
    }
}
