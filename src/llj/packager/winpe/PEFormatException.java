package llj.packager.winpe;

public class PEFormatException extends Exception {

    public PEFormatException(String message) {
        super(message);
    }

    public PEFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public PEFormatException(Throwable cause) {
        super(cause);
    }
}
