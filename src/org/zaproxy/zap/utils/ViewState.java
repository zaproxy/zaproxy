/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.utils;

import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.parosproxy.paros.extension.encoder.Base64;

/**
 * THIS CODE IS FROM THE PROJECT LOCATED AT http://code.google.com/p/embeddednode/ AND THE RIGHT HAS BEEN GRANTED BY HIS OWNED TO BE USED WITHIN THE ZAP PROJECT
 * 
 * <p>Make a implemented java.io.Serializable object to encode base64 string and vice versa.</p>
 * For example:
 * [code]
 * public static void main(String[] argv) { 
 *      class Bicycle implements Serializable {
 *              String manufacturer;
 *              float  price;
 *              byte   level;
 *              Bicycle(String manufacturer, float price, byte level) {
 *                      this.price        = price;
 *                      this.manufacturer = manufacturer;
 *                      this.level        = level;
 *              }
 *      }
 * 
 *      String  base64  = ViewState.encode(new Bicycle("Giant", (float)0x000000FF, (byte)0x0000000A)); //encode
 *      Bicycle bicycle = ViewState.decode(base64); //decode
 * }
 * [/code]
 * @author embeddednode
 * @version 1.2 June 10, 2009
 */

public class ViewState {
        
        /** 
         * encode a object to a base64 string
         * @param o is a implemented java.io.Serializable object
         * @return the encoded object
         */
        public static String encode(Serializable o) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
            try {
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        try {
                                oos.writeObject(o);
                                oos.flush();
                        } finally {
                                oos.close();
                        }
                        return Base64.encodeBytes(bos.toByteArray());
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }
        
        /**
         * decode a base64 string to a object
         * @param <T> the type of the object
         * @param base64 a encoded string by ViewState.encode method
         * @return the decoded object
         */
        @SuppressWarnings("unchecked")
        public static <T> T decode(String base64) {
                //BASE64Decoder decoder = new BASE64Decoder();
                try {
                        //byte[]               b    = decoder.decodeBuffer(base64);
                		byte[]               b    = Base64.decode(base64);
                        ByteArrayInputStream bais = new ByteArrayInputStream(b);
                        ObjectInputStream    ois  = new ObjectInputStream(bais);
                        return (T)ois.readObject();
                } catch (IOException e) {
                        throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                }
        }
        
}
