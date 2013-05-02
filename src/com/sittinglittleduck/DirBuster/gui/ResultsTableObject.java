/*
 * ResultsTableObject.java
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

package com.sittinglittleduck.DirBuster.gui;

import java.util.Vector;

import com.sittinglittleduck.DirBuster.BaseCase;
import com.sittinglittleduck.DirBuster.DirToCheck;


public class ResultsTableObject
{
    private String fieldType = "";
    private String fieldFound = "";
    private String fieldResponceCode = "";
    private String fieldStatus = "";
    private String fullURL = "";
    private String responce = "";
    private String baseCase = "";
    private String rawResponce = "";
    
    private Boolean scanFurther = Boolean.FALSE;
    
    private BaseCase baseCaseObj = null;
    
    private Vector test = new Vector(10,5);
    
    private DirToCheck dirToCheck;
    
    /** Creates a new instance of ResultsTableObject */
    /*
    public ResultsTableObject()
    {
    }
     
     */
    
    
    public ResultsTableObject(String type, String found, String responceCode, String status, String fullURL, BaseCase baseCaseObj)
    {
        this.fieldType = type;
        this.fieldFound = found;
        this.fieldResponceCode = responceCode;
        this.fieldStatus = status;
        this.fullURL = fullURL;
        this.baseCaseObj = baseCaseObj;
        
        
    }
    
    
    public ResultsTableObject(String type, String found, String responceCode, String status, String fullURL, String Responce, String BaseCase, String rawResponce, boolean scanFurther, BaseCase baseCaseObj)
    {
        this.fieldType = type;
        this.fieldFound = found;
        this.fieldResponceCode = responceCode;
        this.fieldStatus = status;
        this.fullURL = fullURL;
        this.baseCase = BaseCase;
        this.responce = Responce;
        this.rawResponce = rawResponce;
        if(scanFurther)
        {
            this.scanFurther = Boolean.TRUE;
        }
        else
        {
            this.scanFurther = Boolean.FALSE;
        }
        this.baseCaseObj = baseCaseObj;
        
        
    }
    
    
    public String getFieldType()
    { return fieldType; }
    public String getFieldFound()
    { return fieldFound; }
    public String getFieldResponceCode()
    { return fieldResponceCode; }
    public String getFieldStatus()
    { return fieldStatus; }
    public String getFullURL()
    { return fullURL; }
    public String getBaseCase()
    { return baseCase; }
    public String getResponce()
    { return responce; }
    public BaseCase getBaseCaseObj()
    { return baseCaseObj; }
    
    public void setFieldType(String f1)
    { fieldType = f1; }
    public void setFieldFound(String f2)
    { fieldFound = f2; }
    public void setFieldResponceCode(String f3)
    {fieldResponceCode = f3; }
    public void setFieldStatus(String f4)
    {fieldStatus = f4; }

    public String getRawResponce()
    {
        return rawResponce;
    }

    public boolean isScanFurther()
    {
        return scanFurther;
    }

    public void setScanFurther(Boolean scanFurther)
    {
        this.scanFurther = scanFurther;
    }

    public DirToCheck getDirToCheck()
    {
        return dirToCheck;
    }
    
}
