package org.apache.camel.component.cics.exceptions;

/**
 * CICS Exception
 * 
 * @author Sergio Gutierrez (sgutierr@redhat.com)
 * @author Jose Roman Martin Gil (rmarting@redhat.com)
 */
public class CICSException extends Exception {

    private static final long serialVersionUID = -7940232221012039607L;

    public CICSException() {
        super();
    }

    public CICSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CICSException(String message, Throwable cause) {
        super(message, cause);
    }

    public CICSException(String message) {
        super(message);
    }

    public CICSException(Throwable cause) {
        super(cause);
    }

}
