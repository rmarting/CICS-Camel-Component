package org.apache.camel.component.cics.adapter;

import java.io.IOException;
import java.util.Properties;

import org.apache.camel.component.cics.CICSEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ctg.client.ECIRequest;
import com.ibm.ctg.client.JavaGateway;

/**
 * This is an adapter class to access IBM CTG/CICS gateway.<br/>
 * 
 * This class uses com.ibm.ctg.client IBM CTG connector proprietary APIs.
 * 
 * @author Sergio Gutierrez (sgutierr@redhat.com)
 * @author Jose Roman Martin Gil (rmarting@redhat.com)
 */
public class CICSAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CICSAdapter.class);

    /** Default Gateway URL */
    public static final String DEFAULT_URL = "local://dummy:2006";

    /** TCP Protocol */
    private static final String GW_PROTOCOL_TCP = "tcp";

    // JavaGateway to connect to CTG/CICS
    private JavaGateway gateway = null;
    // CICSEndpoint to call CTG/CICS
    private CICSEndpoint endpoint = null;

    /**
     * Constructor.<br/>
     * 
     * Connection string data is defined from the end point used. If no URL is defined then the default connection string will
     * be used.<br/>
     * 
     * @param cicsEndpoint end point data to CTG/CICS
     * 
     * @throws IOException
     */
    public CICSAdapter(CICSEndpoint cicsEndpoint) throws IOException {
        LOGGER.info("New CICS Adapter with endpoing {}", cicsEndpoint);

        // Setting local variables
        this.endpoint = cicsEndpoint;

        // Defining Gateway URL
        String gatewayURL = DEFAULT_URL;
        if (cicsEndpoint.getHost() != null) {
            gatewayURL = GW_PROTOCOL_TCP + "://" + cicsEndpoint.getHost() + ":" + cicsEndpoint.getPort();
        }

        LOGGER.info("New Gateway URL {} define", gatewayURL);

        try {
            // Managing Trace and Debug properties
            if (cicsEndpoint.getCtgDebug()) {
                com.ibm.ctg.client.T.setDebugOn(true);
            } else {
                com.ibm.ctg.client.T.setDebugOn(false);
            }

            // Creating JavaGateway
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating empty JavaGateway");
            }

            // The JavaGateway represents a logical connection between your program and the CICS Transaction Gateway
            // when you specify a remote protocol.
            // If you specify a local connection, you connect directly to the CICS server, bypassing any CICS Transaction
            // Gateway servers.
            this.gateway = new JavaGateway();

            // if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Setting connection data to JavaGateway. Protocol: {}, Address: {}, Port: {}", GW_PROTOCOL_TCP,
                    cicsEndpoint.getHost(), cicsEndpoint.getPort());
            // }

            //
            // The correct gateway URL format is: protocol://address:port/
            //
            // When you create a JavaGateway you determine the protocol to use, and if required,
            // the connection details of the remote CICS Transaction Gateway server (network address and port number).
            // With the JavaGateway class you can either specify this information using the setAddress(), setProtocol() and
            // setPort() methods, or you can provide all the information in URL form of Protocol://Address:Port.
            // You can use the setURL() method or pass the URL into one of the JavaGateway constructors.
            this.gateway.setProtocol(GW_PROTOCOL_TCP);
            this.gateway.setAddress(cicsEndpoint.getHost());
            this.gateway.setPort(cicsEndpoint.getPort());
            this.gateway.setInitialFlow(false);

            // TODO Set other properties: socketConnectionTimeout, ...
            // this.gateway.setSocketConnectTimeout(90000);

            // LOGGER.info("JavaGateway info {}", this.gateway);

            // Set the keyring and keyring password
            if (cicsEndpoint.getSslKeyring() != null && cicsEndpoint.getSslPassword() != null) {
                Properties sslProps = new Properties();
                sslProps.setProperty(JavaGateway.SSL_PROP_KEYRING_CLASS, cicsEndpoint.getSslKeyring());
                sslProps.setProperty(JavaGateway.SSL_PROP_KEYRING_PW, cicsEndpoint.getSslPassword());

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Setting SSL properties");
                }
                this.gateway.setProtocolProperties(sslProps);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to create JavaGateway to CTG/CICS: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Open the Gateway to CTG/CICS
     * 
     * @throws IOException
     */
    public void open() throws IOException {
        try {
            if (!this.gateway.isOpen()) {
                this.gateway.open();

                if (this.gateway.isOpen()) {
                    LOGGER.info("Gateway opened to CTC/CICS");
                }
            } else {
                LOGGER.info("Gateway already opened to CTC/CICS");
            }
        } catch (IOException e) {
            LOGGER.error("Unable to open gateway: " + e.getMessage(), e);
            throw e;
        }
    }

    public String runTransaction(String programName, String transactionId, String inputCommArea) throws IOException, Exception {
        // Shared CommArea in byte format
        byte[] byteCommArea = inputCommArea.getBytes(this.endpoint.getEncoding());

        // if (LOGGER.isDebugEnabled()) {
        LOGGER.info("Input CommArea String Data:\n-** INPUT COMMAREA **-\n{}\n-** END INPUT COMMAREA **-", inputCommArea);
        // }

        // Executing Transaction with byte array
        byteCommArea = runTransaction(programName, transactionId, byteCommArea);

        // Output CommArea in String Format
        String outputCommArea = new String(byteCommArea, this.endpoint.getEncoding());

        // if (LOGGER.isDebugEnabled()) {
        LOGGER.info("Output CommArea String Data:\n-** OUTPUT COMMAREA **-\n{}\n-** END OUTPUT COMMAREA **-", outputCommArea);
        // }

        return outputCommArea;
    }

    public byte[] runTransaction(String programName, String transactionId, byte[] inputCommArea) throws IOException, Exception {
        // Shared CommArea in byte format
        byte[] byteCommArea = inputCommArea;

        try {
            LOGGER.info("New ECIRequest in {} server to call {} program with {} transaction", this.endpoint.getServer(),
                    programName, transactionId);

            // ECI Request to call CTG/CICS
            ECIRequest eciRequestObject = new ECIRequest(ECIRequest.ECI_SYNC, // ECI call type
                    this.endpoint.getServer(), // CICS Server
                    this.endpoint.getUserId(), // CICS userid
                    this.endpoint.getPassword(), // CICS password
                    programName, // CICS program to be run
                    transactionId, // CICS transid to be run
                    byteCommArea); // Byte array containing the COMMAREA

            // TODO Other properties
            // eciRequestObject.setECITimeout((short)90000);

            // if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Input CommArea Byte[] Data:\n-** INPUT COMMAREA **-\n{}\n-** END INPUT COMMAREA **-", inputCommArea);
            // }

            // Call the flowRequest method
            boolean flowResult = flowRequest(eciRequestObject);

            if (!flowResult) {
                LOGGER.warn("Something was not working in flowRequest");
            }

            // if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Output CommArea Byte[] Data:\n-** OUTPUT COMMAREA **-\n{}\n-** END OUTPUT COMMAREA **-", byteCommArea);
            // }
        } catch (Exception e) {
            LOGGER.error("Unable to exectue transaction in CTG/CICS: " + e.getMessage(), e);
            throw e;
        }

        return byteCommArea;
    }

    /**
     * The flowRequest method flows data contained in the ECIRequest object to the Gateway and determines whether it has been
     * successful by checking the return code. If an error has occurred, the return code string and abend codes are printed to
     * describe the error before the program exits. Note: Security may be required for client connection to the server and not
     * just for the ECI request. Refer to the security chapter in the product documentation for further details.
     * 
     * @param requestObject
     * 
     * @return <code>true</code> if flow was executed successfully, <code>false</code> otherwise
     */
    private boolean flowRequest(ECIRequest requestObject) throws IOException {
        boolean flowOk = true;

        try {
            LOGGER.info("Executing flow on Gateway {}", this.gateway);

            int iRc = this.gateway.flow(requestObject);

            LOGGER.info("Executed flow on Gateway {}", this.gateway);

            // if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Flow Result Code '{}' on Gateway {}", iRc, this.gateway);
            LOGGER.info("CicsCodes: {}-{} on Gateway {}", requestObject.getCicsRc(), requestObject.getCicsRcString(),
                    this.gateway);
            LOGGER.info("RcCodes: {}-{} on Gateway {}", requestObject.getRc(), requestObject.getRcString(), this.gateway);
            // }

            // Evaluation Result Codes
            if (iRc != 0) {
                if (requestObject.getCicsRc() == 0) {
                    LOGGER.warn("Gateway Flow Exception. Return code number:" + iRc + " Return code String: "
                            + requestObject.getRcString());
                    flowOk = false;
                } else {
                    if (requestObject.getCicsRc() == ECIRequest.ECI_ERR_SECURITY_ERROR
                            || (requestObject.Abend_Code != null && requestObject.Abend_Code.equalsIgnoreCase("AEY7"))) {
                        LOGGER.warn("Security Flow Exception. Server is unable to validate user ID or password");
                        flowOk = false;
                    } else if (requestObject.getCicsRc() == ECIRequest.ECI_ERR_TRANSACTION_ABEND) {
                        LOGGER.warn(
                                "Program Flow Exception. An error was returned from the server. Refer to the abend code for further details. '{}'",
                                requestObject.Abend_Code);
                        flowOk = false;
                    } else {
                        LOGGER.info("Unknown Flow Exception. Return code number: {}. Return code String: {}", iRc,
                                requestObject.getCicsRcString());
                        flowOk = false;
                    }
                }
            } else {
                LOGGER.info("Flow executed successfully");
            }
        } catch (IOException e) {
            LOGGER.error("Unable to execute flow request: " + e.getMessage(), e);
            throw e;
        }

        return flowOk;
    }

    /**
     * Close and finalize the current Gateway to CTG/CICS
     */
    public void close() {
        try {
            if (gateway != null) {
                if (gateway.isOpen()) {
                    gateway.close();
                }
                this.gateway = null;
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to close Gateway: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CICSAdapter [gateway=");
        builder.append(gateway);
        builder.append(", endpoint=");
        builder.append(endpoint);
        builder.append("]");
        return builder.toString();
    }

}
