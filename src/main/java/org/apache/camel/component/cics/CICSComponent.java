package org.apache.camel.component.cics;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CICS component to invoke programs running on CTG/CICS systems.
 * 
 * @author Sergio Gutierrez (sgutierr@redhat.com)
 * @author Jose Roman Martin Gil (rmarting@redhat.com)
 */
public class CICSComponent extends DefaultComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CICSComponent.class);

    /**
     * @return New instance of <code>CICSEndpoint</code>
     * 
     * @see org.apache.camel.impl.DefaultComponent#createEndpoint(java.lang.String)
     */
    @Override
    public Endpoint createEndpoint(String uri) throws Exception {
        LOGGER.info("Creating CICS Component with uri: {}", uri);

        CICSEndpoint cicsEndpoint = new CICSEndpoint(uri, this);

        LOGGER.info("Created CICS Component");
        
        return cicsEndpoint;
    }

    /**
     * @return New instance of <code>CICSEndpoint</code>
     * 
     * @see org.apache.camel.impl.DefaultComponent#createEndpoint(java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        LOGGER.info("Creating CICS Component with uri: {}. remaining: {}, parameters: {}", uri, remaining, parameters);

        CICSEndpoint cicsEndpoint = new CICSEndpoint(uri, this, remaining, parameters);
        setProperties(cicsEndpoint, parameters);

        LOGGER.info("Created CICS Component");

        return cicsEndpoint;
    }

    /**
     * @see org.apache.camel.impl.DefaultComponent#validateParameters(java.lang.String, java.util.Map, java.lang.String)
     */
    @Override
    protected void validateParameters(String uri, Map<String, Object> parameters, String optionPrefix) {
        LOGGER.info("Validating parameter: uri: {}, parameters: {}, optionPrefix: '{}'", uri, parameters, optionPrefix);

        super.validateParameters(uri, parameters, optionPrefix);
    }

}
