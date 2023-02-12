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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

/**
 * Extension enables configuration for Root CA certificate
 *
 * @author MaWoKi
 * @deprecated (2.12.0)
 */
@Deprecated
public class ExtensionDynSSL extends ExtensionAdaptor implements CommandLineListener {

    public static final String EXTENSION_ID = "ExtensionDynSSL";

    private DynSSLParam params;
    private DynamicSSLPanel optionsPanel;

    private CommandLineArgument[] arguments = new CommandLineArgument[3];
    private static final int ARG_CERT_LOAD = 0;
    private static final int ARG_CERT_PUB_DUMP = 1;
    private static final int ARG_CERT_FULL_DUMP = 2;

    private final Logger logger = LogManager.getLogger(ExtensionDynSSL.class);

    public ExtensionDynSSL() {
        super();
        this.setName(EXTENSION_ID);
        this.setOrder(54);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("dynssl.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsPanel());
        }
        extensionHook.addCommandLine(getCommandLineArguments());
        extensionHook.addOptionsParamSet(getParams());
    }

    @Override
    public void start() {
        try {
            startImpl();
        } finally {
            org.parosproxy.paros.network.SSLConnector.setSslCertificateService(
                    org.parosproxy.paros.security.CachedSslCertifificateServiceImpl.getService());
        }
    }

    private void startImpl() {
        final KeyStore rootca = getParams().getRootca();
        if (rootca == null) {
            try {
                createNewRootCa();
            } catch (Exception e) {
                logger.error("Failed to create new root CA certificate:", e);
            }
            return;
        }

        try {
            setRootCa(rootca);
        } catch (final Exception e) {
            logger.error("Couldn't initialize Root CA", e);
        }
        if (isCertExpired(getRootCaCertificate())) {
            warnRootCaCertExpired();
        }
    }

