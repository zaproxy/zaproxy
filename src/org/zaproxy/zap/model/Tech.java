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

public class Tech implements Comparable<Tech> {

    // Tech hierarchy inspired by this article: http://java.dzone.com/articles/enum-tricks-hierarchical-data
    // even though I've gone with a class instead on an enum;)
    public static final Tech Db = new Tech("Db");
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

    public static final Tech OS = new Tech("OS");
    public static final Tech Linux = new Tech(OS, "Linux");
    public static final Tech MacOS = new Tech(OS, "MacOS");
    public static final Tech Windows = new Tech(OS, "Windows");

    public static final Tech WS = new Tech("WS");
    public static final Tech Apache = new Tech(WS, "Apache");
    public static final Tech IIS = new Tech(WS, "IIS");
    public static final Tech Tomcat = new Tech(WS, "Tomcat");

    public static final Tech[] builtInTech = {
        Db, MySQL, PostgreSQL, MsSQL, Oracle, SQLite, Access, Firebird, MaxDB, Sybase, Db2, HypersonicSQL, 
        OS, Linux, MacOS, Windows,
        WS, Apache, IIS, Tomcat};

    public static final Tech[] builtInTopLevelTech = {Db, OS, WS};

    private Tech parent = null;
    private String name = null;

    public Tech(String name) {
        if (name.indexOf(".") > 0) {
            this.name = name.substring(name.lastIndexOf(".") + 1);
            this.parent = new Tech(name.substring(0, name.lastIndexOf(".")));
            
        } else {
            this.name = name;
        }
    }

    public Tech(Tech parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return this.name;
            
        } else {
            return parent.toString() + "." + this.name;
        }
    }

    public boolean equals(Tech tech) {
        if (tech == null) {
            return false;
        }
        
        return this.toString().equals(tech.toString());
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

    @Override
    public int compareTo(Tech o) {
        if (o == null) {
            return -1;
        }
        
        return this.toString().compareTo(o.toString());
    }
}
