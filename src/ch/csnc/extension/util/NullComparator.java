/*
 * NullComparator.java
 *
 * This file is part of WebScarab, an Open Web Application Security
 * Project utility. For details, please see http://www.owasp.org/
 *
 * Copyright (c) 2002 - 2004 Rogan Dawes
 *
 * Please note that this file was originally released under the 
 * GNU General Public License  as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * As of October 2014 Rogan Dawes granted the OWASP ZAP Project permission to 
 * redistribute this code under the Apache License, Version 2.0: 
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
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
