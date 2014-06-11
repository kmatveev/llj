package llj.asm.bytecode.exec;

public class RuntimeTrouble extends Exception {

    public RuntimeTrouble() {
    }

    public RuntimeTrouble(String message) {
        super(message);
    }

    public RuntimeTrouble(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeTrouble(Throwable cause) {
        super(cause);
    }

    public RuntimeTrouble(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
