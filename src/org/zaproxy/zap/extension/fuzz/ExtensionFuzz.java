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
package org.zaproxy.zap.extension.fuzz;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.owasp.jbrofuzz.core.Database;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.search.SearchResult;

public class ExtensionFuzz extends ExtensionAdaptor implements FuzzerListener, SessionChangedListener {

	public final static String NAME = "ExtensionFuzz";
	public final static String JBROFUZZ_CATEGORY_PREFIX = "jbrofuzz / ";
	
    private PopupFuzzMenu popupFuzzMenu = null;
    private FuzzerThread fuzzerThread = null;
    private FuzzerParam fuzzerParam = null;
    private FuzzerPanel fuzzerPanel = null;
    private OptionsFuzzerPanel optionsFuzzerPanel = null;
    private boolean fuzzing = false;
    private Database jbroFuzzDB = new Database();
	private List <String> fuzzerCategories = new ArrayList<String>();
	private Map<String, DirCategory> catMap = new HashMap<String, DirCategory>();
    
	/**
     * 
     */
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
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(48);

        // Initialise the file based fuzzers
		addFileFuzzers(new File(Constant.getInstance().FUZZER_DIR), null);
        Collections.sort(fuzzerCategories);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuFuzz());
	        extensionHook.getHookView().addStatusPanel(getFuzzerPanel());
	        this.getFuzzerPanel().setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsFuzzerPanel());

	        extensionHook.addSessionListener(this);
	        
	    	ExtensionHelp.enableHelpKey(getFuzzerPanel(), "ui.tabs.fuzz");
	    }
        extensionHook.addOptionsParamSet(getFuzzerParam());

	}

	private FuzzerPanel getFuzzerPanel() {
		if (fuzzerPanel == null) {
			fuzzerPanel = new FuzzerPanel(this, this.getFuzzerParam());
		}
		return fuzzerPanel;
	}

	protected void addFuzzResult(HttpMessage msg) {
		this.getFuzzerPanel().addFuzzResult(msg);
	}
	
	public void scanProgress(int done, int todo) {
		this.getFuzzerPanel().scanProgress(done, todo);
	}


	public void startFuzzers (FuzzableHttpMessage fuzzableHttpMessage, Fuzzer[] fuzzers, FileFuzzer[] customFuzzers, AntiCsrfToken acsrfToken, 
			boolean showTokenRequests, boolean followRedirects, boolean urlEncode) {
		this.getFuzzerPanel().scanStarted();

		fuzzerThread = new FuzzerThread(this, getFuzzerParam(), getModel().getOptionsParam().getConnectionParam());
		fuzzerThread.setTarget(fuzzableHttpMessage, fuzzers, customFuzzers, acsrfToken, showTokenRequests, followRedirects, urlEncode);
		fuzzerThread.addFuzzerListener(this);
		fuzzerThread.start();

	}
	
	public void stopFuzzers() {
	    if (fuzzerThread != null) {
	        fuzzerThread.stop();
	    }
	}

	public void pauseFuzzers() {
		fuzzerThread.pause();
	}

	public void resumeFuzzers() {
		fuzzerThread.resume();
	}
	
	public List<SearchResult> searchFuzzResults(Pattern pattern, boolean inverse) {
		return this.getFuzzerPanel().searchResults(pattern, inverse);
	}

    protected void showFuzzDialog(Component invoker) {
        showFuzzDialog(getView().getMainFrame(), invoker);
    }

    private void showFuzzDialog(JFrame frame, Component invoker) {
    	if (!(invoker instanceof FuzzableComponent)) {
    		return;
    	}
    	
    	FuzzableComponent fuzzableComponent = (FuzzableComponent)invoker;
		FuzzableHttpMessage fuzzableHttpMessage = fuzzableComponent.getFuzzableHttpMessage();
		
		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

		List<AntiCsrfToken> tokens = null;
		if (extAntiCSRF != null) {
			tokens = extAntiCSRF.getTokens(fuzzableHttpMessage.getHttpMessage());
		}
		
		FuzzDialog fuzzDialog;
		
		if (tokens == null || tokens.size() == 0) {
			fuzzDialog = new FuzzDialog(this, frame, false, false);
		} else {
			fuzzDialog = new FuzzDialog(this, frame, false, true);
			fuzzDialog.setAntiCsrfTokens(tokens);
		}
		fuzzDialog.setDefaultCategory(this.getFuzzerParam().getDefaultCategory());
		fuzzDialog.setSelection(fuzzableComponent);
		fuzzDialog.setVisible(true);
        
    }
    
    private PopupFuzzMenu getPopupMenuFuzz() {
        if (popupFuzzMenu== null) {
            popupFuzzMenu = new PopupFuzzMenu(this);
            popupFuzzMenu.setText(Constant.messages.getString("fuzz.tools.menu.fuzz"));
            popupFuzzMenu.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	Object source = e.getSource();
                	if (source != null && source instanceof PopupFuzzMenu) {
                		Component invoker = ((PopupFuzzMenu)source).getLastInvoker();
                        showFuzzDialog(invoker);
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

	private OptionsFuzzerPanel getOptionsFuzzerPanel() {
		if (optionsFuzzerPanel == null) {
			optionsFuzzerPanel = new OptionsFuzzerPanel(this);
		}
		return optionsFuzzerPanel;
	}

	@Override
	public void notifyFuzzProcessComplete(FuzzProcess fp) {
		if (fp.isShowTokenRequests()) {
			for (HttpMessage tokenMsg : fp.getTokenRequests()) {
				addFuzzResult(tokenMsg);
			}
		}
		
		// Record the fuzz payload in the note		
		fp.getHttpMessage().setNote(fp.getFuzz());
		
		addFuzzResult(fp.getHttpMessage());
	}

	@Override
	public void notifyFuzzProcessStarted(FuzzProcess fp) {
		this.fuzzing = true;
	}

	@Override
	public void notifyFuzzerComplete() {
		this.getFuzzerPanel().scanFinshed();
		this.fuzzing = false;
	}

	public boolean isFuzzing() {
		return fuzzing;
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
					if (! file.getName().toLowerCase().startsWith("docs")) {
						// Ignore all files under 'docs.*' folders
						addFileFuzzers(file, dirCat.getName());
					}
				} else if (file.getName().toLowerCase().endsWith(".txt") &&
						! file.getName().startsWith("_") && 
						! file.getName().toLowerCase().startsWith("readme")){
					dirCat.addFuzzer(new FileFuzzer(file));
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

	public FileFuzzer getCustomFileFuzzer(String string) {
		return new FileFuzzer(new File(Constant.getInstance().FUZZER_CUSTOM_DIR + File.separator + string));
	}
	
	public List<String> getCustomFileList() {
        List <String> fileList = new ArrayList<String>();
        File customDir = new File(Constant.getInstance().FUZZER_CUSTOM_DIR);
        File[] customFiles = customDir.listFiles();
        if (customFiles != null) {
                Arrays.sort(customFiles);
                for (File file : customFiles) {
                        if (! file.isDirectory()) {
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

	public List <String> getFileFuzzerNames(String category) {
		List <String> fuzzers = new ArrayList<String>();
		DirCategory dirCat = this.catMap.get(category);
		if (dirCat != null) {
			for (FileFuzzer ff : dirCat.getFuzzers()) {
				fuzzers.add(ff.getFileName());
			}
		}
		return fuzzers;
	}

	public FileFuzzer getFileFuzzer(String category, String name) {
		DirCategory dirCat = this.catMap.get(category);
		if (dirCat != null) {
			return dirCat.getFileFuzzer(name);
		}
		return null;
	}

	public List<String> getJBroFuzzCategories() {
		String[] allCats = jbroFuzzDB.getAllCategories();
		Arrays.sort(allCats);
		List <String> categories = new ArrayList<String>(allCats.length);
		for (String category : allCats) {
			categories.add(JBROFUZZ_CATEGORY_PREFIX + category);
		}
		return categories;
	}

	public List <String> getJBroFuzzFuzzerNames(String category) {
		String jbfCategory = category.substring(ExtensionFuzz.JBROFUZZ_CATEGORY_PREFIX.length());
		String [] fuzzers = jbroFuzzDB.getPrototypeNamesInCategory(jbfCategory);
		Arrays.sort(fuzzers);
		List <String> fuzzerNames = new ArrayList<String>(fuzzers.length);
		for (String fuzzer : fuzzers) {
			fuzzerNames.add(fuzzer);
		}
		return fuzzerNames;
	}

	public Fuzzer getJBroFuzzer(String name) throws NoSuchFuzzerException {
		return jbroFuzzDB.createFuzzer(jbroFuzzDB.getIdFromName(name), 1);
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
}
