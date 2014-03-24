/*
 * HTMLparseWorkUnit.java
 *
 * Created on July 3, 2007, 3:22 PM
 *
 *Copyright 2007 James Fisher
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


public class HTMLparseWorkUnit
{
    private String htmlToParse;
    private WorkUnit workUnit;
    
    /** Creates a new instance of HTMLparseWorkUnit */
    public HTMLparseWorkUnit(String htmlToParse, WorkUnit workUnit)
    {
        this.workUnit = workUnit;
        this.htmlToParse = htmlToParse;
    }

    public String getHtmlToParse()
    {
        return htmlToParse;
    }

    public WorkUnit getWorkUnit()
    {
        return workUnit;
    }
    

    
}
