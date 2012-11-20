/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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
package org.zaproxy.zap.control;

import java.io.File;

public class AddOn {
	public enum Status {alpha, beta, weekly, release}
	private String name;
	private int version;
	private Status status;
	private File file;
	
	public static boolean isAddOn(File f) {
		if (! f.exists()) {
			return false;
		}
		String fileName = f.getName();
		// TODO HACK
		if (! fileName.startsWith("zap-ext-") || ! fileName.endsWith(".zap")) {
			return false;
		}
		if (fileName.substring(0, fileName.indexOf(".")).split("-").length < 5) {
			return false;
		}
		return true;
	}
	
	public AddOn(File file) throws Exception {
		if (! isAddOn(file)) {
			throw new Exception("Invalid ZAP add-on file " + file.getAbsolutePath());
		}
		this.file = file;
		String fileName = file.getName();
		// Format is zap-ext-<name>-<status>-<version>.zap
		String[] strArray = fileName.substring(0, fileName.indexOf(".")).split("-");
		this.name = strArray[2];
		this.status = Status.valueOf(strArray[3]);
		this.version = Integer.parseInt(strArray[4]);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public boolean isSameAddOn(AddOn addOn) {
		return this.getName().equals(addOn.getName());
	}

	public boolean isUpdateTo(AddOn addOn) throws Exception {
		if (! this.isSameAddOn(addOn)) {
			throw new Exception("Different addons: " + this.getName() + " != " + addOn.getName());
		}
		if (this.getVersion() > addOn.getVersion()) {
			return true;
		}
		return this.getStatus().ordinal() > addOn.getStatus().ordinal();
	}

	public static void main(String[] args) throws Exception {
		File f1 = new File("zap-ext-test-alpha-1.zap");
		File f2 = new File("zap-ext-test-alpha-2.zap");
		File f3 = new File("zap-ext-test-beta-2.zap");
		File f4 = new File("zap-ext-testy-alpha-1.zap");
		
		AddOn addOn1 = new AddOn(f1);
		AddOn addOn2 = new AddOn(f2);
		AddOn addOn3 = new AddOn(f3);
		AddOn addOn4 = new AddOn(f4);
		System.out.println("Name = " + addOn1.getName());
		System.out.println("Status = " + addOn1.getStatus());
		System.out.println("Version = " + addOn1.getVersion());

		System.out.println("a2 update to a1 " + addOn2.isUpdateTo(addOn1));
		System.out.println("a1 NOT update to a2 " + addOn1.isUpdateTo(addOn2));
		System.out.println("a3 update to a1 " + addOn3.isUpdateTo(addOn1));
		System.out.println("a3 update to a2 " + addOn3.isUpdateTo(addOn2));
		// Should throw exception
		System.out.println("a4 update to a2 " + addOn4.isUpdateTo(addOn2));
	}

}
