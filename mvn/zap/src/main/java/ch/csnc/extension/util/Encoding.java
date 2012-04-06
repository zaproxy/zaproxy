/***********************************************************************
 *
 * $CVSHeader$
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
 *
 * Getting Source
 * ==============
 *
 * Source for this application is maintained at Sourceforge.net, a
 * repository for free software projects.
 * 
 * For details, please see http://www.sourceforge.net/projects/owasp
 *
 */

/*
 * Copyright (c) 2002,2003 Free Software Foundation
 * developed under the custody of the
 * Open Web Application Security Project
 * (http://www.owasp.org)
 *
 * This file is part of the OWASP common library (OCL).
 * OCL is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * OCL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * The valid license text for this file can be retrieved with
 * the call:   java -cp owasp.jar org.owasp.LICENSE
 *
 * If you are not able to view the LICENSE that way, which should
 * always be possible within a valid and working OCL release,
 * please write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * to get a copy of the GNU General Public License or to report a
 * possible license violation.
 */
package ch.csnc.extension.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Utilities to (de-)code data.
 *
 * @since 0.1
 * @version 0.2rc<br />CVS $Revision: 1.4 $ $Author: rogan $
 * @author <a href="mailto:ingo@ingostruck.de">ingo@ingostruck.de</a>
 */
public final class Encoding {
    
    /** Avoids instantiation */
    private Encoding() {}
    /** Map for base64 encoding */
    private static byte[] _base64en;
    /** Map for base64 decoding */
    private static byte[] _base64de;
    /** invalid char on base64 decoding */
    private static final byte B64INV = (byte) 0x80;
    
    /** Initializes the base64 de-/encoding maps according to RFC 1341, 5.2 */
    static {
        _base64en = new byte[65];
        _base64de = new byte[256];
        Arrays.fill( _base64de, B64INV );
        for ( byte i = 0; i < 26; i++ ) {
            _base64en[ i ] = (byte) (65 + i);
            _base64en[ 26 + i ] = (byte) (97 + i);
            _base64de[ 65 + i ] = i;
            _base64de[ 97 + i ] = (byte) (26 + i);
        }
        for ( byte i = 48; i < 58; i++ ) {
            _base64en[ 4 + i ] = i;
            _base64de[ i ] = (byte) (4 + i);
        }
        _base64en[ 62 ] = 43;
        _base64en[ 63 ] = 47;
        _base64en[ 64 ] = 61;
        _base64de[ 43 ] = 62;
        _base64de[ 47 ] = 63;
        _base64de[ 61 ] = 0; // sic!
    }
    
    /**
     * Encodes a byte array to a Base64 encoded String.
     * <br />(cf. RFC 1341 section  5.2)
     * This implementation heavily outperforms sun.misc.BASE64Encoder, which is
     * not "officially" available anyway (about four times faster on 1.3 and
     * about double speed on 1.4).
     * @param code the byte code to be encoded
     * @return the Base64 encoded String representing the plain bytecode
     */
    public static String base64encode( byte[] code ) {
        if ( null == code )
            return null;
        if ( 0 == code.length )
            return new String();
        int len = code.length;
        // remainder of the encoding process
        int rem = len % 3;
        // size of the destination byte array
        byte[] dst = new byte[4 + (((len - 1) / 3) << 2) + (len / 57)];
        // actual column of the destination string;
        // RFC 1341 requires a linefeed every 58 data bytes
        int column = 0;
        // position within source
        int spos = 0;
        // position within destination
        int dpos = 0;
        // adjust length for loop (remainder is treated separately)
        len -= 2;
        // using a while loop here since spos may be needed for the remainder
        while ( spos < len ) {
            byte b0 = code[ spos ];
            byte b1 = code[ spos + 1 ];
            byte b2 = code[ spos + 2 ];
            dst[ dpos++ ] = _base64en[ 0x3f & (b0 >>> 2) ];
            dst[ dpos++ ] = _base64en[ (0x30 & (b0 << 4)) + (0x0f & (b1 >>> 4)) ];
            dst[ dpos++ ] = _base64en[ (0x3c & (b1 << 2)) + (0x03 & (b2 >>> 6)) ];
            dst[ dpos++ ] = _base64en[ 0x3f & b2 ];
            spos += 3;
            column += 3;
            if ( 57 == column ) {
                dst[ dpos++ ] = 10;
                column = 0;
            }
        }
        // there may be a remainder to be processed
        if ( 0 != rem ) {
            byte b0 = code[ spos ];
            dst[ dpos++ ] = _base64en[ 0x3f & (b0 >>> 2) ];
            if ( 1 == rem ) {
                // one-byte remainder
                dst[ dpos++ ] = _base64en[ 0x30 & (b0 << 4) ];
                dst[ dpos++ ] = 61;
            } else {
                // two-byte remainder
                byte b1 = code[ spos + 1 ];
                dst[ dpos++ ] = _base64en[ (0x30 & (b0 << 4)) + (0x0f & (b1 >>> 4)) ];
                dst[ dpos++ ] = _base64en[ 0x3c & (b1 << 2) ];
            }
            dst[ dpos++ ] = 61;
        }
        // using any default encoding is possible, since the base64 char subset is
        // identically represented in all ISO encodings, including US-ASCII
        return new String( dst );
    }
    
