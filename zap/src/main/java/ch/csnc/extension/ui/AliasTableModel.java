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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
@SuppressWarnings("serial")
public class AliasTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -4387633069248206563L;

    private int _ks = -1;
    private List<ch.csnc.extension.httpclient.AliasCertificate> _aliases = new ArrayList<>();
    private ch.csnc.extension.httpclient.SSLContextManager _sslcm;

    public AliasTableModel(ch.csnc.extension.httpclient.SSLContextManager contextManager) {
        _sslcm = contextManager;
    }

    public void setKeystore(int ks) {
        _ks = ks;
        _aliases.clear();
        if (_ks > -1) {
            _aliases = _sslcm.getAliases(_ks);
        }
        fireTableDataChanged();
    }

    public void removeKeystore() {
        _ks = -1;
        _aliases.clear();
        fireTableDataChanged();
    }

    public String getAlias(int row) {
        return _aliases.get(row).getAlias();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return _aliases.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return _aliases.get(row).getName();
    }
}
