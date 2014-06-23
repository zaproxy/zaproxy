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
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.multiFuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.multiFuzz.FileFuzzer;
import org.zaproxy.zap.extension.multiFuzz.FuzzDialog;
import org.zaproxy.zap.extension.multiFuzz.FuzzProcessFactory;
import org.zaproxy.zap.extension.multiFuzz.FuzzerListener;
import org.zaproxy.zap.extension.multiFuzz.PayloadDialog;
import org.zaproxy.zap.extension.multiFuzz.PayloadFactory;
import org.zaproxy.zap.extension.multiFuzz.PayloadScript;
import org.zaproxy.zap.extension.multiFuzz.RegExStringGenerator;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

public class HttpFuzzDialog extends
		FuzzDialog<HttpMessage, HttpFuzzLocation, HttpPayload, HttpFuzzGap> {

	private static final long serialVersionUID = -6286527080805168790L;

	private HttpFuzzComponent fuzzComponent;
	private JCheckBox enableTokens;
	private JCheckBox showTokenRequests;
	private JCheckBox followRedirects;
	private JCheckBox urlEncode;

	private boolean incAcsrfToken = false;
	private HttpPayloadFactory factory;
	private HttpFuzzerDialogTokenPane tokenPane;

	private boolean conModification = false;

	@Override
	public FuzzProcessFactory<HttpFuzzProcess, HttpPayload, HttpFuzzLocation> getFuzzProcessFactory() {
		AntiCsrfToken token = null;
		if (getEnableTokens().isSelected() && getTokensPane().isEnable()) {
			token = getTokensPane().getToken();
		}
		return new HttpFuzzProcessFactory(fuzzableMessage, getFollowRedirects()
				.isSelected());
	}

	public HttpFuzzDialog(ExtensionFuzz ext, HttpMessage msg,
			HttpFuzzLocation loc) {
		super(ext, loc, msg);
		fuzzableMessage = msg;
		ExtensionAntiCSRF extAntiCSRF = (ExtensionAntiCSRF) Control
				.getSingleton().getExtensionLoader()
				.getExtension(ExtensionAntiCSRF.NAME);
		List<AntiCsrfToken> tokens = null;
		if (extAntiCSRF != null) {
			tokens = extAntiCSRF.getTokens(msg);
		}
		if (tokens == null || tokens.size() == 0) {
			incAcsrfToken = false;
		} else {
			incAcsrfToken = true;
		}
		if (incAcsrfToken) {
			setAntiCsrfTokens(tokens);
			this.setSize(500, 550);
		}
		this.factory = new HttpPayloadFactory();
		getTargetField().getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent sel) {
						if (!conModification) {
							conModification = true;
							if (targetTable.getSelectedRow() > -1) {
								HttpFuzzGap mod = (HttpFuzzGap) targetModel
										.getEntries().get(
												targetTable.getSelectedRow());
								PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> payDialog = new PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory>(
										mod,
										(HttpPayloadFactory) getPayloadFactory(),
										res);
								payDialog
										.setModalityType(ModalityType.APPLICATION_MODAL);
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
														|| result.getPayloads()
																.size() < 1) {
													targetModel
															.removeEntry(result);
													if(targetModel.getRowCount() < 1){
														getStartButton().setEnabled(false);
													}
													getMessageContent().highlight(targetModel.getEntries());
												}
											}
										});
								payDialog.setVisible(true);
							}
							conModification = false;
						}
					}
				});
	}

	@Override
	protected int addCustomComponents(JPanel panel, int currentRow) {
		if (incAcsrfToken) { // Options for AcsrfTokens
			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.anticsrf")),
					getGBC(0, currentRow, 6, 2.0D));
			currentRow++;
			panel.add(getEnableTokens(), getGBC(0, currentRow, 6, 0.0D));
			currentRow++;
			panel.add(
					getTokensPane().getPane(),
					getGBC(0, currentRow, 6, 1.0D, 0.0D,
							java.awt.GridBagConstraints.BOTH));
			currentRow++;

			panel.add(
					new JLabel(Constant.messages
							.getString("fuzz.label.showtokens")),
					getGBC(0, currentRow, 2, 1.0D));
			panel.add(getShowTokenRequests(), getGBC(2, currentRow, 4, 0.0D));
			currentRow++;
		}
		panel.add(
				new JLabel(Constant.messages
						.getString("fuzz.label.followredirects")),
				getGBC(0, currentRow, 2, 0.125));
		panel.add(getFollowRedirects(), getGBC(2, currentRow, 4, 0.125));
		currentRow++;

		panel.add(
				new JLabel(Constant.messages.getString("fuzz.label.urlencode")),
				getGBC(0, currentRow, 2, 0.125));
		panel.add(getUrlEncode(), getGBC(2, currentRow, 4, 0.125));
		currentRow++;

		return currentRow;
	}

	private HttpFuzzerDialogTokenPane getTokensPane() {
		if (tokenPane == null) {
			tokenPane = new HttpFuzzerDialogTokenPane();
		}
		return tokenPane;
	}

	private void setAntiCsrfTokens(List<AntiCsrfToken> acsrfTokens) {
		if (acsrfTokens != null && acsrfTokens.size() > 0) {
			getTokensPane().setAll(true, acsrfTokens.get(0),
					acsrfTokens.get(0).getTargetURL());
			this.getEnableTokens().setSelected(true);
			this.getEnableTokens().setEnabled(true);
			this.getTokensPane().getPane().setVisible(true);
		} else {
			getTokensPane().reset();
			this.getEnableTokens().setSelected(false);
			this.getEnableTokens().setEnabled(false);
			this.getTokensPane().getPane().setVisible(false);
		}
	}

	private JCheckBox getEnableTokens() {
		if (enableTokens == null) {
			enableTokens = new JCheckBox();
			enableTokens.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getTokensPane().setEnabled(enableTokens.isSelected());
					getShowTokenRequests()
							.setEnabled(enableTokens.isSelected());
				}
			});
		}
		return enableTokens;
	}

	private JCheckBox getShowTokenRequests() {
		if (showTokenRequests == null) {
			showTokenRequests = new JCheckBox();
		}
		return showTokenRequests;
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
		ArrayList<HttpPayload> payloads = new ArrayList<HttpPayload>();
		Stack<HttpPayload> base = new Stack<HttpPayload>();
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
		FileFuzzer<HttpPayload> ff = new FileFuzzer<HttpPayload>(
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

	private class HttpAddFuzzAction extends AddFuzzAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (adding) {
				if (isValidLocation(getMessageContent().selection())) {
					PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory> payDia = new PayloadDialog<HttpFuzzGap, HttpPayload, HttpPayloadFactory>(
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
									&& result.getPayloads().size() >= 0) {
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
			} else {
				super.actionPerformed(e);
			}
		}
	}

	private class HttpStartFuzzAction extends StartFuzzAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (HttpFuzzGap g : targetModel.getEntries()) {
				ArrayList<HttpPayload> files = new ArrayList<HttpPayload>();
				ArrayList<HttpPayload> regex = new ArrayList<HttpPayload>();
				ArrayList<HttpPayload> scripts = new ArrayList<HttpPayload>();
				for (HttpPayload p : g.getPayloads()) {
					if (p.getType().equals("SCRIPT")) {
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
					if (p.getType().equals("FILE")) {
						files.add(p);
					}
					if (p.getType().equals("REGEX")) {
						regex.add(p);
					}
				}
				for (HttpPayload p : files) {
					FileFuzzer<HttpPayload> fileFuzzer;
					String cat = p.getData().split(" --> ")[0];
					String choice = p.getData().split(" --> ")[1];
					try {
						if (isCustomCategory(cat)) {
							fileFuzzer = new FileFuzzer<HttpPayload>(new File(
									Constant.getInstance().FUZZER_CUSTOM_DIR
											+ File.separator + choice),
									getPayloadFactory());
						} else if (isJBroFuzzCategory(cat)) {

							fileFuzzer = convertToFileFuzzer(res
									.getJBroFuzzer(choice));

						} else {
							fileFuzzer = new FileFuzzer<HttpPayload>(
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
						e1.printStackTrace();
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