package org.zaproxy.zap.extension.multiFuzz;

public interface PayloadFactory<P extends Payload>{
 
    boolean isSupported(String type);
 
    P createPayload(String data);
 
    P createPayload(String type, String data);
}
