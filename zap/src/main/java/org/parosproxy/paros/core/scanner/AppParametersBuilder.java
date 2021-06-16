package org.parosproxy.paros.core.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@code AppParametersBuilder} provides an easy way to build {@code AppParameters} and validates
 * the provided inputs.
 *
 * @author preetkaran20@gmail.com KSASAN
 */
public class AppParametersBuilder {

    private List<AppParameter> appParameters = new ArrayList<>();
    private Set<Integer> nameValuePairPositions = new HashSet<>();
    private final Logger logger = LogManager.getLogger(this.getClass());

    public AppParametersBuilder addAppParameter(
            NameValuePair nameValuePair, String param, String value, AppParamValueType operation) {
        if (!nameValuePairPositions.contains(nameValuePair.getPosition())) {
            appParameters.add(new AppParameter(nameValuePair, param, value, operation));
            nameValuePairPositions.add(nameValuePair.getPosition());
        } else {
            logger.debug("NameValuePair: {} already added earlier so ignoring it", nameValuePair);
        }
        return this;
    }

    public List<AppParameter> build() {
        return Collections.unmodifiableList(appParameters);
    }
}
