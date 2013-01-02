// CHECKSTYLE_OFF
// Copied verbatim (other than these lines) from jetty-util trunk @r1179.
package org.zaproxy.zap.extension.websocket.utility;


/* ------------------------------------------------------------ */
/** UTF-8 StringBuilder.
 *
 * This class wraps a standard {@link java.lang.StringBuffer} and provides methods to append 
 * UTF-8 encoded bytes, that are converted into characters.
 * 
 * This class is stateful and up to 6  calls to {@link #append(byte)} may be needed before 
 * state a character is appended to the string buffer.
 * 
 * The UTF-8 decoding is done by this class and no additional buffers or Readers are used.
 * The UTF-8 code was inspired by http://javolution.org
 * 
 */
class Utf8StringBuilder 
{
    StringBuilder _buffer;
    int _more;
    int _bits;
    
    public Utf8StringBuilder()
    {
        _buffer=new StringBuilder();
    }
    
    public Utf8StringBuilder(int capacity)
    {
        _buffer=new StringBuilder(capacity);
    }

    public void append(byte[] b,int offset, int length)
    {
        int end=offset+length;
        for (int i=offset; i<end;i++) {
            append(b[i]);
        }
    }
    
    public void append(byte b)
    {
        if (b>=0)
        {
            if (_more>0)
            {
                _buffer.append('?');
                _more=0;
                _bits=0;
            }
            else
            {
                _buffer.append((char)(0x7f&b));
            }
        }
        else if (_more==0)
        {
            if ((b&0xc0)!=0xc0)
            {
                // 10xxxxxx
                _buffer.append('?');
                _more=0;
                _bits=0;
            }
            else
            { 
                if ((b & 0xe0) == 0xc0)
                {
                    //110xxxxx
                    _more=1;
                    _bits=b&0x1f;
                }
                else if ((b & 0xf0) == 0xe0)
                {
                    //1110xxxx
                    _more=2;
                    _bits=b&0x0f;
                }
                else if ((b & 0xf8) == 0xf0)
                {
                    //11110xxx
                    _more=3;
                    _bits=b&0x07;
                }
                else if ((b & 0xfc) == 0xf8)
                {
                    //111110xx
                    _more=4;
                    _bits=b&0x03;
                }
                else if ((b & 0xfe) == 0xfc) 
                {
                    //1111110x
                    _more=5;
                    _bits=b&0x01;
                }
                else
                {
                    throw new IllegalArgumentException("!utf8");
                }
            }
        }
        else
        {
            if ((b&0xc0)==0xc0)
            {    // 11??????
                _buffer.append('?');
                _more=0;
                _bits=0;
                throw new IllegalArgumentException("!utf8");
            }
            
            // 10xxxxxx
            _bits=(_bits<<6)|(b&0x3f);
            if (--_more==0) {
                _buffer.append((char)_bits);
            }
        }
    }
    
    public int length()
    {
        return _buffer.length();
    }
    
    public void reset()
    {
        _buffer.setLength(0);
        _more=0;
        _bits=0;
    }
    
    public StringBuilder getStringBuilder()
    {
        if (_more!=0) {
            throw new IllegalStateException("!utf8");
        }
        return _buffer;
    }
    
    @Override
    public String toString()
    {
        if (_more!=0) {
            throw new IllegalStateException("!utf8");
        }
        return _buffer.toString();
    }
}
