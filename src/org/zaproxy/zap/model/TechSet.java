/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.TreeSet;

public class TechSet {
	
	public static final TechSet AllTech = new TechSet(Tech.builtInTopLevelTech);
	
	private TreeSet<Tech> includeTech = new TreeSet<>();
	private TreeSet<Tech> excludeTech = new TreeSet<>();

	public TechSet () {
	}
	
	public TechSet (Tech[] include) {
		this (include, (Tech[])null);
	}
	
	public TechSet (Tech[] include, Tech[] exclude) {
		if (include != null) {
			for (Tech tech : include) {
				this.include(tech);
			}
		}
		if (exclude != null) {
			for (Tech tech : exclude) {
				this.exclude(tech);
			}
		}
	}
	
	public TechSet(TechSet techSet){
		this.includeTech.addAll(techSet.includeTech);
		this.excludeTech.addAll(techSet.excludeTech);
	}
	
	public void include(Tech tech) {
		excludeTech.remove(tech);
		includeTech.add(tech);
	}
	
	public void exclude(Tech tech) {
		includeTech.remove(tech);
		excludeTech.add(tech);
	}
	
	public boolean includes(Tech tech) {
		if (tech == null) {
			return false;
		}
		if (excludeTech.contains(tech)) {
			return false;
		} else if (includeTech.contains(tech)) {
			return true;
		} else {
			return this.includes(tech.getParent());
		}
	}
	
	public TreeSet<Tech> getIncludeTech() {
		TreeSet<Tech> copy = new TreeSet<>();
		copy.addAll(this.includeTech);
		return copy;
	}
	
	public TreeSet<Tech> getExcludeTech() {
		TreeSet<Tech> copy = new TreeSet<>();
		copy.addAll(this.excludeTech);
		return copy;
	}
	
	// Useful for debuging ;)
	public void print() {
		System.out.println("TechSet: " + this.hashCode());
		for (Tech tech : includeTech) {
			System.out.println("\tInclude: " + tech);
		}
		for (Tech tech : excludeTech) {
			System.out.println("\tExclude: " + tech);
		}
		
	}
}
