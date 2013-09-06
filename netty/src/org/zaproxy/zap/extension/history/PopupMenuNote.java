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
package org.zaproxy.zap.extension.history;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.view.PopupMenuHistoryReference;

public class PopupMenuNote extends PopupMenuHistoryReference {

    private static final long serialVersionUID = -5692544221103745600L;

    private static final Logger logger = Logger.getLogger(PopupMenuNote.class);

    private final ExtensionHistory extension;

    public PopupMenuNote(ExtensionHistory extension) {
        super(Constant.messages.getString("history.note.popup"));

        this.extension = extension;
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker) {
        return (invoker == Invoker.history);
    }

    @Override
    public void performAction(HistoryReference href) throws Exception {
        try {
            extension.showNotesAddDialog(href, href.getHttpMessage().getNote());

        } catch (HttpMalformedHeaderException | SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
