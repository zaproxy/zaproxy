 
/*
 * HeadlessResult.java 
 * 
 * Copyright 2008 James Fisher
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

/**
 *
 * @author james
 */


public class HeadlessResult 
{
    public static final int DIR = 0;
    public static final int FILE = 1;
    public static final int ERROR = 2;
    
    private String found;
    private int responceCode;
    private int type;

    public HeadlessResult(String found, int responceCode, int type)
    {
        this.found = found;
        this.responceCode = responceCode;
        this.type = type;
    }

    public String getFound()
    {
        return found;
    }

    public int getResponceCode()
    {
        return responceCode;
    }

    public int getType()
    {
        return type;
    }
    
    

}
