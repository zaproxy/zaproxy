/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.saverawmessage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.popup.PopupMenuHttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

class PopupMenuSaveRawMessage extends PopupMenuHttpMessageContainer {

	private static final long serialVersionUID = -7217818541206464572L;
	
	private static final Logger log = Logger.getLogger(PopupMenuSaveRawMessage.class);

	private static final String POPUP_MENU_LABEL = Constant.messages.getString("saveraw.popup.option");
	private static final String POPUP_MENU_ALL = Constant.messages.getString("saveraw.popup.option.all");
	private static final String POPUP_MENU_BODY = Constant.messages.getString("saveraw.popup.option.body");
	private static final String POPUP_MENU_HEADER = Constant.messages.getString("saveraw.popup.option.header");
	private static final String POPUP_MENU_REQUEST = Constant.messages.getString("saveraw.popup.option.request");
	private static final String POPUP_MENU_RESPONSE = Constant.messages.getString("saveraw.popup.option.response");
	
	private static final String FILE_DESCRIPTION = Constant.messages.getString("saveraw.file.description");
	private static final String ERROR_SAVE = Constant.messages.getString("saveraw.file.save.error");
	private static final String CONFIRM_OVERWRITE = Constant.messages.getString("saveraw.file.overwrite.warning");

	private static enum MessageComponent {
		REQUEST,
		REQUEST_HEADER,
		REQUEST_BODY,
		RESPONSE,
		RESPONSE_HEADER,
		RESPONSE_BODY
	};

	public PopupMenuSaveRawMessage() {
		super(POPUP_MENU_LABEL);
		
		setButtonStateOverriddenByChildren(false);

		JMenu request = new SaveMessagePopupMenu(POPUP_MENU_REQUEST, MessageComponent.REQUEST);
		SaveMessagePopupMenuItem requestHeader = new SaveMessagePopupMenuItem(POPUP_MENU_HEADER, MessageComponent.REQUEST_HEADER);

		request.add(requestHeader);
		SaveMessagePopupMenuItem requestBody = new SaveMessagePopupMenuItem(POPUP_MENU_BODY, MessageComponent.REQUEST_BODY);
		request.add(requestBody);
		request.addSeparator();
		SaveMessagePopupMenuItem requestAll = new SaveMessagePopupMenuItem(POPUP_MENU_ALL, MessageComponent.REQUEST);
		request.add(requestAll);
		add(request);
		
		JMenu response = new SaveMessagePopupMenu(POPUP_MENU_RESPONSE, MessageComponent.RESPONSE);
		SaveMessagePopupMenuItem responseHeader = new SaveMessagePopupMenuItem(POPUP_MENU_HEADER, MessageComponent.RESPONSE_HEADER);
		response.add(responseHeader);
		SaveMessagePopupMenuItem responseBody = new SaveMessagePopupMenuItem(POPUP_MENU_BODY, MessageComponent.RESPONSE_BODY);
		response.add(responseBody);
		response.addSeparator();
		SaveMessagePopupMenuItem responseAll = new SaveMessagePopupMenuItem(POPUP_MENU_ALL, MessageComponent.RESPONSE);
		response.add(responseAll);
		add(response);
	}

	@Override
	public boolean precedeWithSeparator() {
		return true;
	}

    private static class SaveMessagePopupMenu extends PopupMenuHttpMessageContainer {

        private static final long serialVersionUID = -6742362073862968150L;

        private final MessageComponent messageComponent;

        public SaveMessagePopupMenu(String label, MessageComponent messageComponent) {
            super(label);

            setButtonStateOverriddenByChildren(false);

            if (!(messageComponent == MessageComponent.REQUEST || messageComponent == MessageComponent.RESPONSE)) {
                throw new IllegalArgumentException("Parameter messageComponent is not supported.");
            }

            this.messageComponent = messageComponent;
        }

        @Override
        protected boolean isButtonEnabledForSelectedHttpMessage(HttpMessage httpMessage) {
            boolean enabled = false;
            if (MessageComponent.REQUEST == messageComponent) {
                enabled = !httpMessage.getRequestHeader().isEmpty();
            } else if (MessageComponent.RESPONSE == messageComponent) {
                enabled = !httpMessage.getResponseHeader().isEmpty();
            }

            return enabled;
        }
    }

