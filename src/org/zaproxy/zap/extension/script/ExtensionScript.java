/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP development team
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
package org.zaproxy.zap.extension.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;

public class ExtensionScript extends ExtensionAdaptor implements CommandLineListener {
	
	public static final int EXTENSION_ORDER = 60;
	public static final String NAME = "ExtensionScript";
	public static final ImageIcon ICON = new ImageIcon(ZAP.class.getResource("/resource/icon/16/059.png")); // Script icon
	
	public static final String SCRIPTS_DIR = "scripts";
	public static final String TEMPLATES_DIR = SCRIPTS_DIR + File.separator + "templates";
	private static final String LANG_ENGINE_SEP = " : ";
	protected static final String SCRIPT_CONSOLE_HOME_PAGE = "http://code.google.com/p/zaproxy/wiki/ScriptConsole";

	public static final String TYPE_PROXY = "proxy";
	public static final String TYPE_STANDALONE = "standalone";
	public static final String TYPE_TARGETED = "targeted";

	private static final ImageIcon PROXY_ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/script-proxy.png"));
	private static final ImageIcon STANDALONE_ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/script-standalone.png"));
	private static final ImageIcon TARGETED_ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/script-targeted.png"));
	
	private ScriptEngineManager mgr = new ScriptEngineManager();
	private ScriptParam scriptParam = null;

	private ScriptTreeModel treeModel = null;
	private List <ScriptEngineWrapper> engineWrappers = new ArrayList<>();
	private Map<String, ScriptType> typeMap = new HashMap<>();
	private ProxyListenerScript proxyListener = null;
	
	private List<ScriptEventListener> listeners = new ArrayList<>();
	private MultipleWriters writers = new MultipleWriters();
	private ScriptUI scriptUI = null;

	private CommandLineArgument[] arguments = new CommandLineArgument[1];
    private static final int ARG_SCRIPT_IDX = 0;

	private static final Logger logger = Logger.getLogger(ExtensionScript.class);

    public ExtensionScript() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionScript(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(EXTENSION_ORDER);
        
        ScriptEngine se = mgr.getEngineByName("ECMAScript");
        if (se != null) {
        	this.registerScriptEngineWrapper(new JavascriptEngineWrapper(se));
        } else {
        	logger.error("No Javascript/ECMAScript engine found");
        }
        
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

		this.registerScriptType(new ScriptType(TYPE_PROXY, "script.type.proxy", PROXY_ICON, true));
		this.registerScriptType(new ScriptType(TYPE_STANDALONE, "script.type.standalone", STANDALONE_ICON, false));
		this.registerScriptType(new ScriptType(TYPE_TARGETED, "script.type.targeted", TARGETED_ICON, false));

		extensionHook.addProxyListener(this.getProxyListener());
	    extensionHook.addOptionsParamSet(getScriptParam());

	    extensionHook.addCommandLine(getCommandLineArguments());
	    
		if (! View.isInitialised()) {
        	// No GUI so add stdout as a writer
        	this.addWriter(new PrintWriter(System.out));
		}

	}
	
	private ProxyListenerScript getProxyListener() {
		if (this.proxyListener == null) {
			this.proxyListener = new ProxyListenerScript(this);
		}
		return this.proxyListener;
	}
	
	public List<String> getScriptingEngines() {
		List <String> engineNames = new ArrayList<>();
		List<ScriptEngineFactory> engines = mgr.getEngineFactories();
		for (ScriptEngineFactory engine : engines) {
			engineNames.add(engine.getLanguageName() + LANG_ENGINE_SEP + engine.getEngineName());
		}
		for (ScriptEngineWrapper sew : this.engineWrappers) {
			if (! engines.contains(sew.getEngine().getFactory())) {
				engineNames.add(sew.getLanguageName() + LANG_ENGINE_SEP + sew.getEngineName());
			}
		}
		
		Collections.sort(engineNames);
		return engineNames;
	}
	
	public void registerScriptEngineWrapper(ScriptEngineWrapper wrapper) {
		logger.debug("registerEngineWrapper " + wrapper.getLanguageName() + " : " + wrapper.getEngineName());
		this.engineWrappers.add(wrapper);
		// Templates for this engine might not have been loaded
		this.loadTemplates(wrapper);

	}
	
