package org.parosproxy.paros.core.scanner;

/**
 * Abstract Scanner Listener for adding logs into the panel.
 *
 * @author preetkaran20@gmail.com KSASAN
 * @param <T> Any POJO as per the needs of Panel.
 */
public abstract class AbstractScannerListener<T> implements ScannerListener {

    /**
     * provides a hook to update panel.
     *
     * @param msg
     */
    public abstract void log(T msg);
}
