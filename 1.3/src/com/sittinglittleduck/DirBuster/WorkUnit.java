/*
 * WorkUnit.java
 *
 * Created on 11 November 2005, 20:34
 *
 * Copyright 2007 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.sittinglittleduck.DirBuster;
import java.net.*;

public class WorkUnit
{
    private URL urlToGet;
    private boolean isDir;
    private String method;
    //private String basecase = null;
    //private boolean comparebase = false;
    
    //information about the basecase used for this test.
    private BaseCase baseCaseObj;
    
    //the item used to produce the check
    private String itemToCheck;
    /** Creates a new instance of WorkUnit */
    public WorkUnit(URL url, boolean isDir, String method, BaseCase baseCaseObj, String itemToCheck)
    {
        urlToGet = url;
        this.isDir = isDir;
        this.method = method;
        this.baseCaseObj = baseCaseObj;
        this.itemToCheck = itemToCheck;
    }
    
    
    public URL getWork()
    {
        return urlToGet;
    }
    
    public boolean isDir()
    {
        return isDir;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    //public String getBasecase()
    //{
    //    return basecase;
    //}
    
    //public boolean compareBasecase()
    //{
    //    return comparebase;
    //}

    public BaseCase getBaseCaseObj()
    {
        return baseCaseObj;
    }

    public String getItemToCheck()
    {
        return itemToCheck;
    }
    
    

}