    /**
     * Decodes a Base64 encoded String.
     * <br />(cf. RFC 1341 section  5.2)
     * NOTE: This decoder silently ignores all legal line breaks in the input and
     * throws a RuntimeException on any illegal input.
     * This impl slightly outperforms sun's decoder on 1.3 and heavily outperforms
     * it on 1.4 (about a third faster).
     *
     * @param coded the string to be decoded
     * @return the plain bytecode represented by the Base64 encoded String
     */
    public static byte[] base64decode( String coded ) {
        if ( null == coded )
            return null;
        byte[] src = coded.getBytes();
        int len = src.length;
        int dlen = len - (len/77);
        dlen = (dlen >>> 2) + (dlen >>> 1);
        int rem = 0;
        if ( 61 == src[ len - 1 ] )
            rem++;
        if ( 61 == src[ len - 2 ] )
            rem++;
        dlen -= rem;
        byte[] dst = new byte[ dlen ];
        
        int pos = 0;
        int dpos = 0;
        int col = 0;
        // adjust for remainder
        len -= 4;
        
        while ( pos < len ) {
            byte b0 = _base64de[ src[ pos++ ] ];
            byte b1 = _base64de[ src[ pos++ ] ];
            byte b2 = _base64de[ src[ pos++ ] ];
            byte b3 = _base64de[ src[ pos++ ] ];
            
            if ( B64INV == b0 || B64INV == b1 || B64INV == b2 || B64INV == b3 )
                throw new RuntimeException( "Invalid character at or around position " + pos );
            
            dst[ dpos++ ] = (byte) ((b0 << 2) | ((b1 >>> 4) & 0x03));
            dst[ dpos++ ] = (byte) ((b1 << 4) | ((b2 >>> 2) & 0x0f));
            dst[ dpos++ ] = (byte) ((b2 << 6) | (b3 & 0x3f));
            col += 4;
            // skip linefeed which is only allowed here; if at that pos is any
            // other char then the input is goofed and we throw an
            // exception
            if ( 76 == col ) {
                if ( 10 != src[ pos++ ] )
                    throw new RuntimeException( "No linefeed found at position " + (pos - 1 ) );
                col = 0;
            }
        }
        
        // process the remainder
        byte b0 = _base64de[ src[ pos++ ] ];
        byte b1 = _base64de[ src[ pos++ ] ];
        byte b2 = _base64de[ src[ pos++ ] ];
        byte b3 = _base64de[ src[ pos++ ] ];
        if ( B64INV == b0 || B64INV == b1 || B64INV == b2 || B64INV == b3 )
            throw new RuntimeException( "Invalid character at or around position " + pos );
        
        dst[ dpos++ ] = (byte) ((b0 << 2) | ((b1 >>> 4) & 0x03));
        if ( 2 == rem )
            return dst;
        dst[ dpos++ ] = (byte) ((b1 << 4) | ((b2 >>> 2) & 0x0f));
        if ( 1 == rem )
            return dst;
        dst[ dpos++ ] = (byte) ((b2 << 6) | (b3 & 0x3f));
        
        return dst;
    }
    
    /**
     * Converts a byte array into a hexadecimal String.
     * @param b a byte array to be converted
     * @return a hexadecimal (lower-case-based) String representation of the
     * byte array
     */
    public static String toHexString( byte[] b ) {
        if ( null == b )
            return null;
        int len = b.length;
        byte[] hex = new byte[ len << 1 ];
        for ( int i = 0, j = 0; i < len; i++, j+=2 ) {
            hex[ j ] = (byte) ((b[ i ] & 0xF0) >> 4);
            hex[ j ] += 10 > hex[ j ] ? 48 : 87;
            hex[ j + 1 ] = (byte) (b[ i ] & 0x0F);
            hex[ j + 1 ] += 10 > hex[ j + 1 ] ? 48 : 87;
        }
        return new String( hex );
    }

    /**
     *  Returns the MD5 hash of a String.
     *
     *@param  str  Description of the Parameter
     *@return      Description of the Return Value
     */
    public static String hashMD5( String str ) {
        return hashMD5(str.getBytes());
    }
    
    public static String hashMD5( byte[] bytes ) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( "MD5" );
            md.update( bytes );
        }
        catch ( NoSuchAlgorithmException e ) {
            e.printStackTrace();
            // it's got to be there
        }
        return toHexString( md.digest() );
    }
    
    
    
    /**
     *  Returns the SHA hash of a String.
     *
     *@param  str  Description of the Parameter
     *@return      Description of the Return Value
     */
    public static String hashSHA( String str ) {
        byte[] b = str.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance( "SHA1" );
            md.update( b );
        }
        catch ( NoSuchAlgorithmException e ) {
            e.printStackTrace();
            // it's got to be there
        }
        return toHexString( md.digest() );
    }
    
    /**
     *  Description of the Method
     *
     *@param  input  Description of the Parameter
     *@return        Description of the Return Value
     */
    public static synchronized String rot13( String input ) {
        StringBuffer output = new StringBuffer();
        if ( input != null ) {
            for ( int i = 0; i < input.length(); i++ ) {
                char inChar = input.charAt( i );
                if ( ( inChar >= 'A' ) & ( inChar <= 'Z' ) ) {
                    inChar += 13;
                    if ( inChar > 'Z' ) {
                        inChar -= 26;
                    }
                }
                if ( ( inChar >= 'a' ) & ( inChar <= 'z' ) ) {
                    inChar += 13;
                    if ( inChar > 'z' ) {
                        inChar -= 26;
                    }
                }
                output.append( inChar );
            }
        }
        return output.toString();
    }
    /**
     *  Description of the Method
     *
     *@param  str  Description of the Parameter
     *@return      Description of the Return Value
     */
    public static String urlDecode( String str ) {
        try {
            return ( URLDecoder.decode( str, "utf-8" ) );
        }
        catch ( Exception e ) {
            return ( "Decoding error" );
        }
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  str  Description of the Parameter
     *@return      Description of the Return Value
     */
    public static String urlEncode( String str ) {
        try {
            return ( URLEncoder.encode( str, "utf-8" ) );
        }
        catch ( Exception e ) {
            return ( "Encoding error" );
        }
    }
    

} // class Base64