    public void createNewRootCa()
            throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        logger.info("Creating new root CA certificate");
        KeyStore newrootca = SslCertificateUtils.createRootCA();
        setRootCa(newrootca);
        getParams().setRootca(newrootca);
        logger.info("New root CA certificate created");
    }

    private DynamicSSLPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new DynamicSSLPanel(this);
        }
        return optionsPanel;
    }

    public DynSSLParam getParams() {
        if (params == null) {
            params = new DynSSLParam();
        }
        return params;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("dynssl.desc");
    }

    public void setRootCa(KeyStore rootca)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        org.parosproxy.paros.security.CachedSslCertifificateServiceImpl.getService()
                .initializeRootCA(rootca);
    }

    public Certificate getRootCA() throws KeyStoreException {
        if (this.getParams().getRootca() == null) {
            return null;
        }
        return this.getParams()
                .getRootca()
                .getCertificate(
                        org.parosproxy.paros.security.SslCertificateService.ZAPROXY_JKS_ALIAS);
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public boolean supportsLowMemory() {
        return true;
    }

    /**
     * Gets ZAPs current Root CA Certificate in X.509 format. Could return {@code null} if there is
     * a problem getting the certificate.
     *
     * @return The X.509 version of ZAPs current Root CA certificate.
     * @since 2.7.0
     */
    public X509Certificate getRootCaCertificate() {
        try {
            return (X509Certificate) getRootCA();
        } catch (KeyStoreException e) {
            logger.error("Couldn't get ZAP's Root CA Certificate", e);
            return null;
        }
    }

    /**
     * Writes the Root CA public certificate to the specified file in pem format, suitable for
     * importing into browsers
     *
     * @param path the path the Root CA certificate will be written to
     * @throws IOException
     * @throws KeyStoreException
     * @since 2.8.0
     */
    public void writeRootPubCaCertificateToFile(Path path) throws IOException, KeyStoreException {}

    /**
     * Writes the Root CA full certificate to the specified file in pem format, suitable for
     * importing into ZAP
     *
     * @param path the path the Root CA certificate will be written to
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @since 2.8.0
     */
    public void writeRootFullCaCertificateToFile(Path path)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
                    UnrecoverableKeyException {}

    private static void writeCert(String path, CertWriter writer) {
        File file = new File(path);
        if (file.exists() && !file.canWrite()) {
            CommandLine.error(
                    Constant.messages.getString(
                            "dynssl.cmdline.error.nowrite", file.getAbsolutePath()));
        } else {
            try {
                writer.write(file.toPath());
                CommandLine.info(
                        Constant.messages.getString(
                                "dynssl.cmdline.certdump.done", file.getAbsolutePath()));
            } catch (Exception e) {
                CommandLine.error(
                        Constant.messages.getString(
                                "dynssl.cmdline.error.write", file.getAbsolutePath()),
                        e);
            }
        }
    }

    private interface CertWriter {
        void write(Path path) throws Exception;
    }

    /**
     * Imports the root CA certificate from the specified file
     *
     * @param pemFile the pem file containing the certificate
     * @return null on success, otherwise the localised error message
     * @since 2.8.0
     */
    public String importRootCaCertificate(File pemFile) {
        String pem;
        try {
            pem = FileUtils.readFileToString(pemFile, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            return Constant.messages.getString(
                    "dynssl.importpem.failedreadfile", e.getLocalizedMessage());
        }

        byte[] cert;
        try {
            cert = SslCertificateUtils.extractCertificate(pem);
            if (cert.length == 0) {
                return Constant.messages.getString(
                        "dynssl.importpem.nocertsection",
                        SslCertificateUtils.BEGIN_CERTIFICATE_TOKEN,
                        SslCertificateUtils.END_CERTIFICATE_TOKEN);
            }
        } catch (IllegalArgumentException e) {
            return Constant.messages.getString("dynssl.importpem.certnobase64");
        }

        byte[] key;
        try {
            key = SslCertificateUtils.extractPrivateKey(pem);
            if (key.length == 0) {
                return Constant.messages.getString(
                        "dynssl.importpem.noprivkeysection",
                        SslCertificateUtils.BEGIN_PRIVATE_KEY_TOKEN,
                        SslCertificateUtils.END_PRIVATE_KEY_TOKEN);
            }
        } catch (IllegalArgumentException e) {
            return Constant.messages.getString("dynssl.importpem.privkeynobase64");
        }

        try {
            KeyStore ks = SslCertificateUtils.pem2KeyStore(cert, key);
            this.setRootCa(ks);
            this.getParams().setRootca(ks);
            return null;

        } catch (Exception e) {
            return Constant.messages.getString(
                    "dynssl.importpem.failedkeystore", e.getLocalizedMessage());
        }
    }

    /**
     * Returns true if the certificate expired before the current date, otherwise false.
     *
     * @param cert the X.509 certificate for which expiration should be checked.
     * @return true if the certificate has expired, otherwise false.
     */
    private boolean isCertExpired(X509Certificate cert) {
        if (cert != null && cert.getNotAfter().before(new Date())) {
            return true;
        }
        return false;
    }

    /**
     * Displays a warning dialog, and logs a warning message if ZAPs Root CA certificate has
     * expired.
     *
     * @see #isCertExpired(X509Certificate)
     */
    private void warnRootCaCertExpired() {
        X509Certificate cert = getRootCaCertificate();
        if (cert == null) {
            return;
        }
        String warnMsg =
                Constant.messages.getString(
                        "dynssl.warn.cert.expired",
                        cert.getNotAfter().toString(),
                        new Date().toString());
        if (hasView()) {
            if (getView().showConfirmDialog(warnMsg) == JOptionPane.OK_OPTION) {
                try {
                    createNewRootCa();
                    Control.getSingleton()
                            .getMenuToolsControl()
                            .options(Constant.messages.getString("dynssl.options.name"));
                } catch (Exception e) {
                    logger.error("Failed to create new root CA certificate:", e);
                    getView()
                            .showWarningDialog(
                                    Constant.messages.getString(
                                            "dynssl.warn.cert.failed", e.getMessage()));
                }
            }
        }
        logger.warn(warnMsg);
    }

    @Override
    public void execute(CommandLineArgument[] args) {
        if (arguments[ARG_CERT_LOAD].isEnabled()) {
            File file = new File(arguments[ARG_CERT_LOAD].getArguments().firstElement());
            if (!file.canRead()) {
                CommandLine.error(
                        Constant.messages.getString(
                                "dynssl.cmdline.error.noread", file.getAbsolutePath()));
            } else {
                String error = importRootCaCertificate(file);
                if (error == null) {
                    CommandLine.info(
                            Constant.messages.getString(
                                    "dynssl.cmdline.certload.done", file.getAbsolutePath()));
                } else {
                    CommandLine.error(error);
                }
            }
        }
        if (arguments[ARG_CERT_PUB_DUMP].isEnabled()) {
            writeCert(
                    arguments[ARG_CERT_PUB_DUMP].getArguments().firstElement(),
                    this::writeRootPubCaCertificateToFile);
        }
        if (arguments[ARG_CERT_FULL_DUMP].isEnabled()) {
            writeCert(
                    arguments[ARG_CERT_FULL_DUMP].getArguments().firstElement(),
                    this::writeRootFullCaCertificateToFile);
        }
    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_CERT_LOAD] =
                new CommandLineArgument(
                        "-certload",
                        1,
                        null,
                        "",
                        "-certload <path>         "
                                + Constant.messages.getString("dynssl.cmdline.certload"));
        arguments[ARG_CERT_PUB_DUMP] =
                new CommandLineArgument(
                        "-certpubdump",
                        1,
                        null,
                        "",
                        "-certpubdump <path>      "
                                + Constant.messages.getString("dynssl.cmdline.certpubdump"));
        arguments[ARG_CERT_FULL_DUMP] =
                new CommandLineArgument(
                        "-certfulldump",
                        1,
                        null,
                        "",
                        "-certfulldump <path>     "
                                + Constant.messages.getString("dynssl.cmdline.certfulldump"));
        return arguments;
    }

    @Override
    public boolean handleFile(File file) {
        // Not supported
        return false;
    }

    @Override
    public List<String> getHandledExtensions() {
        // Not supported
        return null;
    }
}
