package llj.asm.bytecode.exec;

public class ClassLoadingTrouble extends RuntimeTrouble {

    public ClassLoadingTrouble() {
    }

    public ClassLoadingTrouble(String message) {
        super(message);
    }

    public ClassLoadingTrouble(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassLoadingTrouble(Throwable cause) {
        super(cause);
    }

    public ClassLoadingTrouble(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
