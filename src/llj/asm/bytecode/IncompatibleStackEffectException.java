package llj.asm.bytecode;

public class IncompatibleStackEffectException extends Exception {

    public IncompatibleStackEffectException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleStackEffectException(String message) {
        super(message);
    }

    public IncompatibleStackEffectException() {
    }
}
