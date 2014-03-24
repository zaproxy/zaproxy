/*
 * BaseCase.java
 *
 * Copyright 2006 James Fisher
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

import java.net.URL;

/**
 *
 * Used to store information about a base case.
 */


public class BaseCase
{
    //The URL for which the base applies to, eg if we are testing within /wibble/, then this will be set to /wibble/
    private URL baseCaseURL;
    
    //http responce code for the base case
    private int returnCode = 0;
    
    //store of the actual base case if required
    private String baseCase = null;
    
    //was it a dir or a file
    private boolean dir = true;
    
    //URL of the base that was requested
    private URL baseCaseRequestURL;
    
    //used to store the fileextention used
    private String fileExt = null;
    
    /*
     * Store the flag for if we are to use the regex match instead
     */    
    private boolean useRegexInstead = false;
    
    /*
     * the regex to use
     */
    private String regex;
    
    /** Creates a new instance of BaseCase */
    public BaseCase(URL baseCaseURL, int returnCode, boolean dir, URL baseCaseRequestURL, String baseCase, String fileExt, boolean useRegexInstead, String regex)
    {
        this.baseCaseURL = baseCaseURL;
        this.baseCaseRequestURL = baseCaseRequestURL;
        this.returnCode = returnCode;
        this.dir = dir;
        this.baseCase = baseCase;
        this.fileExt = fileExt;
        this.useRegexInstead = useRegexInstead;
        this.regex = regex;
        
    }

    public String getBaseCase()
    {
        return baseCase;
    }

    public URL getBaseCaseRequestURL()
    {
        return baseCaseRequestURL;
    }

    public URL getBaseCaseURL()
    {
        return baseCaseURL;
    }

    public int getFailCode()
    {
        return returnCode;
    }

    public boolean isDir()
    {
        return dir;
    }

    public void setBaseCaseURL(URL baseCaseURL)
    {
        this.baseCaseURL = baseCaseURL;
    }

    public void setDir(boolean dir)
    {
        this.dir = dir;
    }
    
    public boolean useContentAnalysisMode()
    {
        if(baseCase == null || baseCase.equalsIgnoreCase("") || useRegexInstead) 
        {
            return false;
        }
        
        return true;
    }

    public String getFileExt()
    {
        return fileExt;
    }

    public String getRegex()
    {
        return regex;
    }

    public int getReturnCode()
    {
        return returnCode;
    }

    public boolean isUseRegexInstead()
    {
        return useRegexInstead;
    }
    
    
    


    
    
}
