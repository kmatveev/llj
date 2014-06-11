package llj.asm.bytecode.exec;

public class WrongClass extends RuntimeTrouble {

    public WrongClass() {
    }

    public WrongClass(String message) {
        super(message);
    }

    public WrongClass(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongClass(Throwable cause) {
        super(cause);
    }

    public WrongClass(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
