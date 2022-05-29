/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
 *
 * Please note that this file was originally released under the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version
 * by Compass Security AG.
 *
 * As of October 2014 Compass Security AG granted the OWASP ZAP Project
 * permission to redistribute this code under the Apache License, Version 2.0.
 */
package ch.csnc.extension.ui;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractFrame;
import org.zaproxy.zap.utils.ZapTextArea;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class CertificateView extends AbstractFrame {

    private static final long serialVersionUID = -7284926693579230812L;

    /**
     * Creates new form Certificate
     *
     * @param certificate the certificate to view/display.
     */
    public CertificateView(String certificate) {
        setTitle(Constant.messages.getString("view.cert.title"));

        JButton closeButton = new JButton(Constant.messages.getString("view.cert.button.close"));
        closeButton.addActionListener(
                e -> {
                    setVisible(false);
                    dispose();
                });

        ZapTextArea certificateTextArea = new ZapTextArea(certificate);
        certificateTextArea.setEditable(false);

        JScrollPane certificateScrollPane = new JScrollPane(certificateTextArea);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                layout.createParallelGroup(
                                                                GroupLayout.Alignment.TRAILING)
                                                        .addComponent(
                                                                closeButton,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                93,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                certificateScrollPane,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                658,
                                                                Short.MAX_VALUE))
                                        .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(
                                                certificateScrollPane,
                                                GroupLayout.DEFAULT_SIZE,
                                                439,
                                                Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(closeButton)
                                        .addContainerGap()));
        pack();

        setVisible(true);
    }
}
