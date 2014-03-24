/*
 * NullComparator.java
 *
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

package ch.csnc.extension.util;

import java.util.Comparator;

/**
 *
 * @author rdawes
 */
public class NullComparator implements Comparator {
    
    public NullComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null && o2 != null) return 1;
        if (o1 != null && o2 == null) return -1;
        if (o1 instanceof Comparable) return ((Comparable)o1).compareTo(o2);
        throw new ClassCastException("Incomparable objects " + o1.getClass().getName() + " and " + o2.getClass().getName());
    }
    
}
