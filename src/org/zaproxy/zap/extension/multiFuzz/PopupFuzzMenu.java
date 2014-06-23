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
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.httppanel.Message;

public class PopupFuzzMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private Component lastInvoker = null;
	private JFrame parentFrame = null;
	private ExtensionFuzz ext;

	/**
	 * @return Returns the lastInvoker.
	 */
	public Component getLastInvoker() {
		return lastInvoker;
	}

	/**
	 * This method initializes
	 * 
	 */
	public PopupFuzzMenu(ExtensionFuzz extension) {
		super();
		initialize();
		this.ext = extension;
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setText(Constant.messages.getString("fuzz.tools.menu.multiFuzz"));
	}

	@Override
	public boolean isEnableForComponent(Component invoker) {
		boolean visible = false;

		if (invoker instanceof FuzzableComponent) {
			visible = true;

			FuzzableComponent<?> fuzzableComponent = (FuzzableComponent<?>) invoker;
			if (!fuzzableComponent.canFuzz()
					|| !ext.canFuzz(fuzzableComponent.getMessageClass())) {
				this.setEnabled(false);
			} else {
				this.setEnabled(true);
			}
			if (Control.getSingleton().getMode().equals(Mode.protect)) {
				// In protected mode, so disable if not in scope
				Message aMessage = fuzzableComponent.getFuzzableMessage();

				if (!aMessage.isInScope()) {
					this.setEnabled(false);
				}
			}

			setLastInvoker(invoker);
			Container c = getLastInvoker().getParent();
			while (!(c instanceof JFrame)) {
				c = c.getParent();
			}
			setParentFrame((JFrame) c);
		} else if (invoker instanceof org.zaproxy.zap.extension.fuzz.FuzzableComponent) {
			visible = true;

			org.zaproxy.zap.extension.fuzz.FuzzableComponent fuzzableComponent = (org.zaproxy.zap.extension.fuzz.FuzzableComponent) invoker;

			if (!fuzzableComponent.canFuzz()
					|| !ext.canFuzz(fuzzableComponent.getMessageClass())) {
				this.setEnabled(false);
			} else {
				this.setEnabled(true);
			}
			if (Control.getSingleton().getMode().equals(Mode.protect)) {
				// In protected mode, so disable if not in scope
				Message aMessage = fuzzableComponent.getFuzzableMessage()
						.getMessage();

				if (!aMessage.isInScope()) {
					this.setEnabled(false);
				}
			}

			setLastInvoker(invoker);
			Container c = getLastInvoker().getParent();
			while (!(c instanceof JFrame)) {
				c = c.getParent();
			}
			setParentFrame((JFrame) c);
		} else {
			// Its not fuzzable
			setLastInvoker(null);
		}
		return visible;
	}

	/**
	 * @return Returns the parentFrame.
	 */
	public JFrame getParentFrame() {
		return parentFrame;
	}

	/**
	 * @param parentFrame
	 *            The parentFrame to set.
	 */
	public void setParentFrame(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}

	/**
	 * @param lastInvoker
	 *            The lastInvoker to set.
	 */
	public void setLastInvoker(Component lastInvoker) {
		this.lastInvoker = lastInvoker;
	}

}
