package llj.packager.coff;

public class COFFFormatException extends Exception {

    public COFFFormatException(String message) {
        super(message);
    }

    public COFFFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public COFFFormatException(Throwable cause) {
        super(cause);
    }

    public COFFFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
