package org.apache.camel.component.cics;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.component.cics.adapter.CICSAdapter;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CICS Producer
 * 
 * @author Sergio Gutierrez (sgutierr@redhat.com)
 * @author Jose Roman Martin Gil (rmarting@redhat.com)
 */
public class CICSProducer extends DefaultProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CICSProducer.class);

    /**
     * @param endpoint
     */
    public CICSProducer(CICSEndpoint endpoint) {
        super(endpoint);

        LOGGER.info("New CICS Producer");
    }

    /**
     * Process the exchange
     * 
     * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing Exchange {}", exchange);
        }

        processCTGProcedure(getEndpoint(), exchange);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Procesed Exchange {}", exchange);
        }
    }

    /**
     * @param cicsEndpoint
     * @param exchange
     * 
     * @throws Exception
     */
    private void processCTGProcedure(CICSEndpoint cicsEndpoint, Exchange exchange) throws Exception {
        // All Headers
        Map<String, Object> headers = exchange.getIn().getHeaders();
        // Program Headers: programName, transactionID and commAreaSize
        String programName = (String) headers.get("programName");
        String transactionId = (String) headers.get("transactionId");
        int commAreaSize = -1;
        if (null != headers.get("commAreaSize")) {
            commAreaSize = Integer.parseInt(headers.get("commAreaSize").toString());
        }

        // Input CommArea Data from Exchange
        Object commArea = exchange.getIn().getBody();
        // Output CommArea Data to include in Exchange
        Object result = null;

        // CICS Adapter
        CICSAdapter cicsAdapter = null;

        try {
            // Initialize CICS adapter
            cicsAdapter = new CICSAdapter(cicsEndpoint);

            // Open Gateway
            cicsAdapter.open();

            // Preparing CommArea Data
            if (commArea instanceof String) {
                // Execute Transaction with String
                result = cicsAdapter.runTransaction(programName, transactionId, (String) commArea, commAreaSize);
            } else if (commArea instanceof byte[]) {
                // Execute Transaction with byte[]
                result = cicsAdapter.runTransaction(programName, transactionId, (byte[]) commArea);
            } else {
                byte[] abCommArea = null;
                if (commAreaSize > 0) {
                    abCommArea = new byte[commAreaSize];
                } else {
                    abCommArea = new byte[0];
                }

                LOGGER.warn("Run Transaction with data format not available. Defining Default CommArea with size: {}", abCommArea.length);

                // Execute Transaction with byte[]
                result = cicsAdapter.runTransaction(programName, transactionId, abCommArea);
            }

            // Set Output Results in Exchange
            // copy headers
            exchange.getOut().setHeaders(headers);
            // producer returns a single response
            exchange.getOut().setBody(result);
        } catch (Exception e) {
            LOGGER.error("Exception in process Exchange. Message: " + e.getMessage(), e);

            // Setting exception in exchange
            exchange.setException(e);
        } finally {
            // Close all connections
            cicsAdapter.close();
        }
    }

    @Override
    public CICSEndpoint getEndpoint() {
        return (CICSEndpoint) super.getEndpoint();
    }

}
