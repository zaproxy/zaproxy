/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.Constant;

/**
 * 00000 - 09999 : information gathering 10000 - 19999 : default files 20000 - 29999 : 30000 - 39999
 * :
 */
public class Category {
    public static final int INFO_GATHER = 0;
    public static final int BROWSER = 1;
    public static final int SERVER = 2;
    public static final int MISC = 3;
    public static final int INJECTION = 4;

    // ZAP: i18n
    private static String[] names = {
        Constant.messages.getString("scanner.category.info"),
        Constant.messages.getString("scanner.category.browser"),
        Constant.messages.getString("scanner.category.server"),
        Constant.messages.getString("scanner.category.misc"),
        Constant.messages.getString("scanner.category.inject")
    };

    public static String getName(int category) {
        String result = Constant.messages.getString("scanner.category.undef");
        if (category < names.length) {
            result = names[category];
        }
        return result;
    }

    public static int getCategory(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 0;
    }

    public static String[] getAllNames() {
        return names;
    }

    public static int length() {
        return names.length;
    }
}
