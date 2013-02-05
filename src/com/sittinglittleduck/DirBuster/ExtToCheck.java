/*
 * ExtToCheck.java
 *
 * Created on June 22, 2007, 11:30 AM
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

/**
 *
 * @author james
 */
public class ExtToCheck
{
    public static final String BLANK_EXT = "BLANK";
    private String name;
    private boolean toCheck;
    
    /**
     * Creates a new instance of ExtToCheck
     */
    public ExtToCheck(String name, boolean toCheck)
    {
        this.toCheck = toCheck;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public boolean toCheck()
    {
        return toCheck;
    }

    public void setToCheck(boolean toCheck)
    {
        this.toCheck = toCheck;
    }  
}
