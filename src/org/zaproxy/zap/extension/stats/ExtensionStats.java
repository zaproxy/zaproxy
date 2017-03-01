/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 ZAP development team
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
package org.zaproxy.zap.extension.stats;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.zap.utils.Stats;

public class ExtensionStats extends ExtensionAdaptor implements OptionsChangedListener {

	public static final String NAME = "ExtensionStats"; 
	
	private InMemoryStats inMemStats;
	private Statsd statsd;
	private OptionsStatsPanel optionsStatsPanel;
	private StatsParam statsParam;

    private static final Logger LOG = Logger.getLogger(ExtensionStats.class);

	public ExtensionStats() {
		super();
		initialize();
	}

	private void initialize() {
        this.setName(NAME);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

		extensionHook.addOptionsChangedListener(this);
		extensionHook.addOptionsParamSet(getStatsParam());

	    if (getView() != null) {
	        extensionHook.getHookView().addOptionPanel(getOptionsStatsPanel());
	    }
	    
	    StatsAPI statsAPI = new StatsAPI(this);
	    extensionHook.addApiImplementor(statsAPI);
	    statsAPI.addApiOptions(getStatsParam());

	}

	@Override
	public void optionsLoaded() {
		optionsChanged();
	}

	private OptionsStatsPanel getOptionsStatsPanel() {
		if (optionsStatsPanel == null) {
			optionsStatsPanel = new OptionsStatsPanel();
		}
		return optionsStatsPanel;
	}
	
	private StatsParam getStatsParam() {
		if (statsParam == null) {
			statsParam = new StatsParam();
		}
		return statsParam;
	}
	
	@Override
	public boolean isCore() {
		return true;
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("stats.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }

	@Override
	public void optionsChanged(OptionsParam optionsParam) {
		optionsChanged();
	}
	
	private void optionsChanged() {
		boolean inMemStatsInit = inMemStats != null;
		
        if (inMemStatsInit != this.getStatsParam().isInMemoryEnabled()) {
        	// Somethings changed
        	if (!inMemStatsInit) {
            	LOG.info("Start recording in memory stats");
            	inMemStats = new InMemoryStats();
            	Stats.addListener(inMemStats);
        	} else {
            	LOG.info("Stop recording in memory stats");
            	Stats.removeListener(inMemStats);
            	inMemStats.allCleared();
            	inMemStats = null;
        	}
        }

		boolean statsdInit = statsd != null;
		if (statsdInit != this.getStatsParam().isStatsdEnabled()) {
        	// Somethings changed
        	if (! statsdInit) {
            	LOG.info("Start sending stats to statsd server");
            	try {
    				statsd = newStatsD(this.getStatsParam());
    	        	Stats.addListener(statsd);
    			} catch (Exception e) {
    				LOG.error(e.getMessage(), e);
    			}
        	} else {
            	LOG.info("Stop sending stats to statsd server");
            	Stats.removeListener(statsd);
            	statsd = null;
        	}
        } else if (statsdInit) {
        	if (! StringUtils.equals(this.getStatsParam().getStatsdHost(), statsd.getHost()) ||
        			this.getStatsParam().getStatsdPort() != statsd.getPort()) {
        		// Have to re-initialise it
            	LOG.info("Restart sending stats to statsd server");
            	try {
                	Stats.removeListener(statsd);
    				statsd = newStatsD(this.getStatsParam());
    	        	Stats.addListener(statsd);
    			} catch (Exception e) {
    				LOG.error(e.getMessage(), e);
    			}
        	} else if (! StringUtils.equals(this.getStatsParam().getStatsdPrefix(), statsd.getPrefix())) {
        		statsd.setPrefix(this.getStatsParam().getStatsdPrefix());
        	}
        }
	}
	
	private Statsd newStatsD (StatsParam param) throws UnknownHostException, IOException {
		return new Statsd(
				param.getStatsdHost(),
				param.getStatsdPort(),
				param.getStatsdPrefix());
		
	}
	
	public InMemoryStats getInMemoryStats() {
		return this.inMemStats;
	}
}
