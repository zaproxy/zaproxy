/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy dev team
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class FileFuzzer<P extends Payload>{
	
	private String name = null;
	private File file = null;
	private int length = -1;
	private ArrayList<P> payloads = new ArrayList<>();
	private PayloadFactory<P> factory;
    private static Logger log = Logger.getLogger(FileFuzzer.class);

    public FileFuzzer(String s, PayloadFactory<P> f){
    	this.file = null;
    	this.name = s;
    	this.factory = f;
    }
	public FileFuzzer(File file, PayloadFactory<P> f) {
		this.file = file;
		this.name = file.getName();
		this.factory = f;
		init();
	}
	
	private void init() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			
			String line = in.readLine();
			if(line.startsWith("#<type=\"") && line.endsWith("\">")){
				String type = line.substring(8, (line.length() - 2));
				do  {
					if (line.trim().length() > 0 && ! line.startsWith("#")) {
						payloads.add(factory.createPayload(type, line));
					}
				}
				while((line = in.readLine()) != null);	
			}
			else{
				do  {
					if (line.trim().length() > 0 && ! line.startsWith("#")) {
						payloads.add(factory.createPayload(line));
					}
				}
				while((line = in.readLine()) != null);
			}
			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		length = payloads.size();
	}
	public ArrayList<P> getList(){
		return this.payloads;
	}
	public Iterator<P> getIterator() {
		if (length == -1) {
			init();
		}
		return payloads.iterator();
	}
	
	public int getLength() {
		if (length == -1) {
			init();
		}
		return payloads.size();
	}
	
	public boolean hasNext() {
		return getIterator().hasNext();
	}
	
	public String getFileName() {
		return this.name;
	}
	public void setLength(int maximumValue) {
		this.length = maximumValue;
	}

}
