package org.parosproxy.paros.core.scanner;

import java.util.List;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.ascan.VariantFactory;

/**
 * {@code AbstractAppVariantPlugin} is the abstract base class which is used to run per variant to
 * modify multiple name value pairs of the {@code HttpMessage} per variant.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public abstract class AbstractAppVariantPlugin extends AbstractAppPlugin {

    private final Logger logger = Logger.getLogger(this.getClass());
    private Variant variant = null;

    @Override
    public void scan() {
        VariantFactory factory = Model.getSingleton().getVariantFactory();

        List<Variant> listVariant =
                factory.createVariants(this.getParent().getScannerParam(), this.getBaseMsg());

        if (listVariant.isEmpty()) {
            getParent()
                    .pluginSkipped(
                            this,
                            Constant.messages.getString(
                                    "ascan.progress.label.skipped.reason.noinputvectors"));
            return;
        }

        for (int i = 0; i < listVariant.size() && !isStop(); i++) {

            HttpMessage msg = getNewMsg();
            // ZAP: Removed unnecessary cast.
            variant = listVariant.get(i);
            try {
                variant.setMessage(msg);
                scanVariant();

            } catch (Exception e) {
                logger.error(
                        "Error occurred while scanning with variant "
                                + variant.getClass().getCanonicalName(),
                        e);
            }

            // ZAP: Implement pause and resume
            while (getParent().isPaused() && !isStop()) {
                Util.sleep(500);
            }
        }
    }

    /** Scan the current message using the current Variant */
    private void scanVariant() {
        HttpMessage msg = getNewMsg();
        try {
            scan(msg, variant);
        } catch (Exception e) {
            logger.error("Error occurred while scanning a message:", e);
        }
    }

    /**
     * Plugin method that need to be implemented for the specific test. The passed message is a copy
     * which maintains only the Request's information so if the plugin need to manage the original
     * Response body a getBaseMsg() call should be done. the param name and the value are the
     * original value retrieved by the crawler and the current applied Variant.
     *
     * @param msg a copy of the HTTP message currently under scanning
     * @param variant
     */
    public abstract void scan(HttpMessage msg, Variant variant);

    /**
     * Sets the parameter into the given {@code message}. If both parameter name and value are
     * {@code null}, the parameter will be removed.
     *
     * @param message the message that will be changed
     * @param originalPair original name value pair
     * @param param the name of the parameter
     * @param value the value of the parameter
     * @return the parameter set
     * @see #setEscapedParameter(HttpMessage, NameValuePair, String, String)
     */
    public String setParameter(
            HttpMessage message, NameValuePair originalPair, String param, String value) {
        return variant.setParameter(message, originalPair, param, value);
    }

    /**
     * Sets the parameter into the given {@code message}. If both parameter name and value are
     * {@code null}, the parameter will be removed.
     *
     * <p>The value is expected to be properly encoded/escaped.
     *
     * @param message the message that will be changed
     * @param originalPair original name value pair
     * @param param the name of the parameter
     * @param value the value of the parameter
     * @return the parameter set
     * @see #setParameter(HttpMessage,NameValuePair, String, String)
     */
    public String setEscapedParameter(
            HttpMessage message, NameValuePair originalPair, String param, String value) {
        return variant.setEscapedParameter(message, originalPair, param, value);
    }
}
