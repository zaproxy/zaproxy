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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.owasp.jbrofuzz.core.Database;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.owasp.jbrofuzz.version.JBroFuzzPrefs;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.search.SearchResult;

public class ExtensionFuzz extends ExtensionAdaptor implements
		SessionChangedListener, AddonFilesChangedListener {

	public static final String NAME = "MultiExtensionFuzz";
	public static final String JBROFUZZ_CATEGORY_PREFIX = "jbrofuzz / ";
	public static final String SCRIPT_TYPE_FUZZ = "Fuzz";
	public static final String SCRIPT_TYPE_PAYLOAD = "payload";
	private static final ImageIcon SCRIPT_ICON = new ImageIcon(
			ZAP.class.getResource("/resource/icon/16/script-pscan.png"));

	private PopupFuzzMenu popupFuzzMenu = null;
	private FuzzerParam fuzzerParam = null;
	private FuzzerPanel fuzzerPanel = null;
	private OptionsFuzzerPanel optionsFuzzerPanel = null;
	private Database jbroFuzzDB = null;
	private List<String> fuzzerCategories = new ArrayList<>();
	private Map<String, DirCategory> catMap = new HashMap<>();
	private ExtensionHook hook;
	private Map<Class<? extends Message>, FuzzerHandler> fuzzableMessageHandlers;

	public ExtensionFuzz() {
		super();
		initialize();
	}

	/**
	 * @param name
	 */
	public ExtensionFuzz(String name) {
		super(name);
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setName(NAME);
		this.setOrder(47);
		// Force JBroFuzz to use the ZAP user directory
		Preferences PREFS = Preferences.userRoot().node("owasp/jbrofuzz");
		PREFS.putBoolean(JBroFuzzPrefs.DIRS[1].getId(), true);
		PREFS.put(JBroFuzzPrefs.DIRS[0].getId(), Constant.getZapHome());
		loadFiles();
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		if (getView() != null) {
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuFuzz());
			extensionHook.getHookView().addStatusPanel(getFuzzerPanel());
			extensionHook.getHookView().addOptionPanel(getOptionsFuzzerPanel());
			extensionHook.addSessionListener(this);
			ExtensionHelp.enableHelpKey(getFuzzerPanel(), "ui.tabs.fuzz");
		}
		fuzzableMessageHandlers = new HashMap<>();
		this.hook = extensionHook;
		extensionHook.addOptionsParamSet(getFuzzerParam());
		extensionHook.addAddonFilesChangedListener(this);
		ExtensionScript extScript = (ExtensionScript) Control.getSingleton()
				.getExtensionLoader().getExtension(ExtensionScript.NAME);
		if (extScript != null) {
			extScript.registerScriptType(new ScriptType(SCRIPT_TYPE_FUZZ,
					"fuzz.scripts.type.fuzz", SCRIPT_ICON, true));
			extScript.registerScriptType(new ScriptType(SCRIPT_TYPE_PAYLOAD,
					"fuzz.scripts.type.payload", SCRIPT_ICON, true));
		}
	}

	public ExtensionHook getHook() {
		return this.hook;
	}

	private Database getDB() {
		if (jbroFuzzDB == null) {
			jbroFuzzDB = new Database();
		}
		return jbroFuzzDB;
	}

	private void loadFiles() {
		// (Re)Initialise the file based fuzzers
		fuzzerCategories = new ArrayList<>();
		catMap = new HashMap<>();

		addFileFuzzers(new File(Constant.getInstance().FUZZER_DIR), null);
		addFileFuzzers(new File(Constant.getZapHome(),
				Constant.getInstance().FUZZER_DIR), null);
		Collections.sort(fuzzerCategories);
	}

	public boolean canFuzz(Class<? extends Message> clazz) {
		return fuzzableMessageHandlers.containsKey(clazz);
	}

	public void addFuzzerHandler(Class<? extends Message> clazz,
			FuzzerHandler handler) {
		fuzzableMessageHandlers.put(clazz, handler);
	}

	public void removeFuzzerHandler(Class<? extends Message> clazz) {
		fuzzableMessageHandlers.remove(clazz);
	}

	public FuzzerPanel getFuzzerPanel() {
		if (fuzzerPanel == null) {
			fuzzerPanel = new FuzzerPanel(this);
		}
		return fuzzerPanel;
	}

	public List<SearchResult> searchFuzzResults(Pattern pattern, boolean inverse) {
		if (fuzzableMessageHandlers != null) {
			List<SearchResult> results = new ArrayList<>();
			for (Class<? extends Message> m : fuzzableMessageHandlers.keySet()) {
				results.addAll(fuzzableMessageHandlers.get(m).searchResults(
						pattern, inverse));
			}
			return results;
		}
		return Collections.emptyList();
	}

	public void stopFuzzers() {
		for (FuzzerHandler<?, ?> h : fuzzableMessageHandlers.values()) {
			h.stopFuzzers();
		}
	}

	public void pauseFuzzers() {
		for (FuzzerHandler<?, ?> h : fuzzableMessageHandlers.values()) {
			h.pauseFuzzers();
		}
	}

	public void resumeFuzzers() {
		for (FuzzerHandler<?, ?> h : fuzzableMessageHandlers.values()) {
			h.resumeFuzzers();
		}
	}

	private FuzzerHandler getFuzzableMessageHandler(
			Class<? extends Message> messageClass) {
		return fuzzableMessageHandlers.get(messageClass);
	}

	public PopupFuzzMenu getPopupMenuFuzz() {
		if (popupFuzzMenu == null) {
			popupFuzzMenu = new PopupFuzzMenu(this);
			popupFuzzMenu
					.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							Object source = e.getSource();
							if (source != null
									&& source instanceof PopupFuzzMenu) {
								try {
									FuzzableComponent invoker = (FuzzableComponent) ((PopupFuzzMenu) source)
											.getLastInvoker();
									getFuzzableMessageHandler(
											(invoker.getMessageClass()))
											.showFuzzDialog(invoker);
								} catch (ClassCastException c) {
									final org.zaproxy.zap.extension.fuzz.FuzzableComponent invoker = (org.zaproxy.zap.extension.fuzz.FuzzableComponent) ((PopupFuzzMenu) source)
											.getLastInvoker();
									getFuzzableMessageHandler(
											(invoker.getMessageClass()))
											.showFuzzDialog(
													new FuzzableComponentWrapper(
															invoker));
								}
							}
						}
					});
		}
		return popupFuzzMenu;
	}

	private FuzzerParam getFuzzerParam() {
		if (fuzzerParam == null) {
			fuzzerParam = new FuzzerParam();
		}
		return fuzzerParam;
	}

	public String getDefaultCategory() {
		return getFuzzerParam().getDefaultCategory();
	}

	private OptionsFuzzerPanel getOptionsFuzzerPanel() {
		if (optionsFuzzerPanel == null) {
			optionsFuzzerPanel = new OptionsFuzzerPanel(this);
		}
		return optionsFuzzerPanel;
	}

	private void addFileFuzzers(File dir, String parent) {
		boolean addedFuzzer = false;
		File[] files = dir.listFiles();
		DirCategory dirCat;
		if (parent == null) {
			dirCat = new DirCategory("");
		} else if (parent.length() == 0) {
			// Gets rid of the first slash :)
			dirCat = new DirCategory(dir.getName());
		} else {
			dirCat = new DirCategory(parent + " / " + dir.getName());
		}
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					if (!file.getName().toLowerCase().startsWith("docs")) {
						// Ignore all files under 'docs.*' folders
						addFileFuzzers(file, dirCat.getName());
					}
				} else if (file.getName().toLowerCase().endsWith(".txt")
						&& !file.getName().startsWith("_")
						&& !file.getName().toLowerCase().startsWith("readme")) {
					dirCat.addFuzzer(file);
					addedFuzzer = true;
				}
			}
		}
		if (addedFuzzer) {
			// Dont add 'empty' categories / directories
			this.fuzzerCategories.add(dirCat.getName());
			this.catMap.put(dirCat.getName(), dirCat);
		}
	}

	public List<String> getCustomFileList() {
		List<String> fileList = new ArrayList<>();
		File customDir = new File(Constant.getInstance().FUZZER_CUSTOM_DIR);
		File[] customFiles = customDir.listFiles();
		if (customFiles != null) {
			Arrays.sort(customFiles);
			for (File file : customFiles) {
				if (!file.isDirectory()) {
					fileList.add(file.getName());
				}
			}
		}
		Collections.sort(fileList);
		return fileList;
	}

	public List<String> getFileFuzzerCategories() {
		return fuzzerCategories;
	}

	public List<String> getFileFuzzerNames(String category) {
		List<String> fuzzers = new ArrayList<>();
		DirCategory dirCat = this.catMap.get(category);
		if (dirCat != null) {
			for (File ff : dirCat.getFuzzers()) {
				fuzzers.add(ff.getName());
			}
		}
		return fuzzers;
	}

	public File getFuzzFile(String category, String name) {
		DirCategory dirCat = this.catMap.get(category);
		if (dirCat != null) {
			return dirCat.getFuzzerFile(name);
		}
		return null;
	}

	public List<String> getJBroFuzzCategories() {
		String[] allCats = getDB().getAllCategories();
		Arrays.sort(allCats);
		List<String> categories = new ArrayList<>(allCats.length);
		for (String category : allCats) {
			categories.add(JBROFUZZ_CATEGORY_PREFIX + category);
		}
		return categories;
	}

	public List<String> getJBroFuzzFuzzerNames(String category) {
		String jbfCategory = category
				.substring(ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX.length());
		String[] fuzzers = getDB().getPrototypeNamesInCategory(jbfCategory);
		Arrays.sort(fuzzers);
		List<String> fuzzerNames = new ArrayList<>(fuzzers.length);
		for (String fuzzer : fuzzers) {
			fuzzerNames.add(fuzzer);
		}
		return fuzzerNames;
	}

	public Fuzzer getJBroFuzzer(String name) throws NoSuchFuzzerException {
		return getDB().createFuzzer(getDB().getIdFromName(name), 1);
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("fuzz.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void sessionAboutToChange(Session session) {
		if (getView() != null) {
			getFuzzerPanel().reset();
		}
	}

	@Override
	public void sessionChanged(Session session) {
	}

	@Override
	public void sessionScopeChanged(Session session) {
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}

	@Override
	public void filesAdded() {
		loadFiles();
		if (getView() != null) {
			getOptionsFuzzerPanel().updateFuzzCategories();
		}
	}

	@Override
	public void filesRemoved() {
		loadFiles();
		if (getView() != null) {
			getOptionsFuzzerPanel().updateFuzzCategories();
		}
	}
}
