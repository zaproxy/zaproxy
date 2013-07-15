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
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.ZAP;

public class ExtensionScript extends ExtensionAdaptor {
	
	public static final String NAME = "ExtensionScript";
	public static final ImageIcon ICON = new ImageIcon(ZAP.class.getResource("/resource/icon/16/059.png")); // Script icon
	
	public static final String SCRIPTS_DIR = "scripts";
	public static final String TEMPLATES_DIR = SCRIPTS_DIR + File.separator + "templates";
	private static final String LANG_ENGINE_SEP = " : ";
	protected static final String SCRIPT_CONSOLE_HOME_PAGE = "http://code.google.com/p/zaproxy/wiki/ScriptConsole";

	public static final String TYPE_STANDALONE = "standalone";
	public static final String TYPE_TARGETED = "targeted";

	private static final ImageIcon STANDALONE_ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/script-standalone.png"));
	private static final ImageIcon TARGETED_ICON = 
			new ImageIcon(ZAP.class.getResource("/resource/icon/16/script-targeted.png"));

	private ScriptEngineManager mgr = new ScriptEngineManager();
	private ScriptParam scriptParam = null;

	private ScriptTreeModel treeModel = null;
	private List <ScriptEngineWrapper> engineWrappers = new ArrayList<ScriptEngineWrapper>();
	private Map<String, ScriptType> typeMap = new HashMap<String, ScriptType>();
	
	private List<ScriptEventListener> listeners = new ArrayList<ScriptEventListener>();
	private MultipleWriters writers = new MultipleWriters();

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
        this.setOrder(60);
        
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

		this.registerScriptType(new ScriptType(TYPE_STANDALONE, "script.type.standalone", STANDALONE_ICON, false));
		this.registerScriptType(new ScriptType(TYPE_TARGETED, "script.type.targeted", TARGETED_ICON, false));

	    extensionHook.addOptionsParamSet(getScriptParam());
	}
	
	public List<String> getScriptingEngines() {
		List <String> engineNames = new ArrayList<>();
		List<ScriptEngineFactory> engines = mgr.getEngineFactories();
		for (ScriptEngineFactory engine : engines) {
			engineNames.add(engine.getLanguageName() + LANG_ENGINE_SEP + engine.getEngineName());
		}
		Collections.sort(engineNames);
		return engineNames;
	}
	
	public void registerScriptEngineWrapper(ScriptEngineWrapper wrapper) {
		this.engineWrappers.add(wrapper);
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
			listener.refreshScript(script);
		}
	}
	
	public ScriptWrapper getScript(String name) {
		ScriptWrapper script =  this.treeModel.getScript(name);
		refreshScript(script);
		return script;
	}
	
	public void addScript(ScriptWrapper script) {
		this.addScript(script, true);
	}
	
	public void addScript(ScriptWrapper script, boolean display) {
		if (script == null) {
			return;
		}
		this.getTreeModel().addScript(script);
		
		for (ScriptEventListener listener : this.listeners) {
			listener.scriptAdded(script);
		}
	}

	public void saveScript(ScriptWrapper script) throws IOException {
		refreshScript(script);
	    BufferedWriter fw = new BufferedWriter(new FileWriter(script.getFile(), false));
        fw.append(script.getContents());
        fw.close();
        this.setChanged(script, false);
		this.getScriptParam().addScript(script);
		this.getScriptParam().saveScripts();
		
		for (ScriptEventListener listener : this.listeners) {
			listener.scriptSaved(script);
		}

	}

	public void removeScript(ScriptWrapper script) {
		script.setLoadOnStart(false);
		this.getScriptParam().saveScripts();
		this.getTreeModel().removeScript(script);
		for (ScriptEventListener listener : this.listeners) {
			listener.scriptAdded(script);
		}

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
		List<ScriptWrapper> scripts = new ArrayList<ScriptWrapper>();
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
			listener.preInvoke(script);
		}

		ScriptEngine se = script.getEngine().getEngine();
	    se.getContext().setWriter(this.writers);
	    try {
	    	se.eval(script.getContents());
	    } catch (Exception e) {
	    	writers.append(e.toString());
	    	this.setError(script, e);
	    	this.setEnabled(script, false);
	    }
		return (Invocable) se;
	}
	
    public void invokeTargetedScript(ScriptWrapper script, HttpMessage msg) {
    	if (TYPE_TARGETED.equals(script.getTypeName())) {
			try {
				// Dont need to check if enabled as it can only be invoked manually
				Invocable inv = invokeScript(script);
				TargetedScript s = inv.getInterface(TargetedScript.class);
				
				if (s != null) {
					s.invokeWith(msg);
					
				} else {
					writers.append(Constant.messages.getString("script.interface.targeted.error"));
					this.setError(script, writers.toString());
					this.setEnabled(script, false);
				}
			
			} catch (Exception e) {
				try {
					writers.append(e.toString());
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
			listener.scriptChanged(script);
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
			listener.scriptError(script);
		}
	}

	public void setError(ScriptWrapper script, Exception e) {
		script.setError(true);
		script.setLastException(e);
		
		this.getTreeModel().nodeStructureChanged(script);
		
		for (ScriptEventListener listener : this.listeners) {
			listener.scriptError(script);
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
}
