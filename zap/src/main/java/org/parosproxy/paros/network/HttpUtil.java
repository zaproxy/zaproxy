/*
 * Created on Jun 14, 2004
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
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2021/09/18 Remove commented code.
// ZAP: 2022/04/10 Deprecated.
package org.parosproxy.paros.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

/**
 * @deprecated (2.12.0) No longer in effective use by core. It will be removed in a following
 *     release.
 */
@Deprecated
public class HttpUtil {

    public static String encodeURI(String uri) throws URISyntaxException {

        String tmp = null;

        tmp = uri.replaceAll(" ", "%20");
        tmp = tmp.replaceAll("<", "%3C");
        tmp = tmp.replaceAll(">", "%3E");
        tmp = tmp.replaceAll("'", "%27");
        tmp = tmp.replaceAll("\\x28", "%28"); // left bracket
        tmp = tmp.replaceAll("\\x29", "%29"); // right bracket
        tmp = tmp.replaceAll("\\x22", "%22"); // double quote

        return tmp;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public static void closeServerSocket(ServerSocket socket) {

        if (socket == null) return;

        try {
            socket.close();
        } catch (Exception e) {
        }
    }

    public static void closeSocket(Socket socket) {

        if (socket == null) return;

        try {
            socket.close();
        } catch (Exception e) {
        }
    }

    public static void closeInputStream(InputStream in) {

        if (in == null) return;

        try {
            in.close();
        } catch (Exception e) {
        }
    }

    public static void closeOutputStream(OutputStream out) {

        if (out == null) return;

        try {
            out.close();
        } catch (Exception e) {
        }
    }
}
