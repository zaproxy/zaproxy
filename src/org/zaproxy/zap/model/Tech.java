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

import org.parosproxy.paros.Constant;

public class Tech implements Comparable<Tech> {

    // Tech hierarchy inspired by this article: http://java.dzone.com/articles/enum-tricks-hierarchical-data
    // even though I've gone with a class instead on an enum;)
    public static final Tech Db = new Tech("Db", "technologies.db");
    public static final Tech MySQL = new Tech(Db, "MySQL");
    public static final Tech PostgreSQL = new Tech(Db, "PostgreSQL");
    public static final Tech MsSQL = new Tech(Db, "Microsoft SQL Server");
    public static final Tech Oracle = new Tech(Db, "Oracle");
    public static final Tech SQLite = new Tech(Db, "SQLite");
    public static final Tech Access = new Tech(Db, "Microsoft Access");
    public static final Tech Firebird = new Tech(Db, "Firebird");
    public static final Tech MaxDB = new Tech(Db, "SAP MaxDB");
    public static final Tech Sybase = new Tech(Db, "Sybase");
    public static final Tech Db2 = new Tech(Db, "IBM DB2");
    public static final Tech HypersonicSQL = new Tech(Db, "HypersonicSQL");

    public static final Tech Lang = new Tech("Language", "technologies.lang");
    public static final Tech ASP = new Tech(Lang, "ASP");
    public static final Tech C= new Tech(Lang, "C");
    public static final Tech PHP = new Tech(Lang, "PHP");
    public static final Tech XML = new Tech(Lang, "XML");

    public static final Tech OS = new Tech("OS", "technologies.os");
    public static final Tech Linux = new Tech(OS, "Linux");
    public static final Tech MacOS = new Tech(OS, "MacOS");
    public static final Tech Windows = new Tech(OS, "Windows");

    public static final Tech SCM = new Tech("SCM", "technologies.scm");
    public static final Tech Git = new Tech(SCM, "Git");
    public static final Tech SVN = new Tech(SCM, "SVN");

    public static final Tech WS = new Tech("WS", "technologies.ws");
    public static final Tech Apache = new Tech(WS, "Apache");
    public static final Tech IIS = new Tech(WS, "IIS");
    public static final Tech Tomcat = new Tech(WS, "Tomcat");

    public static final Tech[] builtInTech = {
        Db, MySQL, PostgreSQL, MsSQL, Oracle, SQLite, Access, Firebird, MaxDB, Sybase, Db2, HypersonicSQL, 
        Lang, ASP, C, PHP, XML,
        OS, Linux, MacOS, Windows,
        SCM, Git, SVN,
        WS, Apache, IIS, Tomcat};

    public static final Tech[] builtInTopLevelTech = {Db, Lang, OS, SCM, WS};

    private Tech parent = null;
    private String name = null;
    private String keyUiName;

    public Tech(String name) {
        this(name, null);
    }

    public Tech(String name, String keyUiName) {
        if (name.indexOf(".") > 0) {
            this.name = name.substring(name.lastIndexOf(".") + 1);
            this.parent = new Tech(name.substring(0, name.lastIndexOf(".")));
            
        } else {
            this.name = name;
        }
        this.keyUiName = keyUiName;
    }

    public Tech(Tech parent, String name) {
        this(parent, name, null);
    }

    public Tech(Tech parent, String name, String keyUiName) {
        this.parent = parent;
        this.name = name;
        this.keyUiName = keyUiName;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return this.name;
            
        } else {
            return parent.toString() + "." + this.name;
        }
    }

    @Override
    public boolean equals(Object tech) {
        if (tech == null) {
            return false;
        }
        
        return this.toString().equals(tech.toString());
    }
    
    @Override
    public int hashCode() {
    	return this.toString().hashCode();
    }

    public boolean is(Tech other) {
        if (other == null) {
            return false;
        }
        
        for (Tech t = this; t != null; t = t.parent) {
            if (other == t) {
                return true;
            }
        }
        
        return false;
    }

    public Tech getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getUiName() {
        if (keyUiName == null) {
            return getName();
        }
        return Constant.messages.getString(keyUiName);
    }

    @Override
    public int compareTo(Tech o) {
        if (o == null) {
            return -1;
        }
        
        return this.toString().compareTo(o.toString());
    }
}
