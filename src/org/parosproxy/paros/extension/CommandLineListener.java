/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2005 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/12/19 Code Cleanup: Moved array brackets from variable name to type
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Removed redundant public modifiers from interface method declarations
// ZAP: 2013/12/03 Issue 934: Handle files on the command line via extension

package org.parosproxy.paros.extension;

import java.io.File;
import java.util.List;


public interface CommandLineListener {
    /**
     * execute the command line using the argument provided.
     */
    void execute(CommandLineArgument[] args);
    
    /**
     * Handle the specified file (in whatever way is appropriate).
     * This will only be called for files specified on the command line without switches 
     * and which match one of the extensions returned by getHandledExtensions() 
     * @param file
     * @return true if the listener handled the file, false otherwise
     */
    boolean handleFile (File file);
    
    /**
     * Get the list of extensions this listener can handle
     * @return
     */
    List<String> getHandledExtensions();

}
