/*
 * DirToCheck.java
 *
 * Created on June 22, 2007, 11:22 AM
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*
 */

package com.sittinglittleduck.DirBuster;

import java.util.Vector;

/**
 *
 * @author james
 */
public class DirToCheck
{
    String name = "";
    private Vector exts = new Vector(10,10);
    
    /**
     * Creates a new instance of DirToCheck
     */
    public DirToCheck(String name, Vector exts)
    {
        this.name = name;
        this.exts = exts;
    }

    public String getName()
    {
        return name;
    }

    public Vector getExts()
    {
        return exts;
    }  

    public void setExts(Vector exts)
    {
        this.exts = exts;
    }
    

}