    private static class SaveMessagePopupMenuItem extends PopupMenuItemHttpMessageContainer {

        private static final long serialVersionUID = -4108212857830575776L;

        private final MessageComponent messageComponent;

        public SaveMessagePopupMenuItem(String label, MessageComponent messageComponent) {
            super(label);

            this.messageComponent = messageComponent;
        }

        @Override
        public boolean isButtonEnabledForSelectedHttpMessage(HttpMessage httpMessage) {
            boolean enabled = false;
            switch (messageComponent) {
            case REQUEST_HEADER:
                enabled = !httpMessage.getRequestHeader().isEmpty();
                break;
            case REQUEST_BODY:
            case REQUEST:
                enabled = (httpMessage.getRequestBody().length() != 0);
                break;
            case RESPONSE_HEADER:
                enabled = !httpMessage.getResponseHeader().isEmpty();
                break;
            case RESPONSE_BODY:
            case RESPONSE:
                enabled = (httpMessage.getResponseBody().length() != 0);
                break;
            default:
                enabled = false;
            }

            return enabled;
        }

        @Override
        public void performAction(HttpMessage httpMessage) {
            byte[] bytes = new byte[0];

            byte[] bytesHeader;
            byte[] bytesBody;

            switch (messageComponent) {
            case REQUEST_HEADER:
                bytes = httpMessage.getRequestHeader().toString().getBytes();
                break;
            case REQUEST_BODY:
                bytes = httpMessage.getRequestBody().getBytes();
                break;
            case REQUEST:
                bytesHeader = httpMessage.getRequestHeader().toString().getBytes();
                bytesBody = httpMessage.getRequestBody().getBytes();
                bytes = new byte[bytesHeader.length + bytesBody.length];
                System.arraycopy(bytesHeader, 0, bytes, 0, bytesHeader.length);
                System.arraycopy(bytesBody, 0, bytes, bytesHeader.length, bytesBody.length);
                break;
            case RESPONSE_HEADER:
                bytes = httpMessage.getResponseHeader().toString().getBytes();
                break;
            case RESPONSE_BODY:
                bytes = httpMessage.getResponseBody().getBytes();
                break;
            case RESPONSE:
                bytesHeader = httpMessage.getResponseHeader().toString().getBytes();
                bytesBody = httpMessage.getResponseBody().getBytes();
                bytes = new byte[bytesHeader.length + bytesBody.length];
                System.arraycopy(bytesHeader, 0, bytes, 0, bytesHeader.length);
                System.arraycopy(bytesBody, 0, bytes, bytesHeader.length, bytesBody.length);
                break;
            }

            File file = getOutputFile();
            if (file == null) {
                return;
            }

            if (file.exists()) {
                int rc = View.getSingleton().showConfirmDialog(CONFIRM_OVERWRITE);
                if (rc == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }

            writeToFile(file, bytes);
        }

    }

    private static void writeToFile(File file, byte[] bytes) {
        try (OutputStream fw = new BufferedOutputStream(new FileOutputStream(file))) {
            fw.write(bytes);
        } catch (IOException e) {
            View.getSingleton().showWarningDialog(MessageFormat.format(ERROR_SAVE, file.getAbsolutePath()));
            log.error(e.getMessage(), e);
        }
    }

    private static File getOutputFile() {
        JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
        chooser.setFileFilter(new RawFileFilter());
        File file = null;
        int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                return file;
            }
            String fileName = file.getAbsolutePath();
            if (!fileName.endsWith(".raw")) {
                fileName += ".raw";
                file = new File(fileName);
            }
            return file;

        }
        return file;
    }

	private static final class RawFileFilter extends FileFilter {
		
		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			} else if (file.isFile() && file.getName().endsWith(".raw")) {
				return true;
			}
			return false;
		}
		
		@Override
		public String getDescription() {
			return FILE_DESCRIPTION;
		}
	}
}
