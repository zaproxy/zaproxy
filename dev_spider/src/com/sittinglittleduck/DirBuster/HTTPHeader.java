/*
 * HTTPHeader.java
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
public class HTTPHeader
{
    private String header;
    private String value;
    
    /** Creates a new instance of HTTPHeader */
    public HTTPHeader(String header, String value)
    {
        this.header = header;
        this.value = value;
    }

    public String getHeader()
    {
        return header;
    }

    public String getValue()
    {
        return value;
    }

    public void setHeader(String header)
    {
        this.header = header;
    }

    public void setValue(String value)
    {
        this.value = value;
    } 
}