	public ScriptEngineWrapper getEngineWrapper(String name) {
		for (ScriptEngineWrapper sew : this.engineWrappers) {
			// In the configs we just use the engine name, in the UI we use the language name as well
			if (name.indexOf(LANG_ENGINE_SEP) > 0) {
				if (name.equals(sew.getLanguageName() + LANG_ENGINE_SEP + sew.getEngineName())) {
					return sew;
				}
			} else {
				if (name.equals(sew.getEngineName())) {
					return sew;
				}
			}
		}

		for (ScriptEngineWrapper sew : this.engineWrappers) {
			// Nasty, but sometime the engine names are reported differently, eg 'Mozilla Rhino' vs 'Rhino'
			if (name.indexOf(LANG_ENGINE_SEP) < 0) {
				if (name.endsWith(sew.getEngineName())) {
					return sew;
				}
				if (sew.getEngineName().endsWith(name)) {
					return sew;
				}
			}
		}

		// Not one we know of, create a default wrapper
		List<ScriptEngineFactory> engines = mgr.getEngineFactories();
		ScriptEngine engine = null;
		for (ScriptEngineFactory e : engines) {
			if (name.indexOf(LANG_ENGINE_SEP) > 0) {
				if (name.equals(e.getLanguageName() + LANG_ENGINE_SEP + e.getEngineName())) {
					engine = e.getScriptEngine();
					break;
				}
			} else {
				if (name.equals(e.getEngineName())) {
					engine = e.getScriptEngine();
					break;
				}
			}
		}
		if (engine != null) {
			DefaultEngineWrapper dew = new DefaultEngineWrapper(engine);
			this.registerScriptEngineWrapper(dew);
			return dew;
		}
		throw new InvalidParameterException("No such engine: " + name);
	}
	
	public String getEngineNameForExtension(String ext) {
		ScriptEngine engine = mgr.getEngineByExtension(ext);
		if (engine != null) {
			return engine.getFactory().getLanguageName() + LANG_ENGINE_SEP + engine.getFactory().getEngineName();
		}
		for (ScriptEngineWrapper sew : this.engineWrappers) {
			if (sew.getExtensions() != null) {
				for (String extn : sew.getExtensions()) {
					if (ext.equals(extn)) {
						return sew.getLanguageName() + LANG_ENGINE_SEP + sew.getEngineName();
					}
				}
			}
		}
		return null;
	}

	protected ScriptParam getScriptParam() {
		if (this.scriptParam == null) {
			this.scriptParam = new ScriptParam();
			// NASTY! Need to find a cleaner way of getting the configs to load before the UI
			this.scriptParam.load(Model.getSingleton().getOptionsParam().getConfig());
		}
		return this.scriptParam;
	}

	public ScriptTreeModel getTreeModel() {
		if (this.treeModel == null) {
			this.treeModel = new ScriptTreeModel();
		}
		return this.treeModel;
	}
	
	public void registerScriptType(ScriptType type) {
		if (typeMap.containsKey(type.getName())) {
			throw new InvalidParameterException("ScriptType already registered: " + type.getName());
		}
		this.typeMap.put(type.getName(), type);
		this.getTreeModel().addType(type);
	}

	public ScriptType getScriptType (String name) {
		return this.typeMap.get(name);
	}
	
