/*
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package ch.csnc.extension.httpclient;

import java.security.cert.Certificate;

public class AliasCertificate {

	private Certificate certificate;
	private String alias;
	
	AliasCertificate(Certificate certificate, String alias){
		this.setCertificate(certificate);
		this.setAlias(alias);
	}

	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}
	
	public String getName(){
		
		String cn = getCN();
		
		if(cn.length() == 0){
			return getAlias();
		}else{
			return cn + " ["+getAlias()+"]";
		}
		
		
	}
	
    public String getCN() {
        
    	String dn = getCertificate().toString();
    	
    	int i = 0;
        i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }
        //get the remaining DN without CN=
        dn = dn.substring(i + 3);  
        
        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\') {
                break;
            }
        }
        return dn.substring(0, i);
    }
}
	
	

