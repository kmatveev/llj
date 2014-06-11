package llj.packager.jclass.constants;

import llj.packager.jclass.FormatException;

public class ResolveException extends FormatException {

    public ResolveException() {
    }

    public ResolveException(String message) {
        super(message);
    }

    public ResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveException(Throwable cause) {
        super(cause);
    }

    public ResolveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