	public Collection<ScriptType> getScriptTypes() {
		return typeMap.values();
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("script.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	private void refreshScript(ScriptWrapper script) {
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.refreshScript(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public ScriptWrapper getScript(String name) {
		ScriptWrapper script =  this.getTreeModel().getScript(name);
		refreshScript(script);
		return script;
	}
	
	public ScriptNode addScript(ScriptWrapper script) {
		return this.addScript(script, true);
	}
	
	public ScriptNode addScript(ScriptWrapper script, boolean display) {
		if (script == null) {
			return null;
		}
		ScriptNode node = this.getTreeModel().addScript(script);
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptAdded(script, display);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return node;
	}

	public void saveScript(ScriptWrapper script) throws IOException {
		refreshScript(script);
	    BufferedWriter fw = new BufferedWriter(new FileWriter(script.getFile(), false));
        fw.append(script.getContents());
        fw.close();
        this.setChanged(script, false);
        // The removal is required for script that use wrappers, like Zest
		this.getScriptParam().removeScript(script);
		this.getScriptParam().addScript(script);
		this.getScriptParam().saveScripts();
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptSaved(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void removeScript(ScriptWrapper script) {
		script.setLoadOnStart(false);
		this.getScriptParam().saveScripts();
		this.getTreeModel().removeScript(script);
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptRemoved(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void removeTemplate(ScriptWrapper template) {
		this.getTreeModel().removeTemplate(template);
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.templateRemoved(template);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public ScriptNode addTemplate(ScriptWrapper template) {
		return this.addTemplate(template, true);
	}
	
	public ScriptNode addTemplate(ScriptWrapper template, boolean display) {
		if (template == null) {
			return null;
		}
		ScriptNode node = this.getTreeModel().addTemplate(template);
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.templateAdded(template, display);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return node;
	}

	@Override
	public void optionsLoaded() {
		for (ScriptWrapper script : this.getScriptParam().getScripts()) {
			try {
				this.loadScript(script);
				this.addScript(script, false);
				
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		this.loadTemplates();
	}

	private void loadTemplates() {
		this.loadTemplates(null);
	}

	private void loadTemplates(ScriptEngineWrapper engine) {
		for (ScriptType type : this.getScriptTypes()) {
			File locDir = new File(Constant.getZapHome() + File.separator + TEMPLATES_DIR + File.separator + type.getName());
			File stdDir = new File(Constant.getZapInstall() + File.separator  + TEMPLATES_DIR + File.separator + type.getName());
			
			// Load local files first, as these override any one included in the release
			if (locDir.exists()) {
				for (File f : locDir.listFiles()) {
					loadTemplate(f, type, engine, false);
				}
			}
			for (File f : stdDir.listFiles()) {
				// Dont log errors on duplicates - 'local' templates should take presidence
				loadTemplate(f, type, engine, true);
			}
		}
	}

	private void loadTemplate(File f, ScriptType type, ScriptEngineWrapper engine, boolean ignoreDuplicates) {
		if (f.getName().indexOf(".") > 0) {
			if (this.getTreeModel().getTemplate(f.getName()) == null) {
				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
				String engineName = this.getEngineNameForExtension(ext);
				if (engineName != null && (engine == null || engine.getEngine().getFactory().getExtensions().contains(ext))) {
					try {
						ScriptWrapper template = new ScriptWrapper(f.getName(), "", 
								this.getEngineWrapper(engineName), type, false, f);
						this.loadScript(template);
						this.addTemplate(template);
					} catch (InvalidParameterException e) {
						if (! ignoreDuplicates) {
							logger.error(e.getMessage(), e);
						}
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public ScriptWrapper loadScript(ScriptWrapper script) throws IOException {
	    BufferedReader fr = new BufferedReader(new FileReader(script.getFile()));
	    StringBuilder sb = new StringBuilder();
        String line;
        try {
			while ((line = fr.readLine()) != null) {
			    sb.append(line);
			    sb.append("\n");
			}
		} finally {
	        fr.close();
		}
        script.setContents(sb.toString());
        script.setChanged(false);
        
        if (script.getType() == null) {
        	// This happens when scripts are loaded from the configs as the types 
        	// may well not have been registered at that stage
        	script.setType(this.getScriptType(script.getTypeName()));
        }
	    return script;
	}

	public List<ScriptWrapper> getScripts(String type) {
		return this.getScripts(this.getScriptType(type));
	}

	public List<ScriptWrapper> getScripts(ScriptType type) {
		List<ScriptWrapper> scripts = new ArrayList<>();
		if (type == null) {
			return scripts;
		}
		for (ScriptNode node : this.getTreeModel().getNodes(type.getName())) {
			ScriptWrapper script = (ScriptWrapper) node.getUserObject();
			refreshScript(script);
			scripts.add((ScriptWrapper) node.getUserObject());
		}
		return scripts;
	}
	
	/*
	 * This extension supports any number of writers to be registered which all get written to for
	 * ever script. It also supports script specific writers.
	 */
	private Writer getWriters(ScriptWrapper script) {
		Writer writer = script.getWriter();
		if (writer == null) {
			// No script specific writer, just return the std one
			return this.writers;
		} else {
			// Return the script specific writer in addition to the std one
			MultipleWriters scriptWriters = new MultipleWriters();
			scriptWriters.addWriter(writer);
			scriptWriters.addWriter(writers);
			return scriptWriters;
		}
	}

	public Invocable invokeScript(ScriptWrapper script) throws ScriptException, IOException {
		logger.debug("invokeScript " + script.getName());
		if (script.getEngine() == null) {
			// Scripts loaded from the configs my have loaded before all of the engines
			script.setEngine(this.getEngineWrapper(script.getEngineName()));
		}
		
		if (script.getEngine() == null) {
			throw new ScriptException("Failed to find script engine: " + script.getEngineName());
		}
		
		refreshScript(script);
		script.setLastErrorDetails("");
		script.setLastException(null);
		script.setLastOutput("");
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.preInvoke(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		ScriptEngine se = script.getEngine().getEngine();
	    Writer writer = getWriters(script);
	    se.getContext().setWriter(writer);

	    try {
	    	se.eval(script.getContents());
	    } catch (Exception e) {
	    	if (e instanceof ScriptException && e.getCause() instanceof Exception) {
	    		// Dereference one level
	    		e = (Exception)e.getCause();
	    	}
	    	writer.append(e.toString());
	    	this.setError(script, e);
	    	this.setEnabled(script, false);
	    }

	    if (se instanceof Invocable) {
	    	return (Invocable) se;
	    } else {
	    	return null;
	    }
	}
	
    public void invokeTargetedScript(ScriptWrapper script, HttpMessage msg) {
    	if (TYPE_TARGETED.equals(script.getTypeName())) {
    	    Writer writer = getWriters(script);
			try {
				// Dont need to check if enabled as it can only be invoked manually
				TargetedScript s = this.getInterface(script, TargetedScript.class);
				
				if (s != null) {
					s.invokeWith(msg);
					
				} else {
					writer.append(Constant.messages.getString("script.interface.targeted.error"));
					this.setError(script, writer.toString());
					this.setEnabled(script, false);
				}
			
			} catch (Exception e) {
		    	if (e instanceof ScriptException && e.getCause() instanceof Exception) {
		    		// Dereference one level
		    		e = (Exception)e.getCause();
		    	}
				try {
					writer.append(e.toString());
				} catch (IOException e1) {
					logger.error(e.getMessage(), e);
				}
				this.setError(script, e);
				this.setEnabled(script, false);
			}
		} else {
			throw new InvalidParameterException("Script " + script.getName() + " is not a targeted script: " + script.getTypeName());
		}
	}

    public boolean invokeProxyScript(ScriptWrapper script, HttpMessage msg, boolean request) {
    	if (TYPE_PROXY.equals(script.getTypeName())) {
    	    Writer writer = getWriters(script);
			try {
				// Dont need to check if enabled as it can only be invoked manually
				ProxyScript s = this.getInterface(script, ProxyScript.class);
				
				if (s != null) {
					if (request) {
						return s.proxyRequest(msg);
					} else {
						return s.proxyResponse(msg);
					}
					
				} else {
					writer.append(Constant.messages.getString("script.interface.proxy.error"));
					this.setError(script, writer.toString());
					this.setEnabled(script, false);
				}
			
			} catch (Exception e) {
		    	if (e instanceof ScriptException && e.getCause() instanceof Exception) {
		    		// Dereference one level
		    		e = (Exception)e.getCause();
		    	}
				try {
					writer.append(e.toString());
				} catch (IOException e1) {
					logger.error(e.getMessage(), e);
				}
				this.setError(script, e);
				this.setEnabled(script, false);
			}
		} else {
			throw new InvalidParameterException("Script " + script.getName() + " is not a proxy script: " + script.getTypeName());
		}
    	// Return true so that the request is submitted - if we returned false all proxying would fail on script errors
    	return true;
	}


	public void setChanged(ScriptWrapper script, boolean changed) {
		script.setChanged(changed);
		ScriptNode node = this.getTreeModel().getNodeForScript(script);
		if (node.getNodeName().equals(script.getName())) {
			// The name is the same
			this.getTreeModel().nodeStructureChanged(script);
		} else {
			// The name has changed
			node.setNodeName(script.getName());
			this.getTreeModel().nodeStructureChanged(node.getParent());
		}
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptChanged(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void setEnabled(ScriptWrapper script, boolean enabled) {
		script.setEnabled(enabled);
		this.getTreeModel().nodeStructureChanged(script);
	}

	public void setError(ScriptWrapper script, String details) {
		script.setError(true);
		script.setLastOutput(details);
		
		this.getTreeModel().nodeStructureChanged(script);
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptError(script);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void setError(ScriptWrapper script, Exception e) {
		script.setError(true);
		script.setLastException(e);
		
		this.getTreeModel().nodeStructureChanged(script);
		
		for (ScriptEventListener listener : this.listeners) {
			try {
				listener.scriptError(script);
			} catch (Exception e1) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void addListener(ScriptEventListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(ScriptEventListener listener) {
		this.listeners.remove(listener);
	}

	public void addWriter(Writer writer) {
		this.writers.addWriter(writer);
	}
	
	public void removeWriter(Writer writer) {
		this.writers.removeWriter(writer);
	}

	public ScriptUI getScriptUI() {
		return scriptUI;
	}

	public void setScriptUI(ScriptUI scriptUI) {
		if (this.scriptUI != null) {
			throw new InvalidParameterException("A script UI has already been set - only one is supported");
		}
		this.scriptUI = scriptUI;
	}

	public void removeScriptUI() {
		this.scriptUI = null;
	}

	public <T> T getInterface(ScriptWrapper script, Class<T> class1) throws ScriptException, IOException {
	
		T iface = script.getInterface(class1);
		
		if (iface != null) {
			// the script wrapper has overriden the usual scripting mechanism
			return iface;
		}
		
		return invokeScript(script).getInterface(class1);

	}

	@Override
    public List<String> getUnsavedResources() {
		// Report all of the unsaved scripts
		List<String> list = new ArrayList<>();
		for (ScriptType type : this.getScriptTypes()) {
			for (ScriptWrapper script : this.getScripts(type)) {
				if (script.isChanged()) {
					list.add(MessageFormat.format(Constant.messages.getString("script.resource"), script.getName()));
				}
			}
		}
    	return list;
    }
	
	private void openCmdLineFile(File f) throws IOException, ScriptException {
		if (! f.exists()) {
			System.out.println(MessageFormat.format(
					Constant.messages.getString("script.cmdline.nofile"), f.getAbsolutePath()));
			return;
		}
		if (! f.canRead()) {
			System.out.println(MessageFormat.format(
					Constant.messages.getString("script.cmdline.noread"), f.getAbsolutePath()));
			return;
		}
		int dotIndex = f.getName().lastIndexOf(".");
		if (dotIndex <= 0) {
			System.out.println(MessageFormat.format(
					Constant.messages.getString("script.cmdline.noext"), f.getAbsolutePath()));
			return;
		}
		String ext = f.getName().substring(dotIndex+1);
		String engineName = this.getEngineNameForExtension(ext);
		if (engineName == null) {
			System.out.println(MessageFormat.format(
					Constant.messages.getString("script.cmdline.noengine"), ext));
			return;
		}
        ScriptWrapper sw = new ScriptWrapper(f.getName(), "", engineName,
        		this.getScriptType(TYPE_STANDALONE), true, f);
        
        this.loadScript(sw);
		this.addScript(sw);
		if (! View.isInitialised()) {
			// Only invoke if run from the command line
			// if the GUI is present then its up to the user to invoke it 
			this.invokeScript(sw);
		}
	}

    @Override
    public void execute(CommandLineArgument[] args) {
        if (arguments[ARG_SCRIPT_IDX].isEnabled()) {
            for (CommandLineArgument arg : args) {
            	Vector<String> params = arg.getArguments();
                if (params != null) {
                	for (String script : params) {
                		try {
                			openCmdLineFile(new File(script));
                		} catch (Exception e) {
                			logger.error(e.getMessage(), e);
                		}
                	}
                }
            }
        } else {
            return;
        }
    }

    private CommandLineArgument[] getCommandLineArguments() {
    	
        arguments[ARG_SCRIPT_IDX] = new CommandLineArgument("-script", 1, null, "", 
        		"-script [script_path]: " + Constant.messages.getString("script.cmdline.help"));
        return arguments;
    }

	//@Override
	public boolean handleFile(File file) {
		int dotIndex = file.getName().lastIndexOf(".");
		if (dotIndex <= 0) {
			// No extension, cant work out which engine
			return false;
		}
		String ext = file.getName().substring(dotIndex+1);
		String engineName = this.getEngineNameForExtension(ext);
		if (engineName == null) {
			// No engine for this extension, we cant handle this
			return false;
		}
		try {
			openCmdLineFile(file);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}

	@Override
	public List<String> getHandledExtensions() {
		// The list of all of the script extensions that can be handled from the command line 
		List<String> exts = new ArrayList<String>();
		for (ScriptEngineWrapper sew : this.engineWrappers) {
			exts.addAll(sew.getExtensions());
		}
		
		return exts;
	}

}
