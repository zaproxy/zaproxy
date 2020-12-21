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
            Variant variant = listVariant.get(i);
            try {
                variant.setMessage(msg);
                scanVariant(variant);

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
    private void scanVariant(Variant variant) {
        HttpMessage msg = getNewMsg();
        try {
            scan(msg, variant);
        } catch (Exception e) {
            logger.error("Error occurred while scanning a message:", e);
        }
    }

    /**
     * Scan the current message using the provided Variant
     *
     * @param msg a copy of the HTTP message currently under scanning
     * @param variant
     */
    public abstract void scan(HttpMessage msg, Variant variant);
}
