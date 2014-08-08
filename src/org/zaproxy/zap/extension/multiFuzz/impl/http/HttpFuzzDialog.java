/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.multiFuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.multiFuzz.FileFuzzer;
import org.zaproxy.zap.extension.multiFuzz.FuzzDialog;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcessFactory;
import org.zaproxy.zap.extension.multiFuzz.FuzzerListener;
import org.zaproxy.zap.extension.multiFuzz.Payload;
import org.zaproxy.zap.extension.multiFuzz.PayloadDialog;
import org.zaproxy.zap.extension.multiFuzz.PayloadFactory;
import org.zaproxy.zap.extension.multiFuzz.PayloadScript;
import org.zaproxy.zap.extension.multiFuzz.RegExStringGenerator;
import org.zaproxy.zap.extension.multiFuzz.SubComponent;
import org.zaproxy.zap.extension.multiFuzz.Util;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class HttpFuzzDialog extends
		FuzzDialog<HttpMessage, HttpFuzzLocation, HttpPayload, HttpFuzzGap> {

	private static final long serialVersionUID = -6286527080805168790L;
	private static final Logger logger = Logger
			.getLogger(HttpFuzzDialog.class);
	private HttpFuzzComponent fuzzComponent;

	private JCheckBox followRedirects;
	private JCheckBox urlEncode;

	private HttpPayloadFactory factory;
	private boolean conModification;

	@Override
	public FuzzProcessFactory<HttpFuzzProcess, HttpPayload, HttpFuzzLocation> getFuzzProcessFactory() {
		return new HttpFuzzProcessFactory(fuzzableMessage, getFollowRedirects()
				.isSelected());
	}

	public HttpFuzzDialog(ExtensionFuzz ext, HttpMessage msg, ArrayList<SubComponent> s) {
		super(ext, msg, s);
		this.factory = new HttpPayloadFactory();
	}

	@Override
	protected int addCustomComponents(JPanel panel, int currentRow) {
		panel.add(
				new JLabel(Constant.messages
						.getString("fuzz.label.followredirects")),
				Util.getGBC(0, currentRow, 2, 0.125));
		panel.add(getFollowRedirects(), Util.getGBC(2, currentRow, 4, 0.125));
		currentRow++;

		panel.add(
				new JLabel(Constant.messages.getString("fuzz.label.urlencode")),
				Util.getGBC(0, currentRow, 2, 0.125));
		panel.add(getUrlEncode(), Util.getGBC(2, currentRow, 4, 0.125));
		currentRow++;

		return currentRow;
	}

	private JCheckBox getFollowRedirects() {
		if (followRedirects == null) {
			followRedirects = new JCheckBox();
			followRedirects.setSelected(true);
		}
		return followRedirects;
	}

	private JCheckBox getUrlEncode() {
		if (urlEncode == null) {
			urlEncode = new JCheckBox();
			urlEncode.setSelected(true);
		}
		return urlEncode;
	}

	private ArrayList<HttpPayload> getRecursive(ArrayList<HttpPayload> p, int l) {
		ArrayList<HttpPayload> payloads = new ArrayList<>();
		Stack<HttpPayload> base = new Stack<>();
		base.addAll(p);
		while (!base.empty()) {
			HttpPayload c = base.pop();
			if (c.getData().length() >= l) {
				c.setData(c.getData().substring(0, l));
				payloads.add(c);
			} else {
				for (HttpPayload add : p) {
					base.push(getPayloadFactory().createPayload(
							c.getData() + add.getData()));
				}
			}
		}
		return payloads;
	}

	@Override
	protected HttpFuzzComponent getMessageContent() {
		if (fuzzComponent == null) {
			fuzzComponent = new HttpFuzzComponent(fuzzableMessage);
		}
		return fuzzComponent;
	}

	@Override
	public FileFuzzer<HttpPayload> convertToFileFuzzer(Fuzzer jBroFuzzer) {
		FileFuzzer<HttpPayload> ff = new FileFuzzer<>(
				jBroFuzzer.getName(), factory);
		jBroFuzzer.resetCurrentValue();
		while (jBroFuzzer.hasNext()) {
			ff.getList().add(factory.createPayload(jBroFuzzer.next()));
		}
		return ff;
	}

	@Override
	protected StartFuzzAction getStartFuzzAction() {
		return new HttpStartFuzzAction();
	}

	@Override
	protected AddFuzzAction getAddFuzzAction() {
		return new HttpAddFuzzAction();
	}

	@Override
	protected EditFuzzAction getEditFuzzAction() {
		return new HttpEditFuzzAction();
	}

	private class HttpEditFuzzAction extends EditFuzzAction {
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (!conModification) {
				conModification = true;
				if (targetTable.getSelectedRow() > -1) {
					HttpFuzzGap mod = targetModel.getEntries().get(
							targetTable.getSelectedRow());
					PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> payDialog = new PayloadDialog<>(
							mod, (HttpPayloadFactory) getPayloadFactory(), res);
					payDialog.setModalityType(ModalityType.APPLICATION_MODAL);
					payDialog
							.addListener(new FuzzerListener<PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory>, HttpFuzzGap>() {
								@Override
								public void notifyFuzzerStarted(
										PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> process) {
								}

								@Override
								public void notifyFuzzerPaused(
										PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> process) {
								}

								@Override
								public void notifyFuzzerComplete(
										HttpFuzzGap result) {
									if (result == null
											|| result.getPayloads().isEmpty()) {
										targetModel.removeEntry(result);
										if (targetModel.getRowCount() == 0) {
											getStartButton().setEnabled(false);
										}
										getMessageContent().highlight(
												targetModel.getEntries());
									}
								}
							});
					payDialog.setVisible(true);
				}
				conModification = false;
			}
		}

	}

	private class HttpAddFuzzAction extends AddFuzzAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (isValidLocation(getMessageContent().selection())) {
				PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> payDia = new PayloadDialog<>(
						new HttpFuzzGap(getMessage(), getMessageContent()
								.selection()),
						(HttpPayloadFactory) getPayloadFactory(), res);
				payDia.setModalityType(ModalityType.APPLICATION_MODAL);
				payDia.addListener(new FuzzerListener<PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory>, HttpFuzzGap>() {
					@Override
					public void notifyFuzzerStarted(
							PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> process) {
					}

					@Override
					public void notifyFuzzerPaused(
							PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> process) {
					}

					@Override
					public void notifyFuzzerComplete(HttpFuzzGap result) {
						if (result.getPayloads() != null
								&& result.getPayloads().size() > 0) {
							targetModel.addEntry(result);
							getStartButton().setEnabled(true);
						}
					}
				});
				payDia.setVisible(true);
				super.actionPerformed(e);
			} else {
				JOptionPane.showMessageDialog(null, Constant.messages
						.getString("fuzz.warning.intervalOverlap"));
			}
		}
	}

	private class HttpStartFuzzAction extends StartFuzzAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (HttpFuzzGap g : targetModel.getEntries()) {
				ArrayList<HttpPayload> files = new ArrayList<>();
				ArrayList<HttpPayload> regex = new ArrayList<>();
				ArrayList<HttpPayload> scripts = new ArrayList<>();
				for (HttpPayload p : g.getPayloads()) {
					if (p.getType().equals(Payload.Type.SCRIPT)) {
						scripts.add(p);
					}
				}
				ExtensionScript extension = (ExtensionScript) Control
						.getSingleton().getExtensionLoader()
						.getExtension(ExtensionScript.NAME);
				if (extension != null) {
					for (HttpPayload s : scripts) {
						StringWriter writer = new StringWriter();
						ScriptWrapper wrap = extension.getScript(s.getData());
						try {
							PayloadScript pay = extension.getInterface(wrap,
									PayloadScript.class);
							if (pay != null) {
								pay.insertPayloads(g, getPayloadFactory());
							}
						} catch (Exception ex) {
							writer.append(ex.toString());
							extension.setError(wrap, ex);
							extension.setEnabled(wrap, false);
						}
						g.removePayload(s);
					}
				}
				for (HttpPayload p : g.getPayloads()) {
					if (p.getType().equals(Payload.Type.FILE)) {
						files.add(p);
					}
					if (p.getType().equals(Payload.Type.REGEX)) {
						regex.add(p);
					}
				}
				for (HttpPayload p : files) {
					FileFuzzer<HttpPayload> fileFuzzer;
					String cat = p.getData().split(" --> ")[0];
					String choice = p.getData().split(" --> ")[1];
					try {
						if (isCustomCategory(cat)) {
							fileFuzzer = new FileFuzzer<>(new File(
									Constant.getInstance().FUZZER_CUSTOM_DIR
											+ File.separator + choice),
									getPayloadFactory());
						} else if (isJBroFuzzCategory(cat)) {

							fileFuzzer = convertToFileFuzzer(res
									.getJBroFuzzer(choice));

						} else {
							fileFuzzer = new FileFuzzer<>(
									res.getFuzzFile(cat, choice),
									getPayloadFactory());
						}
						if (p.getRecursive()) {
							int l = g.getLocation().end
									- g.getLocation().begin();
							if (p.getLength() != -1) {
								l = p.getLength();
							}
							g.addPayloads(getRecursive(fileFuzzer.getList(), l));
						} else {
							for (HttpPayload pn : fileFuzzer.getList()) {
								pn.setLength(p.getLength());
								g.addPayload(pn);
							}
						}
						g.removePayload(p);
					} catch (NoSuchFuzzerException e1) {
						logger.debug(e1.getMessage());
					}
				}
				for (HttpPayload p : regex) {
					RegExStringGenerator gen = new RegExStringGenerator();
					for (String exp : gen.regexExpansion(p.data, p.getLength(),
							p.getLimit())) {
						g.addPayload(getPayloadFactory().createPayload(exp));
					}
					g.removePayload(p);
				}
			}
			super.actionPerformed(e);
		}
	}

	@Override
	protected PayloadFactory<HttpPayload> getPayloadFactory() {
		if (factory == null) {
			factory = new HttpPayloadFactory();
		}
		return factory;
	}
}